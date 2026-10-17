package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelEvent
import `in`.hridayan.ashell.portforward.domain.model.TunnelEventKind
import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.TunnelStats
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import `in`.hridayan.ashell.portforward.domain.model.toTunnelError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Runs one tunnel: listens on its local port and relays every accepted connection to its target.
 *
 * While [Config.availability] reports the target unreachable, the port stays bound and new clients
 * are closed at once, so they fail fast instead of waiting for a connect timeout.
 */
class TunnelRunner(
    private val spec: TunnelSpec,
    private val config: Config,
    private val connector: TargetConnector,
    private val pipe: SocketPipe,
    private val dispatcher: CoroutineDispatcher,
    private val clock: () -> Long = System::currentTimeMillis
) {

    /**
     * @property availability emits null while the target can be reached and the reason while it
     * cannot; null itself when the target is always reachable.
     * @property waitTimeoutMillis how long to wait for a device to return before stopping, or null to
     * wait indefinitely. Network waits never time out.
     * @property resumeOnReconnect when false, losing the device stops the tunnel instead of waiting.
     * @property sharedCap a limit shared with the other tunnels on the same device link, so that all
     * of them together cannot flood it; null for tunnels that do not share a link.
     */
    data class Config(
        val bindAddress: InetAddress,
        val connectTimeoutMillis: Int,
        val maxConnections: Int,
        val availability: Flow<WaitReason?>?,
        val waitTimeoutMillis: Long? = null,
        val resumeOnReconnect: Boolean = true,
        val sharedCap: StreamCap? = null
    )

    private val cap = StreamCap(config.maxConnections)
    private val openEndpoints = ConcurrentHashMap.newKeySet<PipeEndpoint>()
    private val openConnections = AtomicInteger()
    private val totalConnections = AtomicInteger()
    private val failedConnections = AtomicInteger()
    private val refusedConnections = AtomicInteger()
    private val bytesSent = AtomicLong()
    private val bytesReceived = AtomicLong()
    private var server: ServerSocket? = null
    private var startedAt = 0L
    private var deadlineJob: Job? = null

    private val _runtime = MutableStateFlow(
        TunnelRuntime(spec = spec, state = TunnelState.Starting, stats = TunnelStats(), events = emptyList())
    )
    val runtime: StateFlow<TunnelRuntime> = _runtime.asStateFlow()

    /** Binds the local port. Blocking; a failure leaves nothing bound. */
    fun bind(): Result<Unit> = try {
        server = ServerSocket().apply {
            reuseAddress = true
            bind(InetSocketAddress(config.bindAddress, spec.localPort), ACCEPT_BACKLOG)
        }
        Result.success(Unit)
    } catch (e: IOException) {
        Result.failure(e)
    }

    /** Starts relaying. Call only after a successful [bind]. */
    fun launchIn(scope: CoroutineScope): Job = scope.launch {
        startedAt = clock()
        record(TunnelEventKind.Started) { it.copy(state = TunnelState.Ready) }
        config.availability?.let { availability -> launch { watchAvailability(availability, this) } }
        launch { publishStatsWhileActive() }
        acceptLoop(this)
    }

    /** Stops listening and drops every relayed connection. */
    fun close() {
        server?.closeQuietly()
        openEndpoints.forEach { it.closeQuietly() }
        openEndpoints.clear()
    }

    private suspend fun acceptLoop(scope: CoroutineScope) {
        val listener = server ?: return
        while (currentCoroutineContext().isActive) {
            val client = try {
                runInterruptible(dispatcher) { listener.accept() }
            } catch (e: IOException) {
                if (!listener.isClosed) onListenerFailed(e)
                return
            }
            dispatch(client, scope)
        }
    }

    private fun dispatch(client: Socket, scope: CoroutineScope) {
        client.tcpNoDelay = true
        when {
            _runtime.value.state is TunnelState.Waiting -> refuse(client, null)
            !acquireStream() -> refuse(client, TunnelError.StreamCapReached)
            else -> scope.launch(dispatcher) { relay(client) }
        }
    }

    private fun refuse(client: Socket, error: TunnelError?) {
        client.closeQuietly()
        refusedConnections.incrementAndGet()
        error?.let { refusal ->
            record(TunnelEventKind.ConnectionRefused(refusal)) { it.copy(state = TunnelState.Warning(refusal)) }
        }
        publishStats()
    }

    private suspend fun relay(client: Socket) {
        val clientEnd = SocketEndpoint(client, dispatcher)
        openEndpoints.add(clientEnd)
        try {
            val target = connector.connect(spec.target, config.connectTimeoutMillis)
                .getOrElse { error ->
                    onConnectFailed(error.toTunnelError())
                    return
                }
            pipeTo(clientEnd, target)
        } finally {
            clientEnd.closeQuietly()
            openEndpoints.remove(clientEnd)
            releaseStream()
        }
    }

    private fun acquireStream(): Boolean {
        if (!cap.tryAcquire()) return false
        if (config.sharedCap?.tryAcquire() == false) {
            cap.release()
            return false
        }
        return true
    }

    private fun releaseStream() {
        cap.release()
        config.sharedCap?.release()
    }

    private suspend fun pipeTo(clientEnd: PipeEndpoint, target: PipeEndpoint) {
        openEndpoints.add(target)
        onConnected()
        try {
            val totals = pipe.run(clientEnd, target, bytesSent, bytesReceived)
            record(TunnelEventKind.ConnectionClosed(bytesReceived = totals.toClient, bytesSent = totals.fromClient))
        } finally {
            openEndpoints.remove(target)
            onDisconnected()
        }
    }

    private fun onConnected() {
        openConnections.incrementAndGet()
        totalConnections.incrementAndGet()
        _runtime.update { current ->
            if (current.state is TunnelState.Waiting) current else current.copy(state = TunnelState.Active)
        }
        publishStats()
    }

    private fun onDisconnected() {
        val remaining = openConnections.decrementAndGet()
        _runtime.update { current ->
            val idle = remaining == 0 && current.state is TunnelState.Active
            if (idle) current.copy(state = TunnelState.Ready) else current
        }
        publishStats()
    }

    private fun onConnectFailed(error: TunnelError) {
        failedConnections.incrementAndGet()
        record(TunnelEventKind.ConnectionFailed(error)) { current ->
            if (current.state is TunnelState.Waiting) current else current.copy(state = TunnelState.Warning(error))
        }
        publishStats()
    }

    private fun onListenerFailed(error: IOException) = fail(TunnelError.Unexpected(error.message))

    private fun fail(error: TunnelError) {
        deadlineJob?.cancel()
        setState(TunnelState.Failed(error))
        close()
    }

    private suspend fun watchAvailability(availability: Flow<WaitReason?>, scope: CoroutineScope) {
        availability.distinctUntilChanged().collect { reason ->
            val waiting = _runtime.value.state is TunnelState.Waiting
            when {
                reason != null && !waiting -> startWaiting(reason, scope)
                reason == null && waiting -> resume()
            }
        }
    }

    private fun startWaiting(reason: WaitReason, scope: CoroutineScope) {
        if (reason == WaitReason.Device && !config.resumeOnReconnect) {
            fail(TunnelError.DeviceNotConnected)
            return
        }
        val timeout = config.waitTimeoutMillis.takeIf { reason == WaitReason.Device }
        val deadline = timeout?.let { clock() + it }
        record(reason.waitingEvent()) { it.copy(state = TunnelState.Waiting(reason, deadline)) }
        timeout?.let { deadlineJob = scope.launch { expireAfter(it) } }
    }

    private suspend fun expireAfter(timeoutMillis: Long) {
        delay(timeoutMillis)
        if (_runtime.value.state is TunnelState.Waiting) fail(TunnelError.DeviceDidNotReturn)
    }

    private fun resume() {
        deadlineJob?.cancel()
        val reason = (_runtime.value.state as? TunnelState.Waiting)?.reason ?: return
        record(reason.restoredEvent()) {
            it.copy(state = if (openConnections.get() > 0) TunnelState.Active else TunnelState.Ready)
        }
    }

    private fun WaitReason.waitingEvent(): TunnelEventKind = when (this) {
        WaitReason.Network -> TunnelEventKind.WaitingForNetwork
        WaitReason.Device -> TunnelEventKind.WaitingForDevice
    }

    private fun WaitReason.restoredEvent(): TunnelEventKind = when (this) {
        WaitReason.Network -> TunnelEventKind.NetworkRestored
        WaitReason.Device -> TunnelEventKind.DeviceReconnected
    }

    private suspend fun publishStatsWhileActive() {
        while (currentCoroutineContext().isActive) {
            delay(STATS_INTERVAL_MS)
            if (openConnections.get() > 0) publishStats()
        }
    }

    private fun publishStats() {
        val stats = TunnelStats(
            openConnections = openConnections.get(),
            totalConnections = totalConnections.get(),
            failedConnections = failedConnections.get(),
            refusedConnections = refusedConnections.get(),
            bytesReceived = bytesReceived.get(),
            bytesSent = bytesSent.get(),
            startedAtMillis = startedAt
        )
        _runtime.update { if (it.stats == stats) it else it.copy(stats = stats) }
    }

    private fun setState(state: TunnelState) {
        _runtime.update { if (it.state == state) it else it.copy(state = state) }
    }

    /** Adds an event and applies [transform] in one update, so observers see both together. */
    private fun record(kind: TunnelEventKind, transform: (TunnelRuntime) -> TunnelRuntime = { it }) {
        val event = TunnelEvent(atMillis = clock(), kind = kind)
        _runtime.update { current ->
            val previous = current.events.firstOrNull()
            val events = if (previous?.kind == kind && kind.collapses) {
                listOf(event) + current.events.drop(1)
            } else {
                (listOf(event) + current.events).take(MAX_EVENTS)
            }
            transform(current).copy(events = events)
        }
    }

    private val TunnelEventKind.collapses: Boolean
        get() = this is TunnelEventKind.ConnectionFailed || this is TunnelEventKind.ConnectionRefused

    private companion object {
        const val ACCEPT_BACKLOG = 50
        const val MAX_EVENTS = 50
        const val STATS_INTERVAL_MS = 1_000L
    }
}
