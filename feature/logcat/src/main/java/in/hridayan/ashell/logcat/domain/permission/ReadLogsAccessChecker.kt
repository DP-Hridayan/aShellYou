package `in`.hridayan.ashell.logcat.domain.permission

/**
 * Answers whether this process can read the full system log.
 */
interface ReadLogsAccessChecker {
    /** True when READ_LOGS is granted to this package; flips immediately after `pm grant`. */
    fun isPermissionGranted(): Boolean

    /** True when the running process already holds the log group; failure means the probe could not run. */
    fun hasLogGroup(): Result<Boolean>
}
