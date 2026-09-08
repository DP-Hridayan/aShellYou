package `in`.hridayan.ashell.core.common.domain.model

/**
 * Working modes for the Logcat screen's "This Device" tab.
 *
 * Stored separately from [in.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode] so users can run logcat
 * in a different mode than the shell screen (e.g. Shizuku logcat + root shell).
 */
object LogcatWorkingMode {
    /** Plain app process; needs READ_LOGS granted once via ADB. */
    const val READ_LOGS = 0

    /** Shizuku; full system log, no extra permission. */
    const val SHIZUKU = 1

    /** Root `su`; full system log, no extra permission. */
    const val ROOT = 2

    /** Own device over Wireless Debugging; no extra permission. */
    const val WIRELESS = 3
}
