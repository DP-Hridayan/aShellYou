package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.dialog.OtgDeviceWaitingDialog
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.presentation.components.bottomsheet.LogEntryDetailBottomSheet
import `in`.hridayan.ashell.logcat.presentation.model.LogListActions
import `in`.hridayan.ashell.logcat.presentation.model.LogcatTab
import `in`.hridayan.ashell.logcat.presentation.viewmodel.LogcatViewModel

/**
 * Content for the "Other Device" tab.
 */
@Composable
fun OtherDeviceContent(
    viewModel: LogcatViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = LocalNavController.current
    val otgState by viewModel.otgState.collectAsStateWithLifecycle()
    val isOtherDeviceConnected by viewModel.isOtherDeviceConnected.collectAsStateWithLifecycle()
    val state by viewModel.otherDeviceState.collectAsStateWithLifecycle()
    val expandedIds by viewModel.expandedIds.collectAsStateWithLifecycle()

    var showOtgDialog by rememberSaveable { mutableStateOf(false) }
    var detailEntry by remember { mutableStateOf<LogEntry?>(null) }
    val listState = rememberLazyListState()

    val actions = remember(viewModel) {
        LogListActions(
            onPauseAutoScroll = { viewModel.pauseAutoScroll(LogcatTab.OTHER_DEVICE) },
            onResumeAutoScroll = { viewModel.resumeAutoScroll(LogcatTab.OTHER_DEVICE) },
            onToggleExpanded = { viewModel.toggleExpanded(it) },
            onLongClick = { detailEntry = it },
        )
    }

    LaunchedEffect(isOtherDeviceConnected, otgState) {
        if (isOtherDeviceConnected) {
            val emitter = if (otgState is OtgState.Connected) {
                viewModel.otgEmitter()
            } else {
                viewModel.wifiAdbEmitter()
            }
            viewModel.startOtherDeviceLogs(emitter)
        } else {
            viewModel.stopOtherDeviceLogs()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !isOtherDeviceConnected -> {
                NotConnectedPanel(
                    onConnectViaWifiAdb = {
                        navController.navigate(NavRoutes.PairingOtherDeviceScreen)
                    },
                    onConnectViaOtg = { showOtgDialog = true },
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

    if (showOtgDialog) {
        OtgDeviceWaitingDialog(
            onDismiss = { showOtgDialog = false },
            onConfirm = { showOtgDialog = false },
        )
    }

    detailEntry?.let { entry ->
        LogEntryDetailBottomSheet(
            entry = entry,
            onDismiss = { detailEntry = null },
        )
    }
}
