package `in`.hridayan.ashell.logcat.presentation.viewmodel

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.matches
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import `in`.hridayan.ashell.logcat.service.LogcatService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_LOGS = 2000

@Stable
@HiltViewModel
class LogcatViewModel @Inject constructor(
    private val sessionHolder: LogcatSessionHolder,
    private val filterRepository: LogcatFilterRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // ── Service running state ──────────────────────────────────────────────
    // Sourced from the singleton SessionHolder so HomeScreen and LogcatScreen
    // both see the same value without any polling.
    val isRunning: StateFlow<Boolean> = sessionHolder.isRunning

    // ── Auto-scroll state (independent of service state) ──────────────────
    // false → user has scrolled manually; only the FAB resets this to true.
    private val _isAutoScrolling = MutableStateFlow(true)
    val isAutoScrolling: StateFlow<Boolean> = _isAutoScrolling.asStateFlow()

    // ── Displayed log list (filtered view, max 2000) ───────────────────────
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    // ── New-entry count while auto-scroll is paused ────────────────────────
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    // ── Active filter ──────────────────────────────────────────────────────
    private val _activeFilter = MutableStateFlow(LogFilter())
    val activeFilter: StateFlow<LogFilter> = _activeFilter.asStateFlow()

    // ── Saved filter profiles ──────────────────────────────────────────────
    val savedFilters: StateFlow<List<LogFilter>> = filterRepository.getSavedFilters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Expanded row IDs ───────────────────────────────────────────────────
    private val _expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    val expandedIds: StateFlow<Set<Long>> = _expandedIds.asStateFlow()

    // ── Search bar visibility ──────────────────────────────────────────────
    private val _searchVisible = MutableStateFlow(false)
    val searchVisible: StateFlow<Boolean> = _searchVisible.asStateFlow()

    // ── Initialization ─────────────────────────────────────────────────────
    init {
        // Restore logs from the singleton buffer (survives ViewModel recreation)
        restoreFromBuffer()
        // Subscribe to live entries from LogcatService
        viewModelScope.launch {
            sessionHolder.entries.collect { entry -> onLiveEntryReceived(entry) }
        }
    }

    // ── Public actions ─────────────────────────────────────────────────────

    /** Start the logcat process. Does NOT affect auto-scroll. */
    fun startLogcat() {
        LogcatService.start(context)
    }

    /** Stop the logcat process. Does NOT affect auto-scroll. */
    fun stopLogcat() {
        LogcatService.stop(context)
    }

    /**
     * Called when user manually scrolls the list.
     * Freezes the displayed list; new entries accumulate in rawBuffer silently.
     */
    fun pauseAutoScroll() {
        _isAutoScrolling.value = false
    }

    /**
     * Called ONLY by the "scroll to bottom" FAB.
     * Rebuilds displayed list from rawBuffer (re-filtered), re-enables auto-scroll.
     */
    fun resumeAutoScroll() {
        reapplyFilter(_activeFilter.value)
        _pendingCount.value = 0
        _isAutoScrolling.value = true
    }

    /**
     * Update active filter and immediately re-filter the rawBuffer so the
     * displayed list reflects the change without waiting for new entries.
     */
    fun updateFilter(filter: LogFilter) {
        _activeFilter.value = filter
        reapplyFilter(filter)
    }

    fun clearLogs() {
        sessionHolder.clearBuffer()
        _logs.value = emptyList()
        _pendingCount.value = 0
    }

    fun toggleExpanded(id: Long) {
        _expandedIds.update { ids -> if (id in ids) ids - id else ids + id }
    }

    fun saveCurrentFilter(name: String) {
        viewModelScope.launch {
            filterRepository.saveFilter(_activeFilter.value.copy(name = name))
        }
    }

    fun deleteFilter(id: String) {
        viewModelScope.launch { filterRepository.deleteFilter(id) }
    }

    fun toggleSearchVisible() {
        _searchVisible.update { !it }
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private fun restoreFromBuffer() {
        val filter = _activeFilter.value
        _logs.value = sessionHolder.rawBuffer.filter { filter.matches(it) }.takeLast(MAX_LOGS)
    }

    private fun onLiveEntryReceived(entry: LogEntry) {
        val filter = _activeFilter.value
        val matches = filter.matches(entry)
        if (_isAutoScrolling.value) {
            if (matches) {
                _logs.update { current ->
                    val updated = current.toMutableList()
                    if (updated.size >= MAX_LOGS) updated.removeFirst()
                    updated.add(entry)
                    updated
                }
            }
        } else {
            if (matches) _pendingCount.update { it + 1 }
        }
    }

    private fun reapplyFilter(filter: LogFilter) {
        _logs.value = sessionHolder.rawBuffer.filter { filter.matches(it) }.takeLast(MAX_LOGS)
    }
}
