package `in`.hridayan.ashell.logcat.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.core.common.domain.repository.ShellRepository
import `in`.hridayan.ashell.core.utils.AppRestartUtils
import `in`.hridayan.ashell.logcat.data.emitter.LogcatEmitterFactory
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.NeedsReadLogs
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.Ready
import `in`.hridayan.ashell.logcat.domain.model.ReadLogsPermission
import `in`.hridayan.ashell.logcat.domain.model.matches
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import `in`.hridayan.ashell.logcat.domain.usecase.CheckLogcatPreflightUseCase
import `in`.hridayan.ashell.logcat.domain.usecase.ObserveLogsUseCase
import `in`.hridayan.ashell.logcat.domain.util.batchByTime
import `in`.hridayan.ashell.logcat.presentation.event.LogcatUiEvent
import `in`.hridayan.ashell.logcat.presentation.model.LogListUiState
import `in`.hridayan.ashell.logcat.presentation.model.LogcatTab
import `in`.hridayan.ashell.logcat.service.LogcatService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_LOGS = 2000
private const val LOG_BATCH_WINDOW_MS = 50L

@HiltViewModel
class LogcatViewModel @Inject constructor(
    private val sessionHolder: LogcatSessionHolder,
    private val filterRepository: LogcatFilterRepository,
    private val observeLogsUseCase: ObserveLogsUseCase,
    private val emitterFactory: LogcatEmitterFactory,
    private val checkPreflight: CheckLogcatPreflightUseCase,
    private val shellRepository: ShellRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val isRunning: StateFlow<Boolean> = sessionHolder.isRunning

    val readLogsGrantCommand: String = ReadLogsPermission.grantCommand(context.packageName)

    private val _preflightResult = MutableStateFlow<LogcatPreflightResult?>(null)
    val preflightResult: StateFlow<LogcatPreflightResult?> = _preflightResult.asStateFlow()

    private val _preflightChecking = MutableStateFlow(false)
    val preflightChecking: StateFlow<Boolean> = _preflightChecking.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LogcatUiEvent>()
    val uiEvent: SharedFlow<LogcatUiEvent> = _uiEvent.asSharedFlow()

    fun consumePreflight() {
        _preflightResult.value = null
    }

    /**
     * Run the pre-flight check for [mode], then start the service when [Ready]
     * or expose the result so the UI can show the matching dialog.
     */
    fun checkAndStart(mode: Int) {
        viewModelScope.launch { applyPreflight(runPreflight(mode)) { LogcatService.start(context) } }
    }

    /**
     * Applies a new [mode] to a running session. Swaps the emitter in place when
     * the new mode is [Ready]; otherwise stops the session and shows the dialog.
     */
    fun checkAndRestart(mode: Int) {
        viewModelScope.launch {
            val result = runPreflight(mode)
            if (result != Ready) LogcatService.stop(context)
            applyPreflight(result) { LogcatService.restart(context) }
        }
    }

    /**
     * Re-runs the pre-flight after the user claims READ_LOGS was granted.
     * Keeps the permission dialog open and notifies when it is still missing.
     */
    fun confirmReadLogsGranted(mode: Int) {
        viewModelScope.launch {
            val result = runPreflight(mode)
            if (result == NeedsReadLogs) {
                _uiEvent.emit(LogcatUiEvent.PermissionStillMissing)
            } else {
                applyPreflight(result) { LogcatService.start(context) }
            }
        }
    }

    private suspend fun runPreflight(mode: Int): LogcatPreflightResult {
        _preflightChecking.value = true
        val result = checkPreflight.check(mode)
        _preflightChecking.value = false
        return result
    }

    private fun applyPreflight(result: LogcatPreflightResult, launch: () -> Unit) {
        if (result == Ready) {
            _preflightResult.value = null
            launch()
        } else {
            _preflightResult.value = result
        }
    }

    fun startLogcat() = LogcatService.start(context)
    fun stopLogcat() = LogcatService.stop(context)

    fun restartApp() {
        LogcatService.stop(context)
        AppRestartUtils.restart(context)
    }

    val shizukuPermissionState: StateFlow<Boolean> =
        shellRepository.shizukuPermissionState()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun requestShizukuPermission() = shellRepository.requestShizukuPermission()

    private val _activeTab = MutableStateFlow(LogcatTab.THIS_DEVICE)
    val activeTab: StateFlow<LogcatTab> = _activeTab.asStateFlow()

    fun switchTab(tab: LogcatTab) {
        _activeTab.value = tab
        storeFor(tab).resume()
    }

    private val thisDevice = LogListStateStore(MAX_LOGS)
    private val otherDevice = LogListStateStore(MAX_LOGS)

    val thisDeviceState: StateFlow<LogListUiState> = thisDevice.state
    val otherDeviceState: StateFlow<LogListUiState> = otherDevice.state

    fun pauseAutoScroll(tab: LogcatTab) = storeFor(tab).pause()

    fun resumeAutoScroll(tab: LogcatTab) = storeFor(tab).resume()

    private fun storeFor(tab: LogcatTab): LogListStateStore = when (tab) {
        LogcatTab.THIS_DEVICE -> thisDevice
        LogcatTab.OTHER_DEVICE -> otherDevice
    }

    val otgState: StateFlow<OtgState> = OtgConnection.state
    val wifiAdbState: StateFlow<WifiAdbState> = WifiAdbConnection.state

    val isOtherDeviceConnected: StateFlow<Boolean> = combine(
        OtgConnection.state,
        WifiAdbConnection.state,
        WifiAdbConnection.currentDevice,
    ) { otg, wifi, device ->
        otg is OtgState.Connected ||
                (wifi is WifiAdbState.Connected && device?.isOwnDevice == false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var otherDeviceJob: Job? = null

    fun startOtherDeviceLogs(emitter: LogcatEmitter) {
        otherDeviceJob?.cancel()
        otherDevice.reset()
        otherDeviceJob = viewModelScope.launch {
            observeLogsUseCase(emitter)
                .batchByTime(LOG_BATCH_WINDOW_MS)
                .collect { batch -> otherDevice.append(batch) }
        }
    }

    fun stopOtherDeviceLogs() {
        otherDeviceJob?.cancel()
        otherDeviceJob = null
        otherDevice.reset()
    }

    private val _activeFilter = MutableStateFlow(LogFilter())
    val activeFilter: StateFlow<LogFilter> = _activeFilter.asStateFlow()

    val savedFilters: StateFlow<List<LogFilter>> = filterRepository.getSavedFilters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    val expandedIds: StateFlow<Set<Long>> = _expandedIds.asStateFlow()

    @Volatile
    private var lastRestoredId: Long = 0L

    init {
        lastRestoredId = restoreFromBuffer()
        observeLiveEntries()
        resetAutoScrollOnStart()
    }

    fun updateFilter(filter: LogFilter) {
        _activeFilter.value = filter
        reapplyFilter(filter)
    }

    fun clearLogs() {
        sessionHolder.clearBuffer()
        thisDevice.reset()
    }

    fun toggleExpanded(id: Long) {
        _expandedIds.update { ids -> if (id in ids) ids - id else ids + id }
    }

    fun saveCurrentFilter(name: String) {
        viewModelScope.launch { filterRepository.saveFilter(_activeFilter.value.copy(name = name)) }
    }

    fun deleteFilter(id: String) {
        viewModelScope.launch { filterRepository.deleteFilter(id) }
    }

    fun otgEmitter(): LogcatEmitter = emitterFactory.otg
    fun wifiAdbEmitter(): LogcatEmitter = emitterFactory.wifiAdb

    private fun observeLiveEntries() {
        viewModelScope.launch {
            sessionHolder.entries
                .filter { it.id > lastRestoredId }
                .batchByTime(LOG_BATCH_WINDOW_MS)
                .collect { batch -> onLiveBatchReceived(batch) }
        }
    }

    private fun resetAutoScrollOnStart() {
        viewModelScope.launch {
            isRunning.filter { it }.collect { thisDevice.resume() }
        }
    }

    private fun restoreFromBuffer(): Long {
        val filter = _activeFilter.value
        val restored = sessionHolder.rawBuffer
            .filter { filter.matches(it) }
            .takeLast(MAX_LOGS)
        thisDevice.replace(restored)
        return restored.lastOrNull()?.id ?: 0L
    }

    private fun onLiveBatchReceived(batch: List<LogEntry>) {
        val filter = _activeFilter.value
        val fresh = batch.filter { it.id > lastRestoredId && filter.matches(it) }
        if (fresh.isNotEmpty()) thisDevice.append(fresh)
    }

    private fun reapplyFilter(filter: LogFilter) {
        val allBuffer = sessionHolder.rawBuffer
        val filtered = allBuffer.filter { filter.matches(it) }.takeLast(MAX_LOGS)
        thisDevice.replace(filtered)
        lastRestoredId = allBuffer.lastOrNull()?.id ?: lastRestoredId
    }

    override fun onCleared() {
        super.onCleared()
        otherDeviceJob?.cancel()
    }
}
