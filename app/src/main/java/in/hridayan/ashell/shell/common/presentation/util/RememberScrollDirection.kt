package `in`.hridayan.ashell.shell.common.presentation.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import `in`.hridayan.ashell.core.domain.model.ScrollDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberScrollDirection(
    listState: LazyListState,
    fastThreshold: Int = 150,
    idleDelayMillis: Long = 2000
): ScrollDirection {
    var lastOffset by remember { mutableIntStateOf(0) }
    var direction by remember { mutableStateOf(ScrollDirection.NONE) }
    var job by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val isScrollable = layoutInfo.totalItemsCount > 0 &&
                    layoutInfo.visibleItemsInfo.sumOf { it.size } < layoutInfo.viewportEndOffset

            val atBottom = layoutInfo.visibleItemsInfo.lastOrNull()?.let { lastItem ->
                lastItem.index == layoutInfo.totalItemsCount - 1 &&
                        lastItem.offset + lastItem.size <= layoutInfo.viewportEndOffset
            } ?: false

            if (!isScrollable || atBottom) null
            else listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { value ->
            if (value == null) {
                direction = ScrollDirection.NONE
                job?.cancel()
                job = null
            } else {
                val (index, offset) = value
                val currentOffset = index * 1000 + offset
                val dy = currentOffset - lastOffset

                when {
                    dy < -fastThreshold -> {
                        job?.cancel()
                        direction = ScrollDirection.UP
                        job = null
                    }

                    dy > fastThreshold -> {
                        job?.cancel()
                        direction = ScrollDirection.DOWN
                        job = null
                    }

                    else -> {
                        job?.cancel()
                        job = launch {
                            delay(idleDelayMillis)
                            direction = ScrollDirection.NONE
                        }
                    }
                }

                lastOffset = currentOffset
            }
        }
    }

    return direction
}

