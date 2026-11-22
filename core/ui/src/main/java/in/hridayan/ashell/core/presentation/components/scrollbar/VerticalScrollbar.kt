package `in`.hridayan.ashell.core.presentation.components.scrollbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private class ScrollbarMetrics {
    val heights = HashMap<Int, Int>()
    var measuredSum = 0L
    var measuredCount = 0
    var cumulativeBefore = 0f
    var lastFirstIndex = -1
    var spacingPx = 0f
    var viewportWidth = -1
    var totalHeight = 0f
    var totalItemCount = -1
    var initialized = false

    fun record(index: Int, height: Int) {
        val previous = heights.put(index, height)
        when {
            previous == null -> {
                measuredSum += height
                measuredCount++
            }
            previous != height -> {
                measuredSum += (height - previous).toLong()
            }
        }
    }

    fun reset() {
        heights.clear()
        measuredSum = 0L
        measuredCount = 0
        cumulativeBefore = 0f
        lastFirstIndex = -1
        spacingPx = 0f
        viewportWidth = -1
        totalHeight = 0f
        totalItemCount = -1
        initialized = false
    }

    fun heightAt(index: Int, fallback: Float): Float =
        heights[index]?.toFloat() ?: fallback
}

@Composable
fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    thumbSize: Int = 16
) {
    val metrics = remember(listState) { ScrollbarMetrics() }
    val density = LocalDensity.current
    val minThumbPx = with(density) { thumbSize.dp.toPx() }
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val thumbColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.drawBehind {
            val layoutInfo = listState.layoutInfo
            val itemCount = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo
            if (itemCount <= 0 || visibleItems.isEmpty()) {
                metrics.reset()
                return@drawBehind
            }

            val viewportWidth = layoutInfo.viewportSize.width
            if (metrics.measuredCount > itemCount ||
                (metrics.viewportWidth >= 0 && metrics.viewportWidth != viewportWidth)
            ) {
                metrics.reset()
            }
            metrics.viewportWidth = viewportWidth

            visibleItems.forEach { metrics.record(it.index, it.size) }

            val trackHeight = size.height
            if (trackHeight <= 0f) return@drawBehind

            val average = if (metrics.measuredCount > 0) {
                metrics.measuredSum.toFloat() / metrics.measuredCount
            } else {
                0f
            }
            if (average <= 0f) return@drawBehind

            val measuredSpacing = visibleSpacing(visibleItems)
            if (measuredSpacing > 0f) metrics.spacingPx = measuredSpacing
            val spacing = metrics.spacingPx
            val stepHeight = average + spacing

            val first = visibleItems.first()
            val last = visibleItems.last()
            val firstIndex = first.index
            val atTop = firstIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            val atBottom = !listState.canScrollForward

            if (!metrics.initialized) {
                metrics.cumulativeBefore = firstIndex * stepHeight
                metrics.lastFirstIndex = firstIndex
                metrics.initialized = true
            } else if (firstIndex > metrics.lastFirstIndex) {
                for (index in metrics.lastFirstIndex until firstIndex) {
                    metrics.cumulativeBefore += metrics.heightAt(index, average) + spacing
                }
                metrics.lastFirstIndex = firstIndex
            } else if (firstIndex < metrics.lastFirstIndex) {
                for (index in firstIndex until metrics.lastFirstIndex) {
                    metrics.cumulativeBefore -= metrics.heightAt(index, average) + spacing
                }
                metrics.lastFirstIndex = firstIndex
            }
            if (atTop) {
                metrics.cumulativeBefore = 0f
                metrics.lastFirstIndex = firstIndex
            }
            if (metrics.cumulativeBefore < 0f) metrics.cumulativeBefore = 0f

            val scrollOffset = metrics.cumulativeBefore + listState.firstVisibleItemScrollOffset
            val unseenAfter = itemCount - 1 - last.index
            val anchored = scrollOffset + last.offset + last.size + stepHeight * unseenAfter
            if (metrics.totalItemCount != itemCount) {
                val grew = metrics.totalItemCount < 0 || itemCount > metrics.totalItemCount
                metrics.totalHeight = if (grew) {
                    maxOf(metrics.totalHeight, anchored)
                } else {
                    anchored
                }
                metrics.totalItemCount = itemCount
            } else if (atBottom) {
                metrics.totalHeight = anchored
            } else if (!listState.isScrollInProgress) {
                metrics.totalHeight = maxOf(metrics.totalHeight, anchored)
            }
            val contentHeight = metrics.totalHeight
            if (contentHeight <= trackHeight) return@drawBehind
            if (atTop && !listState.canScrollForward) return@drawBehind

            val maxScroll = (contentHeight - trackHeight).coerceAtLeast(1f)
            val progress = when {
                atTop || !listState.canScrollBackward -> 0f
                atBottom -> 1f
                else -> (scrollOffset / maxScroll).coerceIn(0f, 1f)
            }

            val visibleFraction = (trackHeight / contentHeight).coerceIn(0f, 1f)
            val thumbHeight = (visibleFraction * trackHeight)
                .coerceIn(minThumbPx.coerceAtMost(trackHeight), trackHeight)
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

private fun visibleSpacing(items: List<LazyListItemInfo>): Float {
    if (items.size < 2) return 0f
    var smallest = Float.MAX_VALUE
    for (i in 0 until items.size - 1) {
        val gap = (items[i + 1].offset - items[i].offset - items[i].size).toFloat()
        if (gap >= 0f && gap < smallest) smallest = gap
    }
    return if (smallest == Float.MAX_VALUE) 0f else smallest
}
