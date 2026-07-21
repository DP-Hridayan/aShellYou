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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    thumbSize: Int = 16
) {
    val shouldShowScrollbar by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo.size
            totalItems > 0 && visibleItems > 0 && totalItems > visibleItems
        }
    }

    if (!shouldShowScrollbar) return

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize.dp)
                .align(Alignment.TopStart)
                .offset {
                    val layoutInfo = listState.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount
                    val visibleItems = layoutInfo.visibleItemsInfo.size

                    val viewportHeight =
                        layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                    val thumbSizePx = with(density) { thumbSize.dp.toPx() }

                    val totalScrollableItems = (totalItems - visibleItems).coerceAtLeast(1)

                    val progress = listState.firstVisibleItemIndex / totalScrollableItems.toFloat()
                    val coercedProgress = progress.coerceIn(0f, 1f)

                    val thumbOffsetY = (coercedProgress * (viewportHeight - thumbSizePx)).toInt()

                    IntOffset(0, thumbOffsetY)
                }
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}