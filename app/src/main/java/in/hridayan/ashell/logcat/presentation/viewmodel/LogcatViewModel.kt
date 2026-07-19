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
private const val MAX_PENDING = 500

@Stable
@HiltViewModel
class LogcatViewModel @Inject constructor(
    private val sessionHolder: LogcatSessionHolder,
    private val filterRepository: LogcatFilterRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // ── Running state ──────────────────────────────────────────────────────

    private val _isRunning = MutableStateFlow(LogcatService.isServiceRunning())
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // ── Visible log buffer (max 2000) ──────────────────────────────────────

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    // ── Background pending buffer (when auto-scroll is paused) ────────────

    private val pendingBuffer = ArrayDeque<LogEntry>()

    // ── Auto-scroll / play-pause ───────────────────────────────────────────

    private val _isAutoScrolling = MutableStateFlow(true)
    val isAutoScrolling: StateFlow<Boolean> = _isAutoScrolling.asStateFlow()

    // ── Active filter ──────────────────────────────────────────────────────

    private val _activeFilter = MutableStateFlow(LogFilter())
    val activeFilter: StateFlow<LogFilter> = _activeFilter.asStateFlow()

    // ── Saved filter profiles ──────────────────────────────────────────────

    val savedFilters: StateFlow<List<LogFilter>> = filterRepository.getSavedFilters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Expanded row IDs (tracked separately to prevent list-wide recomposition) ──

    private val _expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    val expandedIds: StateFlow<Set<Long>> = _expandedIds.asStateFlow()

    // ── Search visibility ──────────────────────────────────────────────────

    private val _searchVisible = MutableStateFlow(false)
    val searchVisible: StateFlow<Boolean> = _searchVisible.asStateFlow()

    init {
        // Subscribe to session holder — entries arrive from LogcatService
        viewModelScope.launch {
            sessionHolder.entries.collect { entry ->
                onEntryReceived(entry)
            }
        }
        // Sync isRunning with service
        viewModelScope.launch {
            while (true) {
                _isRunning.value = LogcatService.isServiceRunning()
                kotlinx.coroutines.delay(1_000)
            }
        }
    }

    // ── Public actions ─────────────────────────────────────────────────────

    fun startLogcat() {
        LogcatService.start(context)
        _isRunning.value = true
        _isAutoScrolling.value = true
    }

    fun stopLogcat() {
        LogcatService.stop(context)
        _isRunning.value = false
    }

    /**
     * Called by the UI when the user scrolls up.
     * Background buffering continues in [pendingBuffer].
     */
    fun pauseFromScroll() {
        _isAutoScrolling.value = false
    }

    /**
     * Called when the user taps the resume/play button.
     * Flushes [pendingBuffer] into [_logs] and re-enables auto-scroll.
     */
    fun resumeAndFlush() {
        val filter = _activeFilter.value
        val toFlush = pendingBuffer.toList().filter { filter.matches(it) }
        pendingBuffer.clear()
        _logs.update { current ->
            (current + toFlush).takeLast(MAX_LOGS)
        }
        _isAutoScrolling.value = true
    }

    fun clearLogs() {
        _logs.value = emptyList()
        pendingBuffer.clear()
    }

    fun toggleExpanded(id: Long) {
        _expandedIds.update { ids ->
            if (id in ids) ids - id else ids + id
        }
    }

    fun updateFilter(filter: LogFilter) {
        _activeFilter.value = filter
        // Re-apply filter is handled by the UI observing activeFilter changes
    }

    fun saveCurrentFilter(name: String) {
        viewModelScope.launch {
            filterRepository.saveFilter(_activeFilter.value.copy(name = name))
        }
    }

    fun deleteFilter(id: String) {
        viewModelScope.launch {
            filterRepository.deleteFilter(id)
        }
    }

    fun toggleSearchVisible() {
        _searchVisible.update { !it }
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private fun onEntryReceived(entry: LogEntry) {
        val filter = _activeFilter.value
        if (!filter.matches(entry)) return

        if (_isAutoScrolling.value) {
            _logs.update { current ->
                val updated = current.toMutableList()
                if (updated.size >= MAX_LOGS) updated.removeFirst()
                updated.add(entry)
                updated
            }
        } else {
            if (pendingBuffer.size >= MAX_PENDING) pendingBuffer.removeFirst()
            pendingBuffer.addLast(entry)
        }
    }
}
