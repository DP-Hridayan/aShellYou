package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository

/**
 * Asks the daemon on the current Wi-Fi ADB connection to listen on a TCP port, and reads back the
 * port the device reports.
 */
interface AdbTcpIpGateway {

    fun isConnected(): Boolean

    /**
     * Sends the `tcpip:` service request. The daemon restarts on success, which drops the
     * connection the request was sent over.
     */
    suspend fun requestTcpIp(port: Int): TcpIpRequestOutcome

    /** The ADB TCP port from the device properties, or null when none is set or it cannot be read. */
    fun currentTcpPort(): Int?
}

sealed interface TcpIpRequestOutcome {

    /** The daemon confirmed it is restarting, or gave no reply, which a restart also produces. */
    data object Accepted : TcpIpRequestOutcome

    data class Refused(val message: String) : TcpIpRequestOutcome
}
