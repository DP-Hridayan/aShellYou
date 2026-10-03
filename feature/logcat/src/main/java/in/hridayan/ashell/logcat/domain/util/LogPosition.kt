package `in`.hridayan.ashell.logcat.domain.util

import `in`.hridayan.ashell.logcat.domain.model.LogEntry

/**
 * Index of the entry with [id], or of the first entry newer than it when it is no longer in
 * the list, for example after eviction or filtering. Returns [List.size] when every entry is
 * older than [id].
 *
 * The list must be ordered by ascending [LogEntry.id], which holds for every list the logcat
 * buffers produce because ids are assigned in arrival order.
 */
internal fun List<LogEntry>.indexAtOrAfter(id: Long): Int {
    val found = binarySearchBy(id) { it.id }
    return if (found >= 0) found else -(found + 1)
}
