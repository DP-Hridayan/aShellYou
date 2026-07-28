package `in`.hridayan.ashell.core.common.domain.model

/**
 * Working modes for the Logcat screen's "This Device" tab.
 *
 * Stored separately from [in.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode] so users can run logcat
 * in a different mode than the shell screen (e.g. Shizuku logcat + root shell).
 */
object LogcatWorkingMode {
    const val BASIC    = 0  // Basic shell — needs READ_LOGS permission
    const val SHIZUKU  = 1  // Shizuku — full system log, no permission needed
    const val ROOT     = 2  // Root su — full system log, no permission needed
    const val WIRELESS = 3  // Own device via Wireless Debugging — no permission needed
}
