@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.domain.model.LogcatWorkingMode
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.dialog.ShizukuUnavailableDialog
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.presentation.components.LogEntryRow
import `in`.hridayan.ashell.logcat.presentation.components.LogcatSecondaryToolbar
import `in`.hridayan.ashell.logcat.presentation.components.LogcatTopBar
import `in`.hridayan.ashell.logcat.presentation.components.OtherDeviceContent
import `in`.hridayan.ashell.logcat.presentation.components.bottomsheet.LogEntryDetailBottomSheet
import `in`.hridayan.ashell.logcat.presentation.components.bottomsheet.LogcatFilterBottomSheet
import `in`.hridayan.ashell.logcat.presentation.components.bottomsheet.LogcatModeBottomSheet
import `in`.hridayan.ashell.logcat.presentation.components.dialog.LogcatPermissionDialog
import `in`.hridayan.ashell.logcat.presentation.components.dialog.RootUnavailableDialog
import `in`.hridayan.ashell.logcat.presentation.components.dialog.WirelessNotConnectedDialog
import `in`.hridayan.ashell.logcat.presentation.viewmodel.LogcatViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun LogcatScreen(
    navController: NavController,
    viewModel: LogcatViewModel = hiltViewModel(),
) {
    val settings = LocalSettings.current
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val isAutoScrolling by viewModel.isAutoScrolling.collectAsStateWithLifecycle()
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

    // ── Auto-scroll: pause only on real user touch input ───────────────────
    // Position-based detection is unusable here: a freshly appended item is
    // laid out below the viewport for one frame before the auto-scroll runs,
    // which looks identical to "user scrolled away" and killed auto-scroll on
    // the very first log line. DragInteraction is emitted only for touch
    // drags, never for programmatic scrolls, so it cannot self-trigger.
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) viewModel.pauseAutoScroll()
        }
    }

    // When auto-scroll is ON, keep scrolling to the bottom as new items arrive.
    // Re-keying on isAutoScrolling makes the FAB jump to the bottom instantly,
    // because a fresh snapshotFlow always emits the current size first.
    LaunchedEffect(listState, isAutoScrolling) {
        if (!isAutoScrolling) return@LaunchedEffect
        snapshotFlow { logs.size }
            .distinctUntilChanged()
            .collect { size ->
                if (size > 0) listState.scrollToItem(size - 1)
            }
    }

    // Auto-start once Shizuku permission is granted (if we were waiting for it)
    LaunchedEffect(shizukuGranted) {
        if (shizukuGranted && logcatMode == LogcatWorkingMode.SHIZUKU && !isRunning) {
            viewModel.startLogcat()
        }
    }

    val tryStartLogcat: () -> Unit = { viewModel.checkAndStart(logcatMode) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LogcatTopBar(
                isRunning = isRunning,
                showModeAction = activeTab == 0,
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
                0 -> ThisDeviceContent(
                    logs = logs,
                    expandedIds = expandedIds,
                    isAutoScrolling = isAutoScrolling,
                    isRunning = isRunning,
                    listState = listState,
                    onToggleExpanded = { viewModel.toggleExpanded(it) },
                    onLongClick = { detailEntry = it },
                    onResumeAutoScroll = { viewModel.resumeAutoScroll() },
                )

                1 -> OtherDeviceContent(viewModel = viewModel)
            }
        }
    }

    when (preflightResult) {
        LogcatPreflightResult.NeedsReadLogs -> {
            LogcatPermissionDialog(
                onContinueAnyway = {
                    viewModel.consumePreflight()
                    viewModel.startLogcat()
                },
                onGranted = {
                    viewModel.consumePreflight()
                    viewModel.startLogcat()
                },
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
            // Shizuku shows its own system permission dialog — just request it.
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

        else -> { /* Ready or null — no dialog */
        }
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
            onModeChanged = {
                if (isRunning) {
                    viewModel.stopLogcat()
                    viewModel.startLogcat()
                }
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

// ── This Device content ───────────────────────────────────────────────────────

@Composable
private fun ThisDeviceContent(
    logs: List<LogEntry>,
    expandedIds: Set<Long>,
    isAutoScrolling: Boolean,
    isRunning: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onToggleExpanded: (Long) -> Unit,
    onLongClick: (LogEntry) -> Unit,
    onResumeAutoScroll: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !isRunning && logs.isEmpty() -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.logcat_tap_play),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            logs.isEmpty() -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.logcat_no_logs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
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
                            onClick = { onToggleExpanded(entry.id) },
                            onLongClick = { onLongClick(entry) },
                        )
                    }
                }
            }
        }

        // Scroll-to-bottom FAB — visible when auto-scroll is paused
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            visible = !isAutoScrolling,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            FloatingActionButton(
                onClick = onResumeAutoScroll,
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
}