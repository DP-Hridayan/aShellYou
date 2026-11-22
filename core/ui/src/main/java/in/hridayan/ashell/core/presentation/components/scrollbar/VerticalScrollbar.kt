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
            val visibleCount = layoutInfo.visibleItemsInfo.size
            if (itemCount <= 0 || visibleCount <= 0 || itemCount <= visibleCount) return@drawBehind

            val trackHeight = size.height
            if (trackHeight <= 0f) return@drawBehind

            val contentHeight = estimatedItemHeight * itemCount
            val visibleFraction = if (contentHeight > 0f) {
                (trackHeight / contentHeight).coerceIn(0f, 1f)
            } else {
                0f
            }
            val thumbHeight = (visibleFraction * trackHeight)
                .coerceIn(minThumbPx.coerceAtMost(trackHeight), trackHeight)
            val totalScrollableItems = (itemCount - visibleCount).coerceAtLeast(1)
            val progress = (listState.firstVisibleItemIndex / totalScrollableItems.toFloat())
                .coerceIn(0f, 1f)
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
