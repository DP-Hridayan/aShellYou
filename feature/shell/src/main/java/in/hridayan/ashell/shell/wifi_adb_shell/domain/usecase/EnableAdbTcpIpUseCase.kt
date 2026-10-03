package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.usecase

import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.TcpIpEnableResult
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.AdbTcpIpGateway
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.TcpIpRequestOutcome
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val MAX_PORT_READS = 20
private const val PORT_READ_INTERVAL_MS = 250L

/**
 * Does what `adb tcpip <port>` does, over the current Wi-Fi ADB connection, and confirms the result
 * from the device properties.
 *
 * The daemon restarts on success and drops the connection the request travelled over, so callers must
 * treat that connection as gone whatever the result.
 */
class EnableAdbTcpIpUseCase @Inject constructor(
    private val gateway: AdbTcpIpGateway
) {

    suspend operator fun invoke(port: Int = DEFAULT_ADB_TCP_PORT): TcpIpEnableResult {
        if (!gateway.isConnected()) return TcpIpEnableResult.NotConnected

        return when (val outcome = gateway.requestTcpIp(port)) {
            is TcpIpRequestOutcome.Refused -> TcpIpEnableResult.Refused(outcome.message)
            TcpIpRequestOutcome.Accepted -> awaitPort(port)
        }
    }

    /**
     * A restarting daemon takes a moment to publish its new port, so a single read right after the
     * request would usually miss it.
     */
    private suspend fun awaitPort(port: Int): TcpIpEnableResult {
        repeat(MAX_PORT_READS) { attempt ->
            if (gateway.currentTcpPort() == port) return TcpIpEnableResult.Opened(port)
            if (attempt < MAX_PORT_READS - 1) delay(PORT_READ_INTERVAL_MS)
        }
        return TcpIpEnableResult.Unverified
    }

    companion object {
        const val DEFAULT_ADB_TCP_PORT = 5555
    }
}
