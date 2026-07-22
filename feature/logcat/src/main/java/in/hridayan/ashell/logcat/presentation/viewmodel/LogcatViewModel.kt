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

/**
 * Logcat viewer ViewModel.
 *
 * ## Key design principles
 *
 * 1. **Logs always appear in the list** regardless of auto-scroll state.
 *    The user sees new lines pile up whether or not auto-scroll is on.
 *    Only the PAUSE button (which stops the service) prevents new entries.
 *
 * 2. **Auto-scroll is purely a scroll-position concern.**
 *    ON  → LazyColumn follows the bottom automatically.
 *    OFF → list stays where the user left it; new entries appear below.
 *    Only the FAB re-enables auto-scroll.
 *
 * 3. **No pending buffer.** Every matching entry goes into [_logs] immediately.
 *    [rawBuffer] in the singleton [LogcatSessionHolder] is only used for
 *    restoring state after ViewModel recreation (back-navigation).
 *
 * 4. **Duplicate-key prevention.** After restoring from [rawBuffer], we track
 *    [lastRestoredId] and skip any SharedFlow entries with id ≤ that value.
 *    Since IDs are monotonically increasing and [rawBuffer] is written before
 *    the SharedFlow emit, this perfectly deduplicates.
 */
@Stable
@HiltViewModel
class LogcatViewModel @Inject constructor(
    private val sessionHolder: LogcatSessionHolder,
    private val filterRepository: LogcatFilterRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // ── Service running state (from singleton — shared with HomeScreen) ────
    val isRunning: StateFlow<Boolean> = sessionHolder.isRunning

    // ── Auto-scroll ───────────────────────────────────────────────────────
    // Only controls whether the UI scrolls to the bottom. Does NOT gate
    // whether entries are added to the list.
    private val _isAutoScrolling = MutableStateFlow(true)
    val isAutoScrolling: StateFlow<Boolean> = _isAutoScrolling.asStateFlow()

    // ── Displayed log list (always receives new entries, max 2000) ─────────
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    // ── Active filter ─────────────────────────────────────────────────────
    private val _activeFilter = MutableStateFlow(LogFilter())
    val activeFilter: StateFlow<LogFilter> = _activeFilter.asStateFlow()

    // ── Saved filter profiles ─────────────────────────────────────────────
    val savedFilters: StateFlow<List<LogFilter>> = filterRepository.getSavedFilters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Expanded row IDs ──────────────────────────────────────────────────
    private val _expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    val expandedIds: StateFlow<Set<Long>> = _expandedIds.asStateFlow()

    // ── Search bar visibility ─────────────────────────────────────────────
    private val _searchVisible = MutableStateFlow(false)
    val searchVisible: StateFlow<Boolean> = _searchVisible.asStateFlow()

    // ── Deduplication: highest ID loaded from rawBuffer ────────────────────
    // SharedFlow entries with id ≤ this value are skipped because they're
    // already in _logs from the rawBuffer restore/reapply.
    @Volatile
    private var lastRestoredId: Long = 0L

    // ── Initialization ────────────────────────────────────────────────────
    init {
        lastRestoredId = restoreFromBuffer()
        viewModelScope.launch {
            sessionHolder.entries.collect { entry ->
                // Skip entries already present from rawBuffer restore
                if (entry.id > lastRestoredId) {
                    onLiveEntryReceived(entry)
                }
            }
        }
    }

    // ── Public actions ────────────────────────────────────────────────────

    fun startLogcat() {
        LogcatService.start(context)
    }

    fun stopLogcat() {
        LogcatService.stop(context)
    }

    /** Called when user touches / scrolls the list manually. */
    fun pauseAutoScroll() {
        _isAutoScrolling.value = false
    }

    /** Called ONLY by the "scroll to bottom" FAB. */
    fun resumeAutoScroll() {
        _isAutoScrolling.value = true
    }

    /** Update filter and immediately re-filter the raw buffer. */
    fun updateFilter(filter: LogFilter) {
        _activeFilter.value = filter
        reapplyFilter(filter)
    }

    fun clearLogs() {
        sessionHolder.clearBuffer()
        _logs.value = emptyList()
        // Don't reset lastRestoredId — new entries from SharedFlow will
        // have higher IDs and will be added normally.
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

    // ── Internal ──────────────────────────────────────────────────────────

    /**
     * Restore logs from the singleton rawBuffer (survives ViewModel recreation).
     * @return the highest entry ID in the restored list (for deduplication).
     */
    private fun restoreFromBuffer(): Long {
        val filter = _activeFilter.value
        val restored = sessionHolder.rawBuffer
            .filter { filter.matches(it) }
            .takeLast(MAX_LOGS)
        _logs.value = restored
        return restored.lastOrNull()?.id ?: 0L
    }

    /**
     * Called for every new live entry from the service.
     * Entries are ALWAYS added to [_logs] regardless of auto-scroll state.
     */
    private fun onLiveEntryReceived(entry: LogEntry) {
        val filter = _activeFilter.value
        if (!filter.matches(entry)) return

        _logs.update { current ->
            val updated = current.toMutableList()
            if (updated.size >= MAX_LOGS) updated.removeAt(0)
            updated.add(entry)
            updated
        }
    }

    /**
     * Re-filter the entire rawBuffer when the filter changes.
     * Also updates [lastRestoredId] to prevent SharedFlow duplicates.
     */
    private fun reapplyFilter(filter: LogFilter) {
        // Use the highest ID in the entire raw buffer for deduplication,
        // not just the filtered subset, because unfiltered entries in
        // the SharedFlow buffer would also be skipped by the filter check
        // in onLiveEntryReceived anyway.
        val allBuffer = sessionHolder.rawBuffer
        val filtered = allBuffer.filter { filter.matches(it) }.takeLast(MAX_LOGS)
        _logs.value = filtered
        lastRestoredId = allBuffer.lastOrNull()?.id ?: lastRestoredId
    }
}
