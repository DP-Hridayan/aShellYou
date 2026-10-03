package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * The outcome of asking the connected daemon to listen on a TCP port, as `adb tcpip <port>` does.
 */
sealed interface TcpIpEnableResult {

    /** The daemon restarted and the device now reports [port] as its ADB TCP port. */
    data class Opened(val port: Int) : TcpIpEnableResult

    /** There was no ADB connection to send the request over. */
    data object NotConnected : TcpIpEnableResult

    /** The daemon answered with something other than a restart, or the request could not be sent. */
    data class Refused(val message: String) : TcpIpEnableResult

    /**
     * The daemon accepted the request, but the port never appeared in the device properties. Either
     * it is still restarting, or the properties cannot be read on this device.
     */
    data object Unverified : TcpIpEnableResult
}

/**
 * The port a saved device should be reached on after the request, or null when the daemon kept its
 * old one. An accepted but unconfirmed restart still retires the old port, so the requested one is the
 * best remaining guess.
 */
fun TcpIpEnableResult.reachablePort(requestedPort: Int): Int? = when (this) {
    is TcpIpEnableResult.Opened -> port
    TcpIpEnableResult.Unverified -> requestedPort
    TcpIpEnableResult.NotConnected, is TcpIpEnableResult.Refused -> null
}
