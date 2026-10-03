package `in`.hridayan.ashell.logcat.domain.util

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.ResumePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Drops the entries a stream restarted from [resumePoint] sends again because they are already
 * held. Each held entry is dropped at most once, so a line genuinely logged twice in the same
 * millisecond keeps its second copy. Re-sent entries carry fresh ids, so they are matched on
 * everything except the id.
 */
internal fun Flow<LogEntry>.dropResumeOverlap(resumePoint: ResumePoint?): Flow<LogEntry> {
    if (resumePoint == null) return this
    return flow {
        val pending = resumePoint.heldAtTimestamp.mapTo(mutableListOf()) { it.withoutId() }
        collect { entry ->
            val atBoundary = entry.timestamp == resumePoint.timestamp
            if (!atBoundary) pending.clear()
            val isResent = atBoundary && pending.remove(entry.withoutId())
            if (!isResent) emit(entry)
        }
    }
}

private fun LogEntry.withoutId(): LogEntry = copy(id = 0L)
