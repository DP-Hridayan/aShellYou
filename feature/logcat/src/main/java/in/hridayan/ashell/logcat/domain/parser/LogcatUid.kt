package `in`.hridayan.ashell.logcat.domain.parser

private const val UIDS_PER_USER = 100_000
private const val FIRST_APPLICATION_UID = 10_000
private val APP_USER_NAME = Regex("""u(\d+)_a(\d+)""")

/**
 * Android's fixed system UIDs whose names are at most five characters, from
 * `android_filesystem_config.h`. Only these can be printed by name; see [LogcatUid].
 */
private val SHORT_SYSTEM_UIDS: Map<String, String> = mapOf(
    "root" to "0",
    "radio" to "1001",
    "input" to "1004",
    "audio" to "1005",
    "log" to "1007",
    "mount" to "1009",
    "wifi" to "1010",
    "adb" to "1011",
    "media" to "1013",
    "dhcp" to "1014",
    "vpn" to "1016",
    "usb" to "1018",
    "drm" to "1019",
    "mdnsr" to "1020",
    "gps" to "1021",
    "mtp" to "1024",
    "nfc" to "1027",
    "clat" to "1029",
    "shell" to "2000",
    "cache" to "2001",
    "diag" to "2002",
    "lmkd" to "1069",
)

/**
 * Converts the UID column logcat prints with `-v uid` into a number.
 *
 * logcat prints the user name when it is at most five characters and the number otherwise
 * (`liblog/logprint.cpp`). App UIDs are named like `u0_a77`, so most print as numbers, but
 * `u0_a1` to `u0_a9` and a handful of system names do not. Unrecognised names, such as isolated
 * processes (`u0_i1`), are returned as printed; they never belong to a package.
 */
internal object LogcatUid {

    fun normalize(printed: String): String = when {
        printed.isEmpty() || printed.all(Char::isDigit) -> printed
        else -> SHORT_SYSTEM_UIDS[printed] ?: appUid(printed) ?: printed
    }

    private fun appUid(name: String): String? {
        val match = APP_USER_NAME.matchEntire(name) ?: return null
        val (user, appId) = match.destructured
        return (user.toInt() * UIDS_PER_USER + FIRST_APPLICATION_UID + appId.toInt()).toString()
    }
}
