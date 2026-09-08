@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.domain.model.LogcatWorkingMode
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.dialog.ShizukuUnavailableDialog
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.ToastUtils
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.presentation.components.AutoScrollingLogList
import `in`.hridayan.ashell.logcat.presentation.components.LogcatSecondaryToolbar
import `in`.hridayan.ashell.logcat.presentation.components.LogcatTopBar
import `in`.hridayan.ashell.logcat.presentation.components.OtherDeviceContent
import `in`.hridayan.ashell.logcat.presentation.components.bottomsheet.LogEntryDetailBottomSheet
import `in`.hridayan.ashell.logcat.presentation.components.bottomsheet.LogcatFilterBottomSheet
import `in`.hridayan.ashell.logcat.presentation.components.bottomsheet.LogcatModeBottomSheet
import `in`.hridayan.ashell.logcat.presentation.components.dialog.LogcatPermissionDialog
import `in`.hridayan.ashell.logcat.presentation.components.dialog.ReadLogsRestartDialog
import `in`.hridayan.ashell.logcat.presentation.components.dialog.RootUnavailableDialog
import `in`.hridayan.ashell.logcat.presentation.components.dialog.WirelessNotConnectedDialog
import `in`.hridayan.ashell.logcat.presentation.event.LogcatUiEvent
import `in`.hridayan.ashell.logcat.presentation.model.LogListActions
import `in`.hridayan.ashell.logcat.presentation.model.LogListUiState
import `in`.hridayan.ashell.logcat.presentation.model.LogcatTab
import `in`.hridayan.ashell.logcat.presentation.viewmodel.LogcatViewModel

@Composable
fun LogcatScreen(
    navController: NavController,
    viewModel: LogcatViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val settings = LocalSettings.current
    val thisDeviceState by viewModel.thisDeviceState.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val savedFilters by viewModel.savedFilters.collectAsStateWithLifecycle()
    val expandedIds by viewModel.expandedIds.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val preflightResult by viewModel.preflightResult.collectAsStateWithLifecycle()
    val preflightChecking by viewModel.preflightChecking.collectAsStateWithLifecycle()
    val shizukuGranted by viewModel.shizukuPermissionState.collectAsStateWithLifecycle()

    val logcatMode = settings[SettingsKeys.LogcatMode]
    val listState = rememberLazyListState()

    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showModeSheet by rememberSaveable { mutableStateOf(false) }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var detailEntry by remember { mutableStateOf<LogEntry?>(null) }

    val actions = remember(viewModel) {
        LogListActions(
            onPauseAutoScroll = { viewModel.pauseAutoScroll(LogcatTab.THIS_DEVICE) },
            onResumeAutoScroll = { viewModel.resumeAutoScroll(LogcatTab.THIS_DEVICE) },
            onToggleExpanded = { viewModel.toggleExpanded(it) },
            onLongClick = { detailEntry = it },
        )
    }

    LaunchedEffect(shizukuGranted) {
        if (shizukuGranted && logcatMode == LogcatWorkingMode.SHIZUKU && !isRunning) {
            viewModel.startLogcat()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event -> handleUiEvent(context, event) }
    }

    val tryStartLogcat: () -> Unit = { viewModel.checkAndStart(logcatMode) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LogcatTopBar(
                isRunning = isRunning,
                showModeAction = activeTab == LogcatTab.THIS_DEVICE,
                searchVisible = searchVisible,
                isPreflightChecking = preflightChecking,
                onSearchToggle = { searchVisible = !searchVisible },
                onPlayPause = {
                    if (isRunning) {
                        viewModel.stopLogcat()
                    } else {
                        tryStartLogcat()
                    }
                },
                onModeClick = { showModeSheet = true },
                onOpenFilter = { showFilterSheet = true },
                onClear = { viewModel.clearLogs() },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LogcatSecondaryToolbar(
                activeTab = activeTab,
                onTabSelected = { viewModel.switchTab(it) },
                searchVisible = searchVisible,
                searchQuery = activeFilter.searchQuery,
                onSearchQueryChange = { viewModel.updateFilter(activeFilter.copy(searchQuery = it)) },
            )

            when (activeTab) {
                LogcatTab.THIS_DEVICE -> ThisDeviceContent(
                    state = thisDeviceState,
                    expandedIds = expandedIds,
                    isRunning = isRunning,
                    listState = listState,
                    actions = actions,
                )

                LogcatTab.OTHER_DEVICE -> OtherDeviceContent(viewModel = viewModel)
            }
        }
    }

    when (preflightResult) {
        LogcatPreflightResult.NeedsReadLogs -> {
            LogcatPermissionDialog(
                grantCommand = viewModel.readLogsGrantCommand,
                onCopyCommand = { copyGrantCommand(context, viewModel.readLogsGrantCommand) },
                onGranted = { viewModel.confirmReadLogsGranted(logcatMode) },
                onDismiss = { viewModel.consumePreflight() },
            )
        }

        LogcatPreflightResult.NeedsRestartForReadLogs -> {
            ReadLogsRestartDialog(
                onRestart = { viewModel.restartApp() },
                onDismiss = { viewModel.consumePreflight() },
            )
        }

        LogcatPreflightResult.ShizukuUnavailable -> {
            ShizukuUnavailableDialog(
                onDismiss = { viewModel.consumePreflight() },
                onConfirm = { viewModel.consumePreflight() },
            )
        }

        LogcatPreflightResult.ShizukuPermissionDenied -> {
            LaunchedEffect(Unit) {
                viewModel.requestShizukuPermission()
                viewModel.consumePreflight()
            }
        }

        LogcatPreflightResult.RootUnavailable -> {
            RootUnavailableDialog(
                onDismiss = { viewModel.consumePreflight() },
            )
        }

        LogcatPreflightResult.WirelessNotConnected -> {
            WirelessNotConnectedDialog(
                onConnect = {
                    navController.navigate(NavRoutes.PairingOwnDeviceScreen)
                },
                onDismiss = { viewModel.consumePreflight() },
            )
        }

        else -> Unit
    }

    if (showFilterSheet) {
        LogcatFilterBottomSheet(
            activeFilter = activeFilter,
            savedFilters = savedFilters,
            onApply = { viewModel.updateFilter(it) },
            onSaveProfile = { viewModel.saveCurrentFilter(it) },
            onDeleteProfile = { viewModel.deleteFilter(it) },
            onDismiss = { showFilterSheet = false },
        )
    }

    if (showModeSheet) {
        LogcatModeBottomSheet(
            currentMode = logcatMode,
            onModeChanged = { newMode ->
                if (isRunning) viewModel.checkAndRestart(newMode)
            },
            onDismiss = { showModeSheet = false },
        )
    }

    detailEntry?.let { entry ->
        LogEntryDetailBottomSheet(
            entry = entry,
            onDismiss = { detailEntry = null },
        )
    }
}

private fun handleUiEvent(context: Context, event: LogcatUiEvent) {
    when (event) {
        LogcatUiEvent.PermissionStillMissing ->
            ToastUtils.makeToast(context, context.getString(R.string.permission_not_granted_yet))
    }
}

private fun copyGrantCommand(context: Context, command: String) {
    ClipboardUtils.copyToClipboard(command, context)
    ToastUtils.makeToast(context, context.getString(R.string.copied_to_clipboard))
}

@Composable
private fun ThisDeviceContent(
    state: LogListUiState,
    expandedIds: Set<Long>,
    isRunning: Boolean,
    listState: LazyListState,
    actions: LogListActions,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !isRunning && state.logs.isEmpty() -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.logcat_tap_play),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            state.logs.isEmpty() -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.logcat_no_logs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                AutoScrollingLogList(
                    logs = state.logs,
                    expandedIds = expandedIds,
                    listState = listState,
                    isAutoScrolling = state.isAutoScrolling,
                    actions = actions,
                )
            }
        }
    }
}
