package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelEventKind
import `in`.hridayan.ashell.portforward.domain.model.TunnelException
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class TunnelRunnerTest {

    private val echo = EchoServer()
    private val connector = FakeConnector(port = { echo.port })
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val runners = mutableListOf<TunnelRunner>()

    @After
    fun tearDown() {
        runners.forEach { it.close() }
        scope.cancel()
        echo.close()
    }

    private fun runner(
        maxConnections: Int = 4,
        availability: Flow<WaitReason?>? = null,
        waitTimeoutMillis: Long? = null,
        resumeOnReconnect: Boolean = true,
        sharedCap: StreamCap? = null,
        localPort: Int = freeLoopbackPort()
    ): TunnelRunner = TunnelRunner(
        spec = TunnelSpec(
            id = "t1",
            name = "Test",
            localPort = localPort,
            shareOnNetwork = false,
            target = TunnelTarget.NetworkHost("10.0.0.2", 80),
            autoStart = false,
            looksLikeHttp = false
        ),
        config = TunnelRunner.Config(
            bindAddress = InetAddress.getLoopbackAddress(),
            connectTimeoutMillis = TIMEOUT_MS.toInt(),
            maxConnections = maxConnections,
            availability = availability,
            waitTimeoutMillis = waitTimeoutMillis,
            resumeOnReconnect = resumeOnReconnect,
            sharedCap = sharedCap
        ),
        connector = connector,
        pipe = SocketPipe(Dispatchers.IO),
        dispatcher = Dispatchers.IO
    ).also { runners += it }

    private fun TunnelRunner.started(): TunnelRunner = apply {
        bind().getOrThrow()
        launchIn(scope)
    }

    private fun connect(runner: TunnelRunner): Socket =
        Socket(InetAddress.getLoopbackAddress(), runner.runtime.value.spec.localPort)

    private suspend fun TunnelRunner.awaitState(predicate: (TunnelState) -> Boolean) =
        withTimeout(TIMEOUT_MS) { runtime.first { predicate(it.state) } }

    @Test
    fun `relays bytes both ways`() = runBlocking<Unit> {
        val runner = runner().started()
        runner.awaitState { it == TunnelState.Ready }

        connect(runner).use { client ->
            client.getOutputStream().write(PAYLOAD)
            val reply = ByteArray(PAYLOAD.size)
            var read = 0
            while (read < reply.size) read += client.getInputStream().read(reply, read, reply.size - read)
            assertEquals(PAYLOAD.toList(), reply.toList())
            runner.awaitState { it == TunnelState.Active }
        }

        runner.awaitState { it == TunnelState.Ready }
        val stats = withTimeout(TIMEOUT_MS) { runner.runtime.first { it.stats.totalConnections == 1 } }.stats
        assertEquals(0, stats.openConnections)
    }

    @Test
    fun `failed connect marks a warning and keeps listening`() = runBlocking<Unit> {
        connector.failWith = TunnelException(TunnelError.TargetRefused)
        val runner = runner().started()
        runner.awaitState { it == TunnelState.Ready }

        connect(runner).use { client -> assertEquals(-1, client.getInputStream().read()) }

        runner.awaitState { it == TunnelState.Warning(TunnelError.TargetRefused) }
        val event = runner.runtime.value.events.first().kind
        assertEquals(TunnelEventKind.ConnectionFailed(TunnelError.TargetRefused), event)
    }

    @Test
    fun `next successful connection clears the warning`() = runBlocking<Unit> {
        connector.failWith = TunnelException(TunnelError.TargetTimeout)
        val runner = runner().started()
        runner.awaitState { it == TunnelState.Ready }
        connect(runner).use { it.getInputStream().read() }
        runner.awaitState { it is TunnelState.Warning }

        connector.failWith = null
        connect(runner).use { client ->
            client.getOutputStream().write(PAYLOAD)
            client.getInputStream().read()
            runner.awaitState { it == TunnelState.Active }
        }
    }

    @Test
    fun `no network waits and refuses clients at once`() = runBlocking<Unit> {
        val network = MutableStateFlow<WaitReason?>(WaitReason.Network)
        val runner = runner(availability = network).started()
        runner.awaitState { it is TunnelState.Waiting }

        connect(runner).use { client -> assertEquals(-1, client.getInputStream().read()) }
        withTimeout(TIMEOUT_MS) { runner.runtime.first { it.stats.refusedConnections == 1 } }

        network.value = null
        runner.awaitState { it == TunnelState.Ready }
        assertEquals(TunnelEventKind.NetworkRestored, runner.runtime.value.events.first().kind)
    }

    @Test
    fun `a device that returns resumes the tunnel`() = runBlocking<Unit> {
        val device = MutableStateFlow<WaitReason?>(null)
        val runner = runner(availability = device, waitTimeoutMillis = TIMEOUT_MS).started()
        runner.awaitState { it == TunnelState.Ready }

        device.value = WaitReason.Device
        val waiting = runner.awaitState { it is TunnelState.Waiting }.state as TunnelState.Waiting
        assertEquals(WaitReason.Device, waiting.reason)
        assertTrue(waiting.deadlineMillis != null)

        device.value = null
        runner.awaitState { it == TunnelState.Ready }
        assertEquals(TunnelEventKind.DeviceReconnected, runner.runtime.value.events.first().kind)
    }

    @Test
    fun `a device that does not return in time stops the tunnel`() = runBlocking<Unit> {
        val device = MutableStateFlow<WaitReason?>(WaitReason.Device)
        val runner = runner(availability = device, waitTimeoutMillis = SHORT_WAIT_MS).started()

        runner.awaitState { it == TunnelState.Failed(TunnelError.DeviceDidNotReturn) }
    }

    @Test
    fun `losing the device stops the tunnel when resuming is off`() = runBlocking<Unit> {
        val device = MutableStateFlow<WaitReason?>(null)
        val runner = runner(availability = device, resumeOnReconnect = false).started()
        runner.awaitState { it == TunnelState.Ready }

        device.value = WaitReason.Device

        runner.awaitState { it == TunnelState.Failed(TunnelError.DeviceNotConnected) }
    }

    @Test
    fun `connections over the cap are refused with a warning`() = runBlocking<Unit> {
        val runner = runner(maxConnections = 1).started()
        runner.awaitState { it == TunnelState.Ready }

        connect(runner).use { first ->
            first.getOutputStream().write(PAYLOAD)
            first.getInputStream().read()
            connect(runner).use { second -> assertEquals(-1, second.getInputStream().read()) }
            runner.awaitState { it == TunnelState.Warning(TunnelError.StreamCapReached) }
        }
    }

    @Test
    fun `tunnels on one device share its connection cap`() = runBlocking<Unit> {
        val deviceCap = StreamCap(1)
        val first = runner(sharedCap = deviceCap).started()
        val second = runner(sharedCap = deviceCap).started()
        first.awaitState { it == TunnelState.Ready }
        second.awaitState { it == TunnelState.Ready }

        connect(first).use { held ->
            held.getOutputStream().write(PAYLOAD)
            held.getInputStream().read()
            connect(second).use { refused -> assertEquals(-1, refused.getInputStream().read()) }
            second.awaitState { it == TunnelState.Warning(TunnelError.StreamCapReached) }
        }
    }

    @Test
    fun `a connection returns its share of the device cap when it ends`() = runBlocking<Unit> {
        val deviceCap = StreamCap(1)
        val runner = runner(sharedCap = deviceCap).started()
        runner.awaitState { it == TunnelState.Ready }

        repeat(3) {
            connect(runner).use { client ->
                client.getOutputStream().write(PAYLOAD)
                assertTrue(client.getInputStream().read() >= 0)
            }
            withTimeout(TIMEOUT_MS) { runner.runtime.first { it.stats.openConnections == 0 } }
        }
    }

    @Test
    fun `bind fails when the port is taken`() {
        ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { taken ->
            val result = runner(localPort = taken.localPort).bind()
            assertTrue(result.isFailure)
        }
    }

    @Test
    fun `close drops open connections`() = runBlocking<Unit> {
        val runner = runner().started()
        runner.awaitState { it == TunnelState.Ready }
        connect(runner).use { client ->
            client.getOutputStream().write(PAYLOAD)
            client.getInputStream().read()
            runner.awaitState { it == TunnelState.Active }

            runner.close()

            assertEquals(-1, drain(client))
        }
    }

    private fun drain(client: Socket): Int {
        val buffer = ByteArray(PAYLOAD.size)
        while (true) {
            val count = try {
                client.getInputStream().read(buffer)
            } catch (_: IOException) {
                END_OF_STREAM
            }
            if (count < 0) return count
        }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
        const val END_OF_STREAM = -1
        const val SHORT_WAIT_MS = 100L
        val PAYLOAD = "hello tunnel".toByteArray()
    }
}
