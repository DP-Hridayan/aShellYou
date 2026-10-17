package `in`.hridayan.ashell.logcat.domain.util

import `in`.hridayan.ashell.logcat.domain.model.LogEntry

private const val ID_COLUMN_WIDTH = 5
private const val COLUMN_SEPARATOR = " "
private const val LINE_SEPARATOR = "\n"

/**
 * The entry as `logcat -v threadtime` prints it, with the `-v uid` column when the entry has a
 * UID, so exported logs read like `adb logcat` output and parse back with the app's own parser.
 */
fun LogEntry.toLogcatLine(): String {
    val columns = listOfNotNull(
        timestamp,
        uid.takeIf { it.isNotBlank() }?.padStart(ID_COLUMN_WIDTH),
        pid.padStart(ID_COLUMN_WIDTH),
        tid.padStart(ID_COLUMN_WIDTH),
        level.tag.toString(),
        "$tag: $message",
    )
    return columns.joinToString(COLUMN_SEPARATOR)
}

/** The entries as logcat text, one [toLogcatLine] per line. */
fun List<LogEntry>.toLogcatText(): String = joinToString(LINE_SEPARATOR) { it.toLogcatLine() }
