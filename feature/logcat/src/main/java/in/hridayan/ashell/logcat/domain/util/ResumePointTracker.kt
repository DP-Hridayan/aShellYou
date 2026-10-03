package `in`.hridayan.ashell.logcat.domain.util

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.ResumePoint

/**
 * Follows a stream of entries and remembers where a restart should pick up.
 *
 * Not thread-safe: callers that record from one thread and read from another must guard it.
 */
class ResumePointTracker {
    private var timestamp: String? = null
    private val heldAtTimestamp = mutableListOf<LogEntry>()

    fun record(entry: LogEntry) {
        if (entry.timestamp != timestamp) {
            timestamp = entry.timestamp
            heldAtTimestamp.clear()
        }
        heldAtTimestamp += entry
    }

    fun current(): ResumePoint? = timestamp?.let { ResumePoint(it, heldAtTimestamp.toList()) }

    fun reset() {
        timestamp = null
        heldAtTimestamp.clear()
    }
}
