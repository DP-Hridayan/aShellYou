package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * The `adb` commands this app can carry out over a direct daemon connection.
 *
 * Single source of truth for both the input suggestions and the `adb help` output, so the two can
 * never drift apart. Entries carry a concrete value wherever a sensible default exists, and a
 * placeholder only where the value is specific to the user's own network.
 */
object AdbCommandCatalog {

    val commands: List<String> = listOf(
        "adb devices",
        "adb devices -l",
        "adb get-serialno",
        "adb get-state",
        "adb tcpip 5555",
        "adb usb",
        "adb connect <host>:<port>",
        "adb disconnect",
        "adb pair <host>:<port> <code>",
        "adb reboot",
        "adb reboot bootloader",
        "adb reboot recovery",
        "adb reboot sideload",
        "adb root",
        "adb unroot",
        "adb remount",
        "adb logcat",
        "adb exec-out <command>",
        "adb wait-for-device",
        "adb version"
    )
}
