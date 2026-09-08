package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.util.appendCapped
import `in`.hridayan.ashell.logcat.presentation.model.LogListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Owns the log list and auto-scroll flag of a single logcat tab.
 */
class LogListStateStore(private val maxLogs: Int) {
    private val _state = MutableStateFlow(LogListUiState())
    val state: StateFlow<LogListUiState> = _state.asStateFlow()

    fun append(batch: List<LogEntry>) {
        _state.update { it.copy(logs = it.logs.appendCapped(batch, maxLogs)) }
    }

    fun replace(logs: List<LogEntry>) {
        _state.update { it.copy(logs = logs.takeLast(maxLogs)) }
    }

    fun pause() {
        _state.update { it.copy(isAutoScrolling = false) }
    }

    fun resume() {
        _state.update { it.copy(isAutoScrolling = true) }
    }

    fun reset() {
        _state.value = LogListUiState()
    }
}
