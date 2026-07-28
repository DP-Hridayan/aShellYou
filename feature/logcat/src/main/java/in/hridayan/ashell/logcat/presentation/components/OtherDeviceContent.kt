package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.presentation.viewmodel.LogcatViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Content for the "Other Device" tab.
 *
 * Auto-scroll uses position-based detection — no isProgrammaticScroll flag,
 * no race conditions when logs arrive fast (OTG/WiFi ADB).
 */
@Composable
fun OtherDeviceContent(
    viewModel: LogcatViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val otgState by viewModel.otgState.collectAsStateWithLifecycle()
    val isOtherDeviceConnected by viewModel.isOtherDeviceConnected.collectAsStateWithLifecycle()
    val logs by viewModel.otherDeviceLogs.collectAsStateWithLifecycle()
    val expandedIds by viewModel.expandedIds.collectAsStateWithLifecycle()
    val isAutoScrolling by viewModel.isOtherAutoScrolling.collectAsStateWithLifecycle()

    var showOtgDialog by rememberSaveable { mutableStateOf(false) }
    var detailEntry by remember { mutableStateOf<LogEntry?>(null) }
    val listState = rememberLazyListState()

    // Start/stop emitter on connection changes
    LaunchedEffect(isOtherDeviceConnected, otgState) {
        if (isOtherDeviceConnected) {
            val emitter = if (otgState is OtgState.Connected)
                viewModel.otgEmitter()
            else
                viewModel.wifiAdbEmitter()
            viewModel.startOtherDeviceLogs(emitter)
            viewModel.resumeOtherAutoScroll()
        } else {
            viewModel.stopOtherDeviceLogs()
        }
    }

    // Position-based auto-scroll pause: fires when user scrolls away from bottom
    LaunchedEffect(listState) {
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            total > 0 && lastVisible < total - 1
        }
            .distinctUntilChanged()
            .collect { scrolledAway ->
                if (scrolledAway) viewModel.pauseOtherAutoScroll()
            }
    }

    // Scroll to bottom when auto-scroll is on and new items arrive
    LaunchedEffect(isAutoScrolling) {
        if (!isAutoScrolling) return@LaunchedEffect
        snapshotFlow { logs.size }
            .distinctUntilChanged()
            .collect { size ->
                if (size > 0) listState.scrollToItem(size - 1)
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isOtherDeviceConnected -> {
                if (logs.isEmpty()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.logcat_no_logs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(bottom = 80.dp),
                    ) {
                        items(items = logs, key = { it.id }) { entry ->
                            LogEntryRow(
                                modifier = Modifier.fillMaxWidth(),
                                entry = entry,
                                isExpanded = entry.id in expandedIds,
                                onClick = { viewModel.toggleExpanded(entry.id) },
                                onLongClick = { detailEntry = entry },
                            )
                        }
                    }
                }
            }

            else -> {
                NotConnectedPanel(
                    onConnectViaWifiAdb = {
                        navController.navigate(NavRoutes.PairingOtherDeviceScreen)
                    },
                    onConnectViaOtg = { showOtgDialog = true },
                )
            }
        }

        // Scroll-to-bottom FAB
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            visible = isOtherDeviceConnected && !isAutoScrolling,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            FloatingActionButton(
                onClick = { viewModel.resumeOtherAutoScroll() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mobile_arrow_down),
                    contentDescription = stringResource(R.string.resume),
                )
            }
        }
    }

    if (showOtgDialog) {
        OtgWaitingDialogWrapper(
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
