package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.util.CappedLog
import `in`.hridayan.ashell.logcat.domain.util.append
import `in`.hridayan.ashell.logcat.domain.util.approximateSizeBytes
import `in`.hridayan.ashell.logcat.domain.util.cappedLogOf
import `in`.hridayan.ashell.logcat.domain.util.trimmedTo
import `in`.hridayan.ashell.logcat.presentation.model.LogListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PAUSED_OVERFLOW_FACTOR = 2

/**
 * Owns the log list and auto-scroll flag of a single logcat tab, keeping the list
 * within the memory budget the user selected.
 *
 * While following is paused the oldest entries are retained past that budget, up to
 * [PAUSED_OVERFLOW_FACTOR] times it. Evicting from the head shifts every entry below it,
 * which moves the rows the reader is looking at; new entries arriving at the tail do not.
 * The overflow is discarded when following resumes.
 */
class LogListStateStore(maxBytes: Long) {
    private val _state = MutableStateFlow(LogListUiState())
    val state: StateFlow<LogListUiState> = _state.asStateFlow()

    private val sizeOf: (LogEntry) -> Int = LogEntry::approximateSizeBytes

    private var limitBytes: Long = maxBytes
    private var isFollowing: Boolean = true
    private var log: CappedLog<LogEntry> = CappedLog()

    fun append(batch: List<LogEntry>) {
        log = log.append(batch, retainedBytes(), sizeOf)
        publishLogs()
    }

    fun replace(logs: List<LogEntry>) {
        log = cappedLogOf(logs, retainedBytes(), sizeOf)
        publishLogs()
    }

    fun updateLimit(maxBytes: Long) {
        if (maxBytes == limitBytes) return
        limitBytes = maxBytes
        log = log.trimmedTo(retainedBytes(), sizeOf)
        publishLogs()
    }

    fun pause() {
        isFollowing = false
        _state.update { it.copy(isAutoScrolling = false) }
    }

    fun resume() {
        isFollowing = true
        log = log.trimmedTo(limitBytes, sizeOf)
        _state.update { it.copy(logs = log.items, isAutoScrolling = true) }
    }

    fun reset() {
        isFollowing = true
        log = CappedLog()
        _state.value = LogListUiState()
    }

    private fun retainedBytes(): Long =
        if (isFollowing) limitBytes else limitBytes * PAUSED_OVERFLOW_FACTOR

    private fun publishLogs() {
        _state.update { it.copy(logs = log.items) }
    }
}
