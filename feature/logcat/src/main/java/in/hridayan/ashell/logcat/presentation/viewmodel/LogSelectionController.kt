package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.ExportTarget
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.repository.LogExportRepository
import `in`.hridayan.ashell.logcat.domain.usecase.BuildLogExportFileNameUseCase
import `in`.hridayan.ashell.logcat.domain.util.toLogcatText
import `in`.hridayan.ashell.logcat.presentation.event.LogcatUiEvent
import `in`.hridayan.ashell.logcat.presentation.model.ExportScope
import `in`.hridayan.ashell.logcat.presentation.model.LogSelectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Kept well under the ~1 MB binder limit the clipboard is bound by. */
private const val MAX_CLIPBOARD_CHARS = 500_000

/**
 * How the rows of the active tab respond to the user: expanding, selection mode, and copying,
 * saving or sharing either the selection or every visible row.
 *
 * Outside selection mode a tap expands a row and a long press starts selecting it, pausing
 * auto-scroll so the rows hold still. Inside it a tap toggles a row and a long press selects the
 * range up to it. Rows the buffer evicts leave the selection by themselves.
 *
 * Results reach the screen through [events]; a successful copy or export ends selection mode.
 * Saving takes two steps: [requestSave] asks the screen for a document, and [saveTo] writes the
 * rows of the scope requested once the user has picked one.
 */
class LogSelectionController(
    private val coroutineScope: CoroutineScope,
    private val repository: LogExportRepository,
    private val activeLogs: StateFlow<List<LogEntry>>,
    private val pauseAutoScroll: () -> Unit,
    private val events: MutableSharedFlow<LogcatUiEvent>,
    private val buildFileName: BuildLogExportFileNameUseCase = BuildLogExportFileNameUseCase(),
) {
    private val selection = LogSelectionStore()
    val state: StateFlow<LogSelectionState> = selection.state

    private val _expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    val expandedIds: StateFlow<Set<Long>> = _expandedIds.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private var pendingSaveScope: ExportScope = ExportScope.ALL

    init {
        coroutineScope.launch {
            activeLogs
                .mapNotNull { it.firstOrNull()?.id }
                .distinctUntilChanged()
                .collect { oldestId -> selection.dropOlderThan(oldestId) }
        }
    }

    fun onClick(entry: LogEntry) {
        if (state.value.isActive) return selection.toggle(entry.id)
        _expandedIds.update { ids -> if (entry.id in ids) ids - entry.id else ids + entry.id }
    }

    fun onLongClick(entry: LogEntry) {
        if (state.value.isActive) {
            selection.extendTo(entry.id, activeLogs.value)
        } else {
            pauseAutoScroll()
            selection.startWith(entry.id)
        }
    }

    fun start() {
        pauseAutoScroll()
        selection.start()
    }

    fun exit() = selection.exit()

    fun toggleSelectAll() {
        val logs = activeLogs.value
        if (selection.selectedIn(logs).size == logs.size) selection.deselectAll() else selection.selectAll(logs)
    }

    fun singleSelectedEntry(): LogEntry? = selection.selectedIn(activeLogs.value).singleOrNull()

    fun copySelection() {
        val entries = selection.selectedIn(activeLogs.value)
        if (entries.isEmpty()) return
        val text = entries.toLogcatText()
        val tooLarge = text.length > MAX_CLIPBOARD_CHARS
        if (!tooLarge) selection.exit()
        coroutineScope.launch {
            events.emit(if (tooLarge) LogcatUiEvent.CopyTooLarge else LogcatUiEvent.CopyLogs(text, entries.size))
        }
    }

    fun requestSave(scope: ExportScope) {
        if (entriesFor(scope).isEmpty()) return
        pendingSaveScope = scope
        coroutineScope.launch { events.emit(LogcatUiEvent.PickSaveLocation(buildFileName())) }
    }

    fun saveTo(uriString: String) = export(entriesFor(pendingSaveScope), ExportTarget.Document(uriString))

    fun share(scope: ExportScope) = export(entriesFor(scope), ExportTarget.ShareCache(buildFileName()))

    private fun export(entries: List<LogEntry>, target: ExportTarget) {
        if (entries.isEmpty() || _isExporting.value) return
        coroutineScope.launch {
            _isExporting.value = true
            val result = repository.writeTo(target, entries)
            _isExporting.value = false
            result
                .onSuccess { uri -> onExported(target, uri) }
                .onFailure { events.emit(LogcatUiEvent.ExportFailed) }
        }
    }

    private suspend fun onExported(target: ExportTarget, uriString: String) {
        selection.exit()
        val event = when (target) {
            is ExportTarget.Document -> LogcatUiEvent.LogsSaved
            is ExportTarget.ShareCache -> LogcatUiEvent.ShareLogs(uriString)
        }
        events.emit(event)
    }

    private fun entriesFor(scope: ExportScope): List<LogEntry> = when (scope) {
        ExportScope.SELECTION -> selection.selectedIn(activeLogs.value)
        ExportScope.ALL -> activeLogs.value
    }
}
