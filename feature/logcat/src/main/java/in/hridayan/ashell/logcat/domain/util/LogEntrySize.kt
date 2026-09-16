package `in`.hridayan.ashell.logcat.domain.util

import `in`.hridayan.ashell.logcat.domain.model.LogEntry

private const val ENTRY_OVERHEAD_BYTES = 64
private const val STRING_OVERHEAD_BYTES = 40
private const val BYTES_PER_CHAR = 2

/**
 * Approximate heap cost of retaining this entry, used to keep the logcat buffer
 * inside the memory budget the user selected.
 */
internal fun LogEntry.approximateSizeBytes(): Int =
    ENTRY_OVERHEAD_BYTES +
        stringSizeBytes(timestamp) +
        stringSizeBytes(pid) +
        stringSizeBytes(tid) +
        stringSizeBytes(uid) +
        stringSizeBytes(packageName) +
        stringSizeBytes(tag) +
        stringSizeBytes(message)

private fun stringSizeBytes(value: String): Int =
    STRING_OVERHEAD_BYTES + value.length * BYTES_PER_CHAR
