@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.logcat.data.permission.LogcatPermissionHelper
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.presentation.components.LogEntryDetailBottomSheet
import `in`.hridayan.ashell.logcat.presentation.components.LogEntryRow
import `in`.hridayan.ashell.logcat.presentation.components.LogcatPermissionDialog
import `in`.hridayan.ashell.logcat.presentation.components.LogcatTopBar
import `in`.hridayan.ashell.logcat.presentation.components.filter.LogcatFilterBottomSheet
import `in`.hridayan.ashell.logcat.presentation.viewmodel.LogcatViewModel
import `in`.hridayan.ashell.logcat.service.LogcatService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@Composable
fun LogcatScreen(
    viewModel: LogcatViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val isAutoScrolling by viewModel.isAutoScrolling.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val savedFilters by viewModel.savedFilters.collectAsStateWithLifecycle()
    val expandedIds by viewModel.expandedIds.collectAsStateWithLifecycle()
    val searchVisible by viewModel.searchVisible.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var detailEntry by remember { mutableStateOf<LogEntry?>(null) }
    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

    // ── Permission + auto-start (only if service isn't already running) ───
    // Use the static flag directly to avoid a 1-second delay from the StateFlow poll.
    LaunchedEffect(Unit) {
        if (!LogcatService.isServiceRunning()) {
            if (LogcatPermissionHelper.hasReadLogsPermission(context)) {
                viewModel.startLogcat()
            } else {
                showPermissionDialog = true
            }
        }
    }

    // ── Auto-scroll to bottom when new logs arrive and auto-scroll is on ──
    LaunchedEffect(listState) {
        snapshotFlow { logs.size }
            .drop(1) // skip initial emission
            .distinctUntilChanged()
            .collectLatest {
                if (isAutoScrolling && logs.isNotEmpty()) {
                    listState.animateScrollToItem(logs.lastIndex)
                }
            }
    }

    // ── Detect user scroll gesture → pause auto-scroll ────────────────────
    // We watch for scroll events where the user is NOT at the bottom of the list.
    // Scrolling down toward the bottom does NOT pause (only upward / away from bottom).
    LaunchedEffect(listState) {
        var lastFirstVisible = 0
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                val movedUp = index < lastFirstVisible ||
                        (index == lastFirstVisible && offset < 0)
                // If user scrolled and we're not at the very end, pause auto-scroll
                if (listState.isScrollInProgress && isAutoScrolling) {
                    val atBottom = !listState.canScrollForward
                    if (!atBottom) viewModel.pauseAutoScroll()
                }
                lastFirstVisible = index
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LogcatTopBar(
                isRunning = isRunning,
                isAutoScrolling = isAutoScrolling,
                searchVisible = searchVisible,
                activeFilter = activeFilter,
                onToggleSearch = { viewModel.toggleSearchVisible() },
                onSearchQueryChange = { query ->
                    viewModel.updateFilter(activeFilter.copy(searchQuery = query))
                },
                // Play/Pause button: controls only the logcat service, NOT auto-scroll
                onTogglePlayPause = {
                    if (isRunning) viewModel.stopLogcat() else viewModel.startLogcat()
                },
                onOpenFilter = { showFilterSheet = true },
                onClear = { viewModel.clearLogs() },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                    items(
                        items = logs,
                        key = { it.id },
                    ) { entry ->
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

            // ── "Scroll to bottom" FAB ─────────────────────────────────────
            // Shown whenever auto-scroll is paused (regardless of service state).
            // Tapping it flushes pending entries and re-enables auto-scroll.
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                visible = !isAutoScrolling,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.resumeAutoScroll()
                        scope.launch {
                            if (logs.isNotEmpty()) {
                                // Scroll after state update completes
                                kotlinx.coroutines.delay(50)
                                listState.animateScrollToItem(logs.lastIndex)
                            }
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_mobile_arrow_down),
                            contentDescription = null,
                        )
                    },
                    text = {
                        val label = if (pendingCount > 0)
                            stringResource(R.string.logcat_new_entries, pendingCount)
                        else
                            stringResource(R.string.logcat_resume)
                        Text(label)
                    },
                )
            }
        }
    }

    // ── Detail bottom sheet ────────────────────────────────────────────────
    detailEntry?.let { entry ->
        LogEntryDetailBottomSheet(
            entry = entry,
            onDismiss = { detailEntry = null },
        )
    }

    // ── Filter bottom sheet ────────────────────────────────────────────────
    if (showFilterSheet) {
        LogcatFilterBottomSheet(
            activeFilter = activeFilter,
            savedFilters = savedFilters,
            onApply = { viewModel.updateFilter(it) },
            onSaveProfile = { name -> viewModel.saveCurrentFilter(name) },
            onDeleteProfile = { id -> viewModel.deleteFilter(id) },
            onDismiss = { showFilterSheet = false },
        )
    }

    // ── Permission dialog ──────────────────────────────────────────────────
    if (showPermissionDialog) {
        LogcatPermissionDialog(
            onContinueAnyway = {
                showPermissionDialog = false
                viewModel.startLogcat()
            },
            onGranted = {
                showPermissionDialog = false
                viewModel.startLogcat()
            },
            onDismiss = { showPermissionDialog = false },
        )
    }
}
