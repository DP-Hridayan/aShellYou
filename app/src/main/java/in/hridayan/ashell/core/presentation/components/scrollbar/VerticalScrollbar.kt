package `in`.hridayan.ashell.core.presentation.components.scrollbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    thumbSize: Int = 16
) {
    val layoutInfo by remember {
        derivedStateOf { listState.layoutInfo }
    }

    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo.size

    if (totalItems == 0 || visibleItems == 0) return
    if (layoutInfo.totalItemsCount <= layoutInfo.visibleItemsInfo.size) return

    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

    val scrollProgress by remember(listState, totalItems, visibleItems) {
        derivedStateOf {
            val totalScrollableItems = (totalItems - visibleItems).coerceAtLeast(1)
            val progress = listState.firstVisibleItemIndex / totalScrollableItems.toFloat()
            progress.coerceIn(0f, 1f)
        }
    }

    // Constant thumb size (circular)
    val thumbSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { thumbSize.dp.toPx().toInt() }
    val thumbOffsetY = (scrollProgress * (viewportHeight - thumbSizePx)).toInt()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize.dp)
                .align(Alignment.TopStart)
                .offset { IntOffset(0, thumbOffsetY) }
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}
