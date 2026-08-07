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
import `in`.hridayan.ashell.logcat.data.emitter.LogcatEmitterFactory
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.Ready
import `in`.hridayan.ashell.logcat.domain.model.matches
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import `in`.hridayan.ashell.logcat.domain.usecase.CheckLogcatPreflightUseCase
import `in`.hridayan.ashell.logcat.domain.usecase.ObserveLogsUseCase
import `in`.hridayan.ashell.logcat.service.LogcatService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_LOGS = 2000

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

    private val _preflightResult = MutableStateFlow<LogcatPreflightResult?>(null)
    val preflightResult: StateFlow<LogcatPreflightResult?> = _preflightResult.asStateFlow()

    private val _preflightChecking = MutableStateFlow(false)
    val preflightChecking: StateFlow<Boolean> = _preflightChecking.asStateFlow()

    fun consumePreflight() {
        _preflightResult.value = null
    }

    /**
     * Run the pre-flight check for [mode], then:
     * - If [Ready] → start the service immediately.
     * - Otherwise → expose the result so the UI can show the right dialog.
     */
    fun checkAndStart(mode: Int) {
        viewModelScope.launch {
            _preflightChecking.value = true
            val result = checkPreflight.check(mode)
            _preflightChecking.value = false
            if (result == Ready) {
                LogcatService.start(context)
            } else {
                _preflightResult.value = result
            }
        }
    }

    fun startLogcat() = LogcatService.start(context)
    fun stopLogcat() = LogcatService.stop(context)

    val shizukuPermissionState: StateFlow<Boolean> =
        shellRepository.shizukuPermissionState()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun requestShizukuPermission() = shellRepository.requestShizukuPermission()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    fun switchTab(tab: Int) {
        _activeTab.value = tab
    }

    private val _isAutoScrolling = MutableStateFlow(true)
    val isAutoScrolling: StateFlow<Boolean> = _isAutoScrolling.asStateFlow()

    fun pauseAutoScroll() {
        _isAutoScrolling.value = false
    }

    fun resumeAutoScroll() {
        _isAutoScrolling.value = true
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

    private val _otherLogs = MutableStateFlow<List<LogEntry>>(emptyList())
    val otherDeviceLogs: StateFlow<List<LogEntry>> = _otherLogs.asStateFlow()
    private var otherDeviceJob: Job? = null

    private val _isOtherAutoScrolling = MutableStateFlow(true)
    val isOtherAutoScrolling: StateFlow<Boolean> = _isOtherAutoScrolling.asStateFlow()

    fun pauseOtherAutoScroll() {
        _isOtherAutoScrolling.value = false
    }

    fun resumeOtherAutoScroll() {
        _isOtherAutoScrolling.value = true
    }

    fun startOtherDeviceLogs(emitter: LogcatEmitter) {
        otherDeviceJob?.cancel()
        _otherLogs.value = emptyList()
        otherDeviceJob = viewModelScope.launch {
            observeLogsUseCase(emitter).collect { entry ->
                _otherLogs.update { current ->
                    val updated = current.toMutableList()
                    if (updated.size >= MAX_LOGS) updated.removeAt(0)
                    updated.add(entry)
                    updated
                }
            }
        }
    }

    fun stopOtherDeviceLogs() {
        otherDeviceJob?.cancel()
        otherDeviceJob = null
        _otherLogs.value = emptyList()
    }

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

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
        viewModelScope.launch {
            sessionHolder.entries.collect { entry ->
                if (entry.id > lastRestoredId) onLiveEntryReceived(entry)
            }
        }
    }

    fun updateFilter(filter: LogFilter) {
        _activeFilter.value = filter
        reapplyFilter(filter)
    }

    fun clearLogs() {
        sessionHolder.clearBuffer()
        _logs.value = emptyList()
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

    private fun restoreFromBuffer(): Long {
        val filter = _activeFilter.value
        val restored = sessionHolder.rawBuffer
            .filter { filter.matches(it) }
            .takeLast(MAX_LOGS)
        _logs.value = restored
        return restored.lastOrNull()?.id ?: 0L
    }

    private fun onLiveEntryReceived(entry: LogEntry) {
        if (!_activeFilter.value.matches(entry)) return
        _logs.update { current ->
            val updated = current.toMutableList()
            if (updated.size >= MAX_LOGS) updated.removeAt(0)
            updated.add(entry)
            updated
        }
    }

    private fun reapplyFilter(filter: LogFilter) {
        val allBuffer = sessionHolder.rawBuffer
        val filtered = allBuffer.filter { filter.matches(it) }.takeLast(MAX_LOGS)
        _logs.value = filtered
        lastRestoredId = allBuffer.lastOrNull()?.id ?: lastRestoredId
    }

    override fun onCleared() {
        super.onCleared()
        otherDeviceJob?.cancel()
    }
}
