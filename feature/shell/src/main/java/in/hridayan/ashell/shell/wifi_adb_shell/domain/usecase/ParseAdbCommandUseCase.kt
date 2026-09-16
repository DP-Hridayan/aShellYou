package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.usecase

import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.AdbCommand
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.UnsupportedHint
import javax.inject.Inject

private const val ADB = "adb"
private const val NO_SUBCOMMAND = ""

private const val SUB_SHELL = "shell"
private const val SUB_EXEC_OUT = "exec-out"
private const val SUB_LOGCAT = "logcat"
private const val SUB_TCPIP = "tcpip"
private const val SUB_USB = "usb"
private const val SUB_REBOOT = "reboot"
private const val SUB_ROOT = "root"
private const val SUB_UNROOT = "unroot"
private const val SUB_REMOUNT = "remount"
private const val SUB_DEVICES = "devices"
private const val SUB_GET_SERIALNO = "get-serialno"
private const val SUB_GET_STATE = "get-state"
private const val SUB_GET_DEVPATH = "get-devpath"
private const val SUB_WAIT_FOR_DEVICE = "wait-for-device"
private const val SUB_CONNECT = "connect"
private const val SUB_DISCONNECT = "disconnect"
private const val SUB_PAIR = "pair"
private const val SUB_VERSION = "version"
private const val SUB_HELP = "help"
private const val SUB_START_SERVER = "start-server"
private const val SUB_KILL_SERVER = "kill-server"
private const val SUB_UNINSTALL = "uninstall"
private const val SUB_SIDELOAD = "sideload"

private const val SERVICE_TCPIP = "tcpip:"
private const val SERVICE_USB = "usb:"
private const val SERVICE_REBOOT = "reboot:"
private const val SERVICE_ROOT = "root:"
private const val SERVICE_UNROOT = "unroot:"
private const val SERVICE_REMOUNT = "remount:"

private const val VERBOSE_FLAG = "-l"
private const val ADDRESS_SEPARATOR = ':'
private const val DEFAULT_CONNECT_PORT = 5555
private const val MIN_PORT = 1
private const val MAX_PORT = 65535

private const val USAGE_TCPIP = "adb tcpip <port>"
private const val USAGE_CONNECT = "adb connect <host>[:port]"
private const val USAGE_PAIR = "adb pair <host>:<port> <code>"

private val OPTIONS_WITH_VALUE = setOf("-s", "-t", "-H", "-P", "-L")
private val OPTIONS_WITHOUT_VALUE = setOf("-d", "-e", "-a")

private val FILE_TRANSFER_SUBCOMMANDS = setOf("push", "pull", "sync")
private val INSTALL_SUBCOMMANDS =
    setOf("install", "install-multiple", "install-multi-package")
private val HOST_ONLY_SUBCOMMANDS = setOf(
    "forward", "reverse", "backup", "restore", "bugreport", "emu",
    "ppp", "jdwp", "mdns", "track-devices", "keygen", "reconnect"
)

/**
 * Classifies a line of user input into the ADB service it should reach.
 *
 * Input that does not begin with the `adb` token is returned unchanged as [AdbCommand.Shell], so
 * every command that worked before this parser existed keeps working exactly as it did.
 */
class ParseAdbCommandUseCase @Inject constructor() {

    operator fun invoke(rawInput: String): AdbCommand {
        val input = rawInput.trim()
        if (input.firstToken() != ADB) return AdbCommand.Shell(input)

        val withoutOptions = input.afterFirstToken().dropGlobalOptions()
        val subcommand = withoutOptions.firstToken()
        val arguments = withoutOptions.afterFirstToken()

        return parseSubcommand(subcommand, arguments)
    }

    private fun parseSubcommand(subcommand: String, arguments: String): AdbCommand =
        parseShellFamily(subcommand, arguments)
            ?: parseDeviceService(subcommand, arguments)
            ?: parseHost(subcommand, arguments)
            ?: parseUnsupported(subcommand)

    private fun parseShellFamily(subcommand: String, arguments: String): AdbCommand? =
        when (subcommand) {
            SUB_SHELL ->
                if (arguments.isEmpty()) {
                    AdbCommand.Unsupported(SUB_SHELL, UnsupportedHint.INTERACTIVE_SHELL)
                } else {
                    AdbCommand.Shell(arguments)
                }

            SUB_EXEC_OUT ->
                if (arguments.isEmpty()) {
                    AdbCommand.Unsupported(SUB_EXEC_OUT, UnsupportedHint.INTERACTIVE_SHELL)
                } else {
                    AdbCommand.Exec(arguments)
                }

            SUB_LOGCAT -> AdbCommand.Shell(joinTokens(SUB_LOGCAT, arguments))

            else -> null
        }

    private fun joinTokens(subcommand: String, arguments: String): String =
        if (arguments.isEmpty()) subcommand else "$subcommand $arguments"

    private fun parseDeviceService(subcommand: String, arguments: String): AdbCommand? =
        when (subcommand) {
            SUB_TCPIP -> parseTcpIp(arguments)
            SUB_USB -> restartingService(SERVICE_USB)
            SUB_REBOOT -> restartingService(SERVICE_REBOOT + arguments)
            SUB_ROOT -> restartingService(SERVICE_ROOT)
            SUB_UNROOT -> restartingService(SERVICE_UNROOT)
            SUB_REMOUNT -> AdbCommand.DeviceService(
                service = SERVICE_REMOUNT + arguments,
                restartsDaemon = false
            )

            else -> null
        }

    private fun parseTcpIp(arguments: String): AdbCommand {
        val port = arguments.firstToken().toPortOrNull()
            ?: return AdbCommand.InvalidUsage(SUB_TCPIP, USAGE_TCPIP)
        return restartingService(SERVICE_TCPIP + port)
    }

    private fun restartingService(service: String): AdbCommand.DeviceService =
        AdbCommand.DeviceService(service = service, restartsDaemon = true)

    private fun parseHost(subcommand: String, arguments: String): AdbCommand? =
        when (subcommand) {
            SUB_DEVICES -> AdbCommand.Host.Devices(
                verbose = arguments.split(WHITESPACE).any { it == VERBOSE_FLAG }
            )

            SUB_GET_SERIALNO -> AdbCommand.Host.GetSerialNo
            SUB_GET_STATE -> AdbCommand.Host.GetState
            SUB_GET_DEVPATH -> AdbCommand.Host.GetDevPath
            SUB_WAIT_FOR_DEVICE -> AdbCommand.Host.WaitForDevice
            SUB_VERSION -> AdbCommand.Host.Version
            SUB_HELP, NO_SUBCOMMAND -> AdbCommand.Host.Help
            SUB_START_SERVER, SUB_KILL_SERVER -> AdbCommand.Host.ServerNoOp
            SUB_CONNECT -> parseConnect(arguments)
            SUB_DISCONNECT -> AdbCommand.Host.Disconnect(arguments.firstToken().ifEmpty { null })
            SUB_PAIR -> parsePair(arguments)
            else -> null
        }

    private fun parseConnect(arguments: String): AdbCommand {
        val address = arguments.firstToken().toAddressOrNull(DEFAULT_CONNECT_PORT)
            ?: return AdbCommand.InvalidUsage(SUB_CONNECT, USAGE_CONNECT)
        return AdbCommand.Host.Connect(address.first, address.second)
    }

    private fun parsePair(arguments: String): AdbCommand {
        val address = arguments.firstToken().toAddressOrNull(defaultPort = null)
            ?: return AdbCommand.InvalidUsage(SUB_PAIR, USAGE_PAIR)
        val code = arguments.afterFirstToken().firstToken()
        if (code.isEmpty()) return AdbCommand.InvalidUsage(SUB_PAIR, USAGE_PAIR)
        return AdbCommand.Host.PairDevice(address.first, address.second, code)
    }

    private fun parseUnsupported(subcommand: String): AdbCommand =
        AdbCommand.Unsupported(subcommand, hintFor(subcommand))

    private fun hintFor(subcommand: String): UnsupportedHint = when (subcommand) {
        in FILE_TRANSFER_SUBCOMMANDS -> UnsupportedHint.USE_FILE_BROWSER
        in INSTALL_SUBCOMMANDS -> UnsupportedHint.USE_FILE_BROWSER
        SUB_UNINSTALL -> UnsupportedHint.USE_PM_UNINSTALL
        SUB_SIDELOAD -> UnsupportedHint.USE_SIDELOAD_SCREEN
        in HOST_ONLY_SUBCOMMANDS -> UnsupportedHint.HOST_ONLY
        else -> UnsupportedHint.NONE
    }
}

private val WHITESPACE = "\\s+".toRegex()

private fun String.firstToken(): String = takeWhile { !it.isWhitespace() }

private fun String.afterFirstToken(): String =
    dropWhile { !it.isWhitespace() }.trimStart()

private tailrec fun String.dropGlobalOptions(): String {
    val token = firstToken()
    return when (token) {
        in OPTIONS_WITH_VALUE -> afterFirstToken().afterFirstToken().dropGlobalOptions()
        in OPTIONS_WITHOUT_VALUE -> afterFirstToken().dropGlobalOptions()
        else -> this
    }
}

private fun String.toPortOrNull(): Int? = toIntOrNull()?.takeIf { it in MIN_PORT..MAX_PORT }

private fun String.toAddressOrNull(defaultPort: Int?): Pair<String, Int>? {
    if (isEmpty()) return null
    val separatorIndex = lastIndexOf(ADDRESS_SEPARATOR)
    if (separatorIndex < 0) return defaultPort?.let { this to it }
    val host = take(separatorIndex)
    val port = substring(separatorIndex + 1).toPortOrNull()
    if (host.isEmpty() || port == null) return null
    return host to port
}
