package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.portforward.data.device.DeviceChannels
import `in`.hridayan.ashell.portforward.di.PortForwardDispatcher
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelException
import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.TunnelStats
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import `in`.hridayan.ashell.portforward.domain.model.allowsNetworkSharing
import `in`.hridayan.ashell.portforward.domain.model.isRunning
import `in`.hridayan.ashell.portforward.domain.notification.TunnelAlertNotifier
import `in`.hridayan.ashell.portforward.domain.repository.NetworkMonitor
import `in`.hridayan.ashell.portforward.domain.repository.PortAvailability
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettings
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettingsSource
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import `in`.hridayan.ashell.portforward.domain.session.PortForwardForegroundSession
import `in`.hridayan.ashell.portforward.domain.usecase.MapTunnelErrorUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single owner of every running tunnel.
 *
 * Start and stop requests are serialised, so a restart never races its own stop. The foreground
 * service is kept running exactly while some tunnel holds its port.
 */
@Singleton
class TunnelEngine @Inject constructor(
    private val connector: TargetConnector,
    private val networkMonitor: NetworkMonitor,
    private val deviceChannels: DeviceChannels,
    private val settings: PortForwardSettingsSource,
    private val repository: SavedTunnelRepository,
    private val ports: PortAvailability,
    private val session: PortForwardForegroundSession,
    private val alerts: TunnelAlertNotifier,
    private val mapError: MapTunnelErrorUseCase,
    @param:PortForwardDispatcher private val dispatcher: CoroutineDispatcher
) : TunnelController {

    private data class RunningTunnel(val runner: TunnelRunner, val jobs: List<Job>)

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()
    private val running = ConcurrentHashMap<String, RunningTunnel>()
    private val sessionLock = Any()
    private val deviceLinkCaps = ExternalDeviceTransport.entries.associateWith {
        StreamCap(
            MAX_STREAMS_PER_DEVICE_LINK
        )
    }
    private var sessionActive = false

    private val _runtimes = MutableStateFlow<Map<String, TunnelRuntime>>(emptyMap())
    override val runtimes: StateFlow<Map<String, TunnelRuntime>> = _runtimes.asStateFlow()

    override suspend fun start(spec: TunnelSpec): Result<Unit> = mutex.withLock {
        shutdown(spec.id)
        val runner = createRunner(spec, settings.current())
        val bound = withContext(dispatcher) { runner.bind() }
        bound.exceptionOrNull()?.let { return@withLock failStart(spec, bindError(spec, it)) }
        val mirrorJob = mirror(spec.id, runner)
        running[spec.id] = RunningTunnel(runner, listOf(mirrorJob, runner.launchIn(scope)))
        _runtimes.update { it + (spec.id to runner.runtime.value) }
        mirrorJob.start()
        if (!ensureSession()) {
            shutdown(spec.id)
            return@withLock failStart(spec, TunnelError.BackgroundStartBlocked)
        }
        repository.setWasRunning(listOf(spec.id), wasRunning = true)
        Result.success(Unit)
    }

    override suspend fun stop(id: String) {
        mutex.withLock {
            shutdown(id)
            _runtimes.update { it - id }
            repository.setWasRunning(listOf(id), wasRunning = false)
            updateSession()
        }
    }

    override suspend fun stopAll(): List<TunnelSpec> = mutex.withLock {
        val stopped = _runtimes.value.values.filter { it.state.isRunning }.map { it.spec }
        running.keys.toList().forEach { shutdown(it) }
        _runtimes.value = emptyMap()
        repository.setWasRunning(stopped.map { it.id }, wasRunning = false)
        updateSession()
        stopped
    }

    override fun dismiss(id: String) {
        _runtimes.update { current ->
            if (current[id]?.state is TunnelState.Failed) current - id else current
        }
    }

    private fun createRunner(spec: TunnelSpec, current: PortForwardSettings) = TunnelRunner(
        spec = spec,
        config = TunnelRunner.Config(
            bindAddress = bindAddressFor(spec, current),
            connectTimeoutMillis = current.connectTimeoutMillis,
            maxConnections = current.maxConnections,
            availability = availabilityOf(spec.target),
            waitTimeoutMillis = current.waitTimeoutMillis,
            resumeOnReconnect = current.resumeOnReconnect,
            sharedCap = (spec.target as? TunnelTarget.Device)?.let { deviceLinkCaps.getValue(it.transport) }
        ),
        connector = connector,
        pipe = SocketPipe(dispatcher),
        dispatcher = dispatcher
    )

    private fun mirror(id: String, runner: TunnelRunner): Job = scope.launch(start = CoroutineStart.LAZY) {
        runner.runtime.collect { runtime ->
            if (running[id]?.runner !== runner) return@collect
            val previous = _runtimes.value[id]?.state
            _runtimes.update { it + (id to runtime) }
            val state = runtime.state
            if (state is TunnelState.Failed && previous !is TunnelState.Failed) {
                onRunnerFailed(runtime.spec, state.error)
            }
        }
    }

    private fun onRunnerFailed(spec: TunnelSpec, error: TunnelError) {
        scope.launch {
            mutex.withLock {
                shutdown(spec.id)
                repository.setWasRunning(listOf(spec.id), wasRunning = false)
                updateSession()
            }
            alerts.tunnelFailed(spec, error)
        }
    }

    private suspend fun bindError(spec: TunnelSpec, error: Throwable): TunnelError =
        mapError.forBind(error, ports.nextFree(spec.localPort))

    private fun failStart(spec: TunnelSpec, error: TunnelError): Result<Unit> {
        _runtimes.update {
            it + (spec.id to TunnelRuntime(spec, TunnelState.Failed(error), TunnelStats(), emptyList()))
        }
        updateSession()
        return Result.failure(TunnelException(error))
    }

    private fun shutdown(id: String) {
        val tunnel = running.remove(id) ?: return
        tunnel.runner.close()
        tunnel.jobs.forEach { it.cancel() }
    }

    private fun ensureSession(): Boolean = synchronized(sessionLock) {
        if (sessionActive) return true
        sessionActive = session.start().isSuccess
        sessionActive
    }

    private fun updateSession() = synchronized(sessionLock) {
        val anyRunning = _runtimes.value.values.any { it.state.isRunning }
        if (!anyRunning && sessionActive) {
            session.stop()
            sessionActive = false
        }
    }

    private fun bindAddressFor(spec: TunnelSpec, current: PortForwardSettings): InetAddress {
        val shared = spec.shareOnNetwork && current.allowLanSharing && spec.target.allowsNetworkSharing
        return if (shared) InetAddress.getByName(ANY_ADDRESS) else InetAddress.getLoopbackAddress()
    }

    private fun availabilityOf(target: TunnelTarget): Flow<WaitReason?>? = when (target) {
        is TunnelTarget.NetworkHost -> if (target.isOnThisPhone()) null else networkAvailability()
        is TunnelTarget.Device -> deviceChannels.availability(target)
    }

    private fun networkAvailability(): Flow<WaitReason?> =
        networkMonitor.isConnected.map { connected -> if (connected) null else WaitReason.Network }

    private fun TunnelTarget.NetworkHost.isOnThisPhone(): Boolean =
        host.equals(LOCALHOST, ignoreCase = true) || host.isLoopbackLiteral()

    private fun String.isLoopbackLiteral(): Boolean =
        startsWith(IPV4_LOOPBACK_PREFIX) || this == IPV6_LOOPBACK

    private companion object {
        const val ANY_ADDRESS = "0.0.0.0"

        /**
         * Relayed connections across every tunnel on one device link. Each one can queue a packet
         * ahead of the link's other traffic, so this bounds how long mirroring or the shell can
         * wait behind tunnels.
         */
        const val MAX_STREAMS_PER_DEVICE_LINK = 16
        const val LOCALHOST = "localhost"
        const val IPV4_LOOPBACK_PREFIX = "127."
        const val IPV6_LOOPBACK = "::1"
    }
}
