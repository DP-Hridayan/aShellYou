package `in`.hridayan.ashell.core.presentation.components.scrollbar

import androidx.compose.foundation.layout.Box
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
    var appliedAverage = 0f
    var appliedItemCount = -1
    var cumulativeBefore = 0f
    var lastFirstIndex = 0
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
        appliedAverage = 0f
        appliedItemCount = -1
        cumulativeBefore = 0f
        lastFirstIndex = 0
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
            if ((metrics.initialized && metrics.lastFirstIndex >= itemCount) ||
                metrics.measuredCount > itemCount ||
                (metrics.appliedItemCount >= 0 && itemCount < metrics.appliedItemCount)
            ) {
                metrics.reset()
            }

            visibleItems.forEach { metrics.record(it.index, it.size) }

            val trackHeight = size.height
            if (trackHeight <= 0f) return@drawBehind

            val first = visibleItems.first()
            val last = visibleItems.last()
            val atTop = first.index == 0 && listState.firstVisibleItemScrollOffset == 0
            val atBottom = last.index == itemCount - 1 &&
                last.offset + last.size <= layoutInfo.viewportEndOffset + 1
            if (atTop && atBottom) return@drawBehind

            if (metrics.appliedAverage <= 0f || metrics.appliedItemCount != itemCount) {
                metrics.appliedAverage = if (metrics.measuredCount > 0) {
                    metrics.measuredSum.toFloat() / metrics.measuredCount
                } else {
                    0f
                }
                metrics.appliedItemCount = itemCount
            }
            val average = metrics.appliedAverage
            if (average <= 0f) return@drawBehind

            val firstIndex = first.index
            if (!metrics.initialized) {
                metrics.cumulativeBefore = firstIndex * average
                metrics.lastFirstIndex = firstIndex
                metrics.initialized = true
            } else if (firstIndex != metrics.lastFirstIndex) {
                if (firstIndex > metrics.lastFirstIndex) {
                    for (index in metrics.lastFirstIndex until firstIndex) {
                        metrics.cumulativeBefore += metrics.heightAt(index, average)
                    }
                } else {
                    for (index in firstIndex until metrics.lastFirstIndex) {
                        metrics.cumulativeBefore -= metrics.heightAt(index, average)
                    }
                }
                metrics.lastFirstIndex = firstIndex
            }
            if (atTop) {
                metrics.cumulativeBefore = 0f
                metrics.lastFirstIndex = firstIndex
            }
            if (metrics.cumulativeBefore < 0f) metrics.cumulativeBefore = 0f

            val totalHeight = average * itemCount
            if (totalHeight <= trackHeight) return@drawBehind

            val scrollOffset = metrics.cumulativeBefore + listState.firstVisibleItemScrollOffset
            val maxScroll = (totalHeight - trackHeight).coerceAtLeast(1f)
            val progress = when {
                atBottom -> 1f
                atTop -> 0f
                else -> (scrollOffset / maxScroll).coerceIn(0f, 1f)
            }

            val visibleFraction = (trackHeight / totalHeight).coerceIn(0f, 1f)
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
