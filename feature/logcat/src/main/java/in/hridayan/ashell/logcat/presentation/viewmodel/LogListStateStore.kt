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
 * Owns the log list and auto-scroll flag of a single logcat tab, keeping the collected
 * entries within the memory budget the user selected.
 *
 * While following is paused the published list is held still. Entries keep being collected
 * into [live] and the newest are always retained, but nothing is published, because any
 * change to the list moves the rows the reader is looking at: entries evicted from the head
 * shift everything below them, and a burst such as the initial buffer dump evicts faster
 * than the list can stay anchored. Resuming publishes the newest entries in one step.
 */
class LogListStateStore(maxBytes: Long) {
    private val _state = MutableStateFlow(LogListUiState())
    val state: StateFlow<LogListUiState> = _state.asStateFlow()

    private val sizeOf: (LogEntry) -> Int = LogEntry::approximateSizeBytes

    private var limitBytes: Long = maxBytes
    private var isFollowing: Boolean = true
    private var live: CappedLog<LogEntry> = CappedLog()

    fun append(batch: List<LogEntry>) {
        live = live.append(batch, limitBytes, sizeOf)
        publishWhileFollowing()
    }

    fun replace(logs: List<LogEntry>) {
        live = cappedLogOf(logs, limitBytes, sizeOf)
        publishLogs()
    }

    fun updateLimit(maxBytes: Long) {
        if (maxBytes == limitBytes) return
        limitBytes = maxBytes
        live = live.trimmedTo(maxBytes, sizeOf)
        publishWhileFollowing()
    }

    fun pause() {
        isFollowing = false
        _state.update { it.copy(isAutoScrolling = false) }
    }

    fun resume() {
        isFollowing = true
        _state.update { it.copy(logs = live.items, isAutoScrolling = true) }
    }

    fun reset() {
        isFollowing = true
        live = CappedLog()
        _state.value = LogListUiState()
    }

    private fun publishWhileFollowing() {
        if (isFollowing) publishLogs()
    }

    private fun publishLogs() {
        _state.update { it.copy(logs = live.items) }
    }
}
