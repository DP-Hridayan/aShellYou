package `in`.hridayan.ashell.core.presentation.components.scrollbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

private const val THUMB_TRACK_FRACTION = 0.25f

@Composable
fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    thumbSize: Int = 16
) {
    val shouldShowScrollbar by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo.size
            totalItems > 0 && visibleItems > 0 && totalItems > visibleItems
        }
    }

    if (!shouldShowScrollbar) return

    val density = LocalDensity.current
    var trackHeightPx by remember { mutableFloatStateOf(0f) }

    val thumbHeightPx = if (trackHeightPx > 0f) {
        (trackHeightPx * THUMB_TRACK_FRACTION)
            .coerceAtLeast(with(density) { thumbSize.dp.toPx() })
            .coerceAtMost(trackHeightPx)
    } else {
        0f
    }

    val progress by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo.size
            val totalScrollableItems = (totalItems - visibleItems).coerceAtLeast(1)
            (listState.firstVisibleItemIndex / totalScrollableItems.toFloat()).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { thumbHeightPx.toDp() })
                .align(Alignment.TopStart)
                .offset {
                    val travel = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)
                    IntOffset(0, (progress * travel).toInt())
                }
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}
