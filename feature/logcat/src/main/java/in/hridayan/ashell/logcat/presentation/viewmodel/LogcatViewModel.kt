package `in`.hridayan.ashell.logcat.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.LogcatBufferSize
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.domain.repository.ShellRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.utils.AppRestartUtils
import `in`.hridayan.ashell.logcat.data.emitter.LogcatEmitterFactory
import `in`.hridayan.ashell.logcat.data.packages.PackageUidResolvers
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.NeedsReadLogs
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.Ready
import `in`.hridayan.ashell.logcat.domain.model.ReadLogsPermission
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import `in`.hridayan.ashell.logcat.domain.usecase.CheckLogcatPreflightUseCase
import `in`.hridayan.ashell.logcat.domain.usecase.ObserveLogsUseCase
import `in`.hridayan.ashell.logcat.domain.util.batchByTime
import `in`.hridayan.ashell.logcat.presentation.event.LogcatUiEvent
import `in`.hridayan.ashell.logcat.presentation.model.LogListUiState
import `in`.hridayan.ashell.logcat.presentation.model.LogcatTab
import `in`.hridayan.ashell.logcat.service.LogcatService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOG_BATCH_WINDOW_MS = 50L

private fun defaultBufferBytes(): Long = LogcatBufferSize.toBytes(LogcatBufferSize.DEFAULT)

@HiltViewModel
class LogcatViewModel @Inject constructor(
    private val sessionHolder: LogcatSessionHolder,
    private val filterRepository: LogcatFilterRepository,
    private val observeLogsUseCase: ObserveLogsUseCase,
    private val emitterFactory: LogcatEmitterFactory,
    private val checkPreflight: CheckLogcatPreflightUseCase,
    private val shellRepository: ShellRepository,
    private val settingsRepository: SettingsRepository,
    private val packageResolvers: PackageUidResolvers,
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
            thisDevice.resume()
            launch()
        } else {
            _preflightResult.value = result
        }
    }

    fun startLogcat() {
        thisDevice.resume()
        LogcatService.start(context)
    }

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
    }

    private val thisDevice = LogListStateStore(defaultBufferBytes())
    private val otherDevice = OtherDeviceSession(
        scope = viewModelScope,
        observeLogs = { emitter, resumeFrom -> observeLogsUseCase(emitter, resumeFrom) },
        currentCriteria = { otherDeviceCriteria.value },
        batchWindowMs = LOG_BATCH_WINDOW_MS,
        maxBytes = defaultBufferBytes(),
    )

    val thisDeviceState: StateFlow<LogListUiState> = thisDevice.state
    val otherDeviceState: StateFlow<LogListUiState> = otherDevice.state
    val isOtherDeviceRunning: StateFlow<Boolean> = otherDevice.isRunning

    fun pauseAutoScroll(tab: LogcatTab) = when (tab) {
        LogcatTab.THIS_DEVICE -> thisDevice.pause()
        LogcatTab.OTHER_DEVICE -> otherDevice.pauseAutoScroll()
    }

    fun resumeAutoScroll(tab: LogcatTab) = when (tab) {
        LogcatTab.THIS_DEVICE -> thisDevice.resume()
        LogcatTab.OTHER_DEVICE -> otherDevice.resumeAutoScroll()
    }

    fun toggleOtherDeviceLogs() {
        if (otherDevice.isRunning.value) otherDevice.stop() else otherDevice.play()
    }

    private val otherDeviceTarget: StateFlow<OtherDeviceTarget?> =
        otherDeviceTargets(emitterFactory, packageResolvers)
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isOtherDeviceConnected: StateFlow<Boolean> = otherDeviceTarget
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val filters = LogFilterSelection(filterRepository, viewModelScope)
    val filterProfiles: StateFlow<List<LogFilter>> = filters.profiles
    val activeProfileIds: StateFlow<Set<String>> = filters.activeProfileIds
    val searchQuery: StateFlow<String> = filters.searchQuery
    val chosenProfileIds: StateFlow<Set<String>> = filters.chosenProfileIds

    fun search(query: String) = filters.search(query)

    fun onProfileTap(profileId: String) = filters.tap(profileId)

    fun onProfileLongPress(profileId: String) = filters.longPress(profileId)

    fun clearChosenProfiles() = filters.clearChosen()

    fun deleteChosenProfiles() = filters.deleteChosen()

    private val thisDeviceCriteria = filters.criteriaFor(flowOf(packageResolvers.local))
    private val otherDeviceCriteria = filters.criteriaFor(otherDeviceTarget.map { it?.packages })

    /** Packages installed under [uid] on the device behind [tab], for the log detail sheet. */
    suspend fun packagesOf(uid: String, tab: LogcatTab): List<String> {
        if (uid.isBlank()) return emptyList()
        val resolver = when (tab) {
            LogcatTab.THIS_DEVICE -> packageResolvers.local
            LogcatTab.OTHER_DEVICE -> otherDeviceTarget.value?.packages
        }
        return resolver?.packagesOf(uid).orEmpty()
    }

    private val _expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    val expandedIds: StateFlow<Set<Long>> = _expandedIds.asStateFlow()

    @Volatile
    private var lastRestoredId: Long = 0L

    init {
        lastRestoredId = restoreFromBuffer()
        observeLiveEntries()
        observeBufferLimit()
        observeOtherDeviceConnection()
        observeFilterCriteria()
    }

    private fun observeFilterCriteria() {
        viewModelScope.launch { thisDeviceCriteria.collect { reapplyFilter() } }
        viewModelScope.launch { otherDeviceCriteria.collect { otherDevice.reapplyFilter() } }
    }

    private fun observeOtherDeviceConnection() {
        viewModelScope.launch {
            otherDeviceTarget.collect { target ->
                if (target == null) otherDevice.disconnect() else otherDevice.connect(target.emitter)
            }
        }
    }

    private fun observeBufferLimit() {
        viewModelScope.launch {
            settingsRepository.getInt(SettingsKeys.LogcatBufferLimit)
                .collect { applyBufferLimit(it) }
        }
    }

    private fun applyBufferLimit(megabytes: Int) {
        sessionHolder.updateLimit(megabytes)
        val maxBytes = LogcatBufferSize.toBytes(megabytes)
        thisDevice.updateLimit(maxBytes)
        otherDevice.updateLimit(maxBytes)
    }

    fun clearLogs(tab: LogcatTab) {
        when (tab) {
            LogcatTab.THIS_DEVICE -> clearThisDevice()
            LogcatTab.OTHER_DEVICE -> otherDevice.clear()
        }
    }

    private fun clearThisDevice() {
        sessionHolder.clearBuffer()
        thisDevice.reset()
    }

    fun toggleExpanded(id: Long) {
        _expandedIds.update { ids -> if (id in ids) ids - id else ids + id }
    }

    private fun observeLiveEntries() {
        viewModelScope.launch {
            sessionHolder.entries
                .filter { it.id > lastRestoredId }
                .batchByTime(LOG_BATCH_WINDOW_MS)
                .collect { batch -> onLiveBatchReceived(batch) }
        }
    }

    private fun restoreFromBuffer(): Long {
        val criteria = thisDeviceCriteria.value
        val restored = sessionHolder.rawBuffer.filter { criteria.matches(it) }
        thisDevice.replace(restored)
        return restored.lastOrNull()?.id ?: 0L
    }

    private fun onLiveBatchReceived(batch: List<LogEntry>) {
        val criteria = thisDeviceCriteria.value
        val fresh = batch.filter { it.id > lastRestoredId && criteria.matches(it) }
        if (fresh.isNotEmpty()) thisDevice.append(fresh)
    }

    private fun reapplyFilter() {
        val criteria = thisDeviceCriteria.value
        val allBuffer = sessionHolder.rawBuffer
        val filtered = allBuffer.filter { criteria.matches(it) }
        thisDevice.replace(filtered)
        lastRestoredId = allBuffer.lastOrNull()?.id ?: lastRestoredId
    }
}
