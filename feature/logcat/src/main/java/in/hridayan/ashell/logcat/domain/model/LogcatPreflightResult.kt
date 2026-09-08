package `in`.hridayan.ashell.logcat.domain.model

sealed class LogcatPreflightResult {
    /** All prerequisites met — start the service. */
    object Ready : LogcatPreflightResult()

    /** Log access mode: READ_LOGS permission not granted. */
    object NeedsReadLogs : LogcatPreflightResult()

    /**
     * Log access mode: READ_LOGS is granted but this process was forked before the grant,
     * so logcat cannot see system logs until the app restarts.
     */
    object NeedsRestartForReadLogs : LogcatPreflightResult()

    /** Shizuku mode: Shizuku is not running / not installed. */
    object ShizukuUnavailable : LogcatPreflightResult()

    /** Shizuku mode: Shizuku is running but app permission not yet granted. */
    object ShizukuPermissionDenied : LogcatPreflightResult()

    /** Root mode: `su` is not available or exited non-zero. */
    object RootUnavailable : LogcatPreflightResult()

    /** Wireless mode: own device not connected via Wireless Debugging. */
    object WirelessNotConnected : LogcatPreflightResult()
}
