package `in`.hridayan.ashell.logcat.domain.model

sealed class LogcatPreflightResult {
    /** All prerequisites met — start the service. */
    object Ready : LogcatPreflightResult()

    /** Basic mode: READ_LOGS permission not granted. */
    object NeedsReadLogs : LogcatPreflightResult()

    /** Shizuku mode: Shizuku is not running / not installed. */
    object ShizukuUnavailable : LogcatPreflightResult()

    /** Shizuku mode: Shizuku is running but app permission not yet granted. */
    object ShizukuPermissionDenied : LogcatPreflightResult()

    /** Root mode: `su` is not available or exited non-zero. */
    object RootUnavailable : LogcatPreflightResult()

    /** Wireless mode: own device not connected via Wireless Debugging. */
    object WirelessNotConnected : LogcatPreflightResult()
}
