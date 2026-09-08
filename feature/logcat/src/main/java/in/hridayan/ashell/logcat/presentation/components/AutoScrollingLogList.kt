package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.presentation.model.LogListActions
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private val LogListBottomPadding = 80.dp
private val FabBottomPadding = 16.dp

/**
 * Log list that follows the newest entry while [isAutoScrolling] is on.
 * A touch drag pauses following; the scroll-to-bottom button resumes it.
 */
@Composable
fun AutoScrollingLogList(
    logs: List<LogEntry>,
    expandedIds: Set<Long>,
    listState: LazyListState,
    isAutoScrolling: Boolean,
    actions: LogListActions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = LogListBottomPadding),
) {
    val scope = rememberCoroutineScope()
    val latestLogs by rememberUpdatedState(logs)

    PauseOnUserScroll(listState, isAutoScrolling, actions.onPauseAutoScroll)
    FollowNewestEntry(listState, logs, isAutoScrolling)

    Box(modifier = modifier.fillMaxSize()) {
        LogList(logs, expandedIds, listState, contentPadding, actions)
        ScrollToBottomFab(
            visible = !isAutoScrolling,
            onClick = {
                scope.launch {
                    jumpToBottom(listState, latestLogs.lastIndex)
                    actions.onResumeAutoScroll()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = FabBottomPadding),
        )
    }
}

private suspend fun jumpToBottom(listState: LazyListState, lastIndex: Int) {
    if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
}

@Composable
private fun PauseOnUserScroll(
    listState: LazyListState,
    isAutoScrolling: Boolean,
    onPause: () -> Unit,
) {
    val autoScrolling by rememberUpdatedState(isAutoScrolling)
    val currentOnPause by rememberUpdatedState(onPause)
    LaunchedEffect(listState) {
        listState.interactionSource.interactions
            .filterIsInstance<DragInteraction.Start>()
            .collect { if (autoScrolling) currentOnPause() }
    }
}

@Composable
private fun FollowNewestEntry(
    listState: LazyListState,
    logs: List<LogEntry>,
    isAutoScrolling: Boolean,
) {
    val latestLogs by rememberUpdatedState(logs)
    LaunchedEffect(listState, isAutoScrolling) {
        if (!isAutoScrolling) return@LaunchedEffect
        snapshotFlow { latestLogs.lastOrNull()?.id }
            .filterNotNull()
            .collect { listState.requestScrollToItem(latestLogs.lastIndex) }
    }
}

@Composable
private fun LogList(
    logs: List<LogEntry>,
    expandedIds: Set<Long>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    actions: LogListActions,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = contentPadding,
    ) {
        items(items = logs, key = { it.id }) { entry ->
            LogEntryRow(
                modifier = Modifier.fillMaxWidth(),
                entry = entry,
                isExpanded = entry.id in expandedIds,
                onClick = { actions.onToggleExpanded(entry.id) },
                onLongClick = { actions.onLongClick(entry) },
            )
        }
    }
}

@Composable
private fun ScrollToBottomFab(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mobile_arrow_down),
                contentDescription = stringResource(R.string.scroll_to_bottom),
            )
        }
    }
}
