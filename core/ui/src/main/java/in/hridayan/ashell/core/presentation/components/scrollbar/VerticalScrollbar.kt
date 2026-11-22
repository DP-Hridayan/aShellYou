package `in`.hridayan.ashell.core.presentation.components.scrollbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    thumbSize: Int = 16
) {
    var estimatedItemHeight by remember(listState) { mutableFloatStateOf(0f) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .distinctUntilChanged()
            .collect {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                if (visibleItems.isNotEmpty()) {
                    estimatedItemHeight =
                        visibleItems.sumOf { it.size }.toFloat() / visibleItems.size
                }
            }
    }

    val density = LocalDensity.current
    val minThumbPx = with(density) { thumbSize.dp.toPx() }
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val thumbColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.drawBehind {
            val layoutInfo = listState.layoutInfo
            val itemCount = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo
            if (itemCount <= 0 || visibleItems.isEmpty()) return@drawBehind

            val trackHeight = size.height
            if (trackHeight <= 0f) return@drawBehind

            val firstVisible = visibleItems.first()
            val lastVisible = visibleItems.last()
            val isScrollable = firstVisible.index > 0 ||
                listState.firstVisibleItemScrollOffset > 0 ||
                lastVisible.index < itemCount - 1 ||
                lastVisible.offset + lastVisible.size > layoutInfo.viewportEndOffset
            if (!isScrollable) return@drawBehind

            val itemHeight = if (estimatedItemHeight > 0f) {
                estimatedItemHeight
            } else {
                visibleItems.sumOf { it.size }.toFloat() / visibleItems.size
            }
            if (itemHeight <= 0f) return@drawBehind

            val contentHeight = itemHeight * itemCount
            val visibleFraction = (trackHeight / contentHeight).coerceIn(0f, 1f)
            val thumbHeight = (visibleFraction * trackHeight)
                .coerceIn(minThumbPx.coerceAtMost(trackHeight), trackHeight)

            val maxScroll = (contentHeight - trackHeight).coerceAtLeast(1f)
            val scrollOffset =
                listState.firstVisibleItemIndex * itemHeight + listState.firstVisibleItemScrollOffset
            val progress = (scrollOffset / maxScroll).coerceIn(0f, 1f)
            val thumbOffsetY = progress * (trackHeight - thumbHeight).coerceAtLeast(0f)

            val radius = size.width / 2f
            drawRoundRect(
                color = trackColor,
                size = Size(size.width, trackHeight),
                cornerRadius = CornerRadius(radius, radius)
            )
            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(0f, thumbOffsetY),
                size = Size(size.width, thumbHeight),
                cornerRadius = CornerRadius(radius, radius)
            )
        }
    )
}
