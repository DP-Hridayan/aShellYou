package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.presentation.model.LogListActions
import kotlinx.coroutines.launch

private val LogListBottomPadding = 80.dp
private val FabBottomPadding = 16.dp

/**
 * Log list that follows the newest entry while [isAutoScrolling] is on.
 *
 * Touching the list stops following from the moment the finger lands. The flag that
 * gates the follow scroll lives here and is written by the pointer handler itself, so
 * a busy main thread cannot delay it: no recomposition stands between the touch and
 * the list going still. Only the scroll-to-bottom button resumes following.
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
    val isFollowing = remember { mutableStateOf(isAutoScrolling) }

    LaunchedEffect(isAutoScrolling) { isFollowing.value = isAutoScrolling }

    FollowNewestEntry(listState, isFollowing)

    Box(modifier = modifier.fillMaxSize()) {
        LogList(
            logs = logs,
            expandedIds = expandedIds,
            listState = listState,
            contentPadding = contentPadding,
            actions = actions,
            modifier = Modifier.stopFollowingOnTouch(isFollowing, actions.onPauseAutoScroll),
        )
        ScrollToBottomFab(
            visible = !isAutoScrolling,
            onClick = {
                scope.launch {
                    jumpToNewest(listState)
                    actions.onResumeAutoScroll()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = FabBottomPadding),
        )
    }
}

private suspend fun jumpToNewest(listState: LazyListState) {
    val lastIndex = listState.layoutInfo.totalItemsCount - 1
    if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
}

/**
 * Clears [isFollowing] on the initial pass of the first pointer going down, before touch
 * slop and before the list sees the gesture, then reports the pause. Nothing is consumed,
 * so scrolling, taps and long presses behave as they did.
 */
private fun Modifier.stopFollowingOnTouch(
    isFollowing: MutableState<Boolean>,
    onPause: () -> Unit,
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        isFollowing.value = false
        onPause()
    }
}

/**
 * Keeps the newest entry in view while following. The effect is never restarted, and
 * [isFollowing] is read at the moment of each scroll rather than captured, so the gate
 * reflects the latest touch even when recomposition is lagging behind the log stream.
 */
@Composable
private fun FollowNewestEntry(
    listState: LazyListState,
    isFollowing: State<Boolean>,
) {
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .collect { count ->
                if (isFollowing.value && count > 0 && !listState.isScrollInProgress) {
                    listState.requestScrollToItem(count - 1)
                }
            }
    }
}

@Composable
private fun LogList(
    logs: List<LogEntry>,
    expandedIds: Set<Long>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    actions: LogListActions,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
