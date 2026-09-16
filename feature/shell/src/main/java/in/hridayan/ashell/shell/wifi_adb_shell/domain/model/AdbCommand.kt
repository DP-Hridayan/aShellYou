package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * A single line of user input classified into the ADB service it maps to.
 *
 * The app talks straight to the ADB daemon on the target device, so there is no ADB server in the
 * loop. Device services reach the daemon as a raw service string, while host services have no wire
 * equivalent and are emulated locally from what the app already knows about its own connection.
 */
sealed interface AdbCommand {

    /** Runs through the `shell:` service, keeping the working-directory emulation. */
    data class Shell(val command: String) : AdbCommand

    /** Runs through the `exec:` service, which has no pseudo terminal attached. */
    data class Exec(val command: String) : AdbCommand

    /**
     * A service the daemon implements natively, such as `tcpip:5555`.
     *
     * @property restartsDaemon whether the daemon is expected to restart and drop the connection.
     */
    data class DeviceService(
        val service: String,
        val restartsDaemon: Boolean
    ) : AdbCommand

    /** A command the ADB server would normally answer, emulated from local connection state. */
    sealed interface Host : AdbCommand {
        data class Devices(val verbose: Boolean) : Host
        data object GetSerialNo : Host
        data object GetState : Host
        data object GetDevPath : Host
        data class Connect(val ip: String, val port: Int) : Host
        data class Disconnect(val address: String?) : Host
        data class PairDevice(val ip: String, val port: Int, val code: String) : Host
        data object WaitForDevice : Host
        data object Version : Host
        data object Help : Host
        data object ServerNoOp : Host
    }

    /** A real ADB command that cannot work over this transport. */
    data class Unsupported(
        val subcommand: String,
        val hint: UnsupportedHint
    ) : AdbCommand

    /** A known command written incorrectly, answered with the correct form. */
    data class InvalidUsage(
        val subcommand: String,
        val usage: String
    ) : AdbCommand
}
