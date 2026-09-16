package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * The follow-up advice shown alongside an [AdbCommand.Unsupported] result, so the user is pointed at
 * the part of the app that does the same job instead of being left with a bare failure.
 */
enum class UnsupportedHint {
    NONE,
    USE_FILE_BROWSER,
    USE_SIDELOAD_SCREEN,
    USE_PM_UNINSTALL,
    HOST_ONLY,
    INTERACTIVE_SHELL
}
