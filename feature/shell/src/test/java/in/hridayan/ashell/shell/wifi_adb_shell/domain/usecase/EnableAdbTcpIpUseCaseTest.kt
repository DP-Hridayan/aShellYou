package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.usecase

import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.TcpIpEnableResult
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.AdbTcpIpGateway
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.TcpIpRequestOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EnableAdbTcpIpUseCaseTest {

    private class FakeGateway(
        private val connected: Boolean = true,
        private val outcome: TcpIpRequestOutcome = TcpIpRequestOutcome.Accepted,
        private val portReadings: List<Int?> = emptyList()
    ) : AdbTcpIpGateway {
        var requestedPort: Int? = null
        var portReads = 0

        override fun isConnected() = connected

        override suspend fun requestTcpIp(port: Int): TcpIpRequestOutcome {
            requestedPort = port
            return outcome
        }

        override fun currentTcpPort(): Int? = portReadings.getOrNull(portReads++)
    }

    @Test
    fun `no connection is reported without sending anything`() = runTest {
        val gateway = FakeGateway(connected = false)

        assertEquals(TcpIpEnableResult.NotConnected, EnableAdbTcpIpUseCase(gateway)(PORT))
        assertEquals(null, gateway.requestedPort)
    }

    @Test
    fun `the requested port is the one sent`() = runTest {
        val gateway = FakeGateway(portReadings = listOf(PORT))

        EnableAdbTcpIpUseCase(gateway)(PORT)

        assertEquals(PORT, gateway.requestedPort)
    }

    @Test
    fun `a refusal carries the daemon message`() = runTest {
        val gateway = FakeGateway(outcome = TcpIpRequestOutcome.Refused("invalid port"))

        assertEquals(TcpIpEnableResult.Refused("invalid port"), EnableAdbTcpIpUseCase(gateway)(PORT))
    }

    @Test
    fun `an accepted request is opened once the port appears`() = runTest {
        val gateway = FakeGateway(portReadings = listOf(null, null, PORT))

        assertEquals(TcpIpEnableResult.Opened(PORT), EnableAdbTcpIpUseCase(gateway)(PORT))
    }

    @Test
    fun `a different port does not count as opened`() = runTest {
        val gateway = FakeGateway(portReadings = List(100) { OTHER_PORT })

        assertEquals(TcpIpEnableResult.Unverified, EnableAdbTcpIpUseCase(gateway)(PORT))
    }

    @Test
    fun `an accepted request whose port never appears is unverified`() = runTest {
        val gateway = FakeGateway(portReadings = emptyList())

        assertEquals(TcpIpEnableResult.Unverified, EnableAdbTcpIpUseCase(gateway)(PORT))
    }

    @Test
    fun `polling stops once the port appears`() = runTest {
        val gateway = FakeGateway(portReadings = listOf(null, PORT, PORT, PORT))

        EnableAdbTcpIpUseCase(gateway)(PORT)

        assertEquals(2, gateway.portReads)
    }

    private companion object {
        const val PORT = 5555
        const val OTHER_PORT = 5037
    }
}
