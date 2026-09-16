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

/**
 * Owns the log list and auto-scroll flag of a single logcat tab, keeping the list
 * within the memory budget the user selected.
 */
class LogListStateStore(maxBytes: Long) {
    private val _state = MutableStateFlow(LogListUiState())
    val state: StateFlow<LogListUiState> = _state.asStateFlow()

    private val sizeOf: (LogEntry) -> Int = LogEntry::approximateSizeBytes

    private var limitBytes: Long = maxBytes
    private var log: CappedLog<LogEntry> = CappedLog()

    fun append(batch: List<LogEntry>) {
        log = log.append(batch, limitBytes, sizeOf)
        publishLogs()
    }

    fun replace(logs: List<LogEntry>) {
        log = cappedLogOf(logs, limitBytes, sizeOf)
        publishLogs()
    }

    fun updateLimit(maxBytes: Long) {
        if (maxBytes == limitBytes) return
        limitBytes = maxBytes
        log = log.trimmedTo(maxBytes, sizeOf)
        publishLogs()
    }

    fun pause() {
        _state.update { it.copy(isAutoScrolling = false) }
    }

    fun resume() {
        _state.update { it.copy(isAutoScrolling = true) }
    }

    fun reset() {
        log = CappedLog()
        _state.value = LogListUiState()
    }

    private fun publishLogs() {
        _state.update { it.copy(logs = log.items) }
    }
}
