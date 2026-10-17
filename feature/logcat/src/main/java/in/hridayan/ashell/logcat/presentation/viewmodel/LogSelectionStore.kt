package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.util.indexAtOrAfter
import `in`.hridayan.ashell.logcat.presentation.model.LogSelectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Selection mode of a log list.
 *
 * The mode ends by itself when a toggle leaves nothing selected, but not after [deselectAll], so
 * the select-all toggle never throws the user out. Every list passed in must be ordered by
 * ascending [LogEntry.id], which the logcat buffers guarantee.
 */
class LogSelectionStore {
    private val _state = MutableStateFlow(LogSelectionState())
    val state: StateFlow<LogSelectionState> = _state.asStateFlow()

    fun start() {
        _state.value = LogSelectionState(isActive = true)
    }

    fun startWith(id: Long) {
        _state.value = LogSelectionState(isActive = true, selectedIds = setOf(id), anchorId = id)
    }

    fun toggle(id: Long) {
        _state.update { current ->
            val selected = if (id in current.selectedIds) current.selectedIds - id else current.selectedIds + id
            current.copy(isActive = selected.isNotEmpty(), selectedIds = selected, anchorId = id)
        }
    }

    /** Adds every row between the anchor and [id], inclusive; just toggles [id] without an anchor in [logs]. */
    fun extendTo(id: Long, logs: List<LogEntry>) {
        val range = rangeFromAnchor(id, logs) ?: return toggle(id)
        _state.update { it.copy(selectedIds = it.selectedIds + range, anchorId = id) }
    }

    fun selectAll(logs: List<LogEntry>) {
        _state.update { it.copy(selectedIds = logs.mapTo(mutableSetOf()) { entry -> entry.id }) }
    }

    fun deselectAll() {
        _state.update { it.copy(selectedIds = emptySet(), anchorId = null) }
    }

    fun exit() {
        _state.value = LogSelectionState()
    }

    /** Forgets rows evicted from the list, all of which are older than [oldestId]. */
    fun dropOlderThan(oldestId: Long) {
        _state.update { current ->
            if (current.selectedIds.none { it < oldestId }) return@update current
            current.copy(selectedIds = current.selectedIds.filterTo(mutableSetOf()) { it >= oldestId })
        }
    }

    /** The selected entries still in [logs], in list order. */
    fun selectedIn(logs: List<LogEntry>): List<LogEntry> {
        val selected = _state.value.selectedIds
        return logs.filter { it.id in selected }
    }

    private fun rangeFromAnchor(id: Long, logs: List<LogEntry>): Set<Long>? {
        val anchorIndex = _state.value.anchorId?.let { logs.exactIndexOf(it) } ?: return null
        val targetIndex = logs.exactIndexOf(id) ?: return null
        val bounds = minOf(anchorIndex, targetIndex)..maxOf(anchorIndex, targetIndex)
        return bounds.mapTo(mutableSetOf()) { logs[it].id }
    }

    private fun List<LogEntry>.exactIndexOf(id: Long): Int? =
        indexAtOrAfter(id).takeIf { it < size && this[it].id == id }
}
