package `in`.hridayan.ashell.logcat.domain.parser

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel

/**
 * Parses `logcat -v threadtime` lines into [LogEntry], with or without the `-v uid` column.
 *
 * Without UID: `MM-DD HH:MM:SS.mmm  PID   TID  LEVEL TAG  : message`
 * With UID:    `MM-DD HH:MM:SS.mmm  UID:  PID   TID  LEVEL TAG  : message`
 *
 * The UID column is the only part of a line that identifies the app, so package filters depend
 * on it. Lines without it get an empty [LogEntry.uid].
 */
object LogcatParser {

    private val THREADTIME_REGEX = Regex(
        """^(?<timestamp>\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+)\s+(?:(?<uid>\w+):\s*)?""" +
            """(?<pid>\d+)\s+(?<tid>\d+)\s+(?<level>[VDIWEFS?])\s+(?<tag>.*?)\s*:\s*(?<message>.*)"""
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
        return LogEntry(
            id = id,
            timestamp = match.group("timestamp"),
            pid = match.group("pid"),
            tid = match.group("tid"),
            uid = LogcatUid.normalize(match.group("uid")),
            packageName = "",
            level = match.group("level").first().toLogLevel(),
            tag = match.group("tag").trim(),
            message = match.group("message"),
        )
    }

    private fun MatchResult.group(name: String): String = groups[name]?.value.orEmpty()
}
