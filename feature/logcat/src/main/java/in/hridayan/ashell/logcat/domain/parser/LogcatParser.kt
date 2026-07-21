package `in`.hridayan.ashell.logcat.domain.parser

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel

/**
 * Parses `logcat -v threadtime` format lines into [LogEntry].
 *
 * threadtime format:
 * `MM-DD HH:MM:SS.mmm  PID   TID  LEVEL TAG  : message`
 *
 * Extended threadtime (Android 9+, with UID) also supported.
 */
object LogcatParser {

    // Basic threadtime: date time pid tid level tag: message
    // e.g. 07-19 20:12:17.345  1234  5678 D MyTag  : Hello world
    private val THREADTIME_REGEX = Regex(
        """^(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+)\s+(\d+)\s+(\d+)\s+([VDIWEFS?])\s+(.*?)\s*:\s*(.*)"""
    )

    private fun Char.toLogLevel(): LogLevel = when (this) {
        'V' -> LogLevel.VERBOSE
        'D' -> LogLevel.DEBUG
        'I' -> LogLevel.INFO
        'W' -> LogLevel.WARNING
        'E' -> LogLevel.ERROR
        'F' -> LogLevel.FATAL
        'S' -> LogLevel.SILENT
        else -> LogLevel.UNKNOWN
    }

    fun parse(raw: String, id: Long): LogEntry? {
        val match = THREADTIME_REGEX.matchEntire(raw.trim()) ?: return null
        val (timestamp, pid, tid, levelChar, tag, message) = match.destructured
        return LogEntry(
            id = id,
            timestamp = timestamp,
            pid = pid.trim(),
            tid = tid.trim(),
            uid = "",
            packageName = "",
            level = levelChar.first().toLogLevel(),
            tag = tag.trim(),
            message = message,
        )
    }
}
