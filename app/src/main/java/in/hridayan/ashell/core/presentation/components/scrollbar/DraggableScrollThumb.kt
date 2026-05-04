package `in`.hridayan.ashell.core.presentation.components.scrollbar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A draggable circular scroll thumb that enables smooth fast-scrolling on a [LazyListState].
 *
 * Uses [dispatchRawDelta] for synchronous, jitter-free scrolling during drag.
 */
@Composable
fun DraggableScrollThumb(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thumbSize: Int = 48
) {
    val canScroll by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            info.totalItemsCount > 0 && info.visibleItemsInfo.size < info.totalItemsCount
        }
    }

    if (!canScroll) return

    val thumbSizeDp = thumbSize.dp
    val thumbSizePx = with(LocalDensity.current) { thumbSizeDp.toPx() }

    // Scroll progress from list state (0..1)
    val scrollProgress by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            if (info.totalItemsCount <= 1 || info.visibleItemsInfo.isEmpty()) 0f
            else {
                val first = info.visibleItemsInfo.first()
                val maxIndex = info.totalItemsCount - 1
                val frac = if (first.size > 0) -first.offset.toFloat() / first.size else 0f
                ((first.index + frac) / maxIndex).coerceIn(0f, 1f)
            }
        }
    }

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var showThumb by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    var lastDragY by remember { mutableFloatStateOf(0f) }

    // Keep dragProgress synced when not dragging
    LaunchedEffect(scrollProgress, isDragging) {
        if (!isDragging) dragProgress = scrollProgress
    }

    val animatedProgress by animateFloatAsState(
        targetValue = scrollProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scrollProgress"
    )

    // During drag: thumb follows finger directly. Otherwise: follows scroll with animation.
    val effectiveProgress = if (isDragging) dragProgress else animatedProgress
    val thumbOffsetY = (effectiveProgress * (trackHeightPx - thumbSizePx)).coerceAtLeast(0f)

    val thumbAlpha by animateFloatAsState(
        targetValue = if (showThumb || isDragging) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbAlpha"
    )

    LaunchedEffect(listState.isScrollInProgress, isDragging) {
        if (listState.isScrollInProgress || isDragging) {
            showThumb = true
        } else {
            delay(1500)
            showThumb = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(thumbSizeDp)
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        lastDragY = offset.y
                        dragProgress = ((offset.y - thumbSizePx / 2) /
                                (trackHeightPx - thumbSizePx)).coerceIn(0f, 1f)
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        val y = change.position.y
                        val deltaY = y - lastDragY
                        lastDragY = y

                        // Update thumb position directly from finger
                        dragProgress = ((y - thumbSizePx / 2) /
                                (trackHeightPx - thumbSizePx)).coerceIn(0f, 1f)

                        // Synchronous scroll — no coroutine, no jitter
                        val info = listState.layoutInfo
                        val vis = info.visibleItemsInfo
                        if (vis.isNotEmpty() && deltaY != 0f) {
                            val avgSize = vis.sumOf { it.size }.toFloat() / vis.size
                            val totalContent = avgSize * info.totalItemsCount
                            val vpHeight =
                                (info.viewportEndOffset - info.viewportStartOffset).toFloat()
                            val scrollRange = (totalContent - vpHeight).coerceAtLeast(1f)
                            val trackRange = (trackHeightPx - thumbSizePx).coerceAtLeast(1f)
                            val scrollDelta = deltaY * (scrollRange / trackRange)

                            listState.dispatchRawDelta(scrollDelta)
                        }
                    }
                )
            }
    ) {
        val thumbColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .size(thumbSizeDp)
                .align(Alignment.TopStart)
                .offset { IntOffset(0, thumbOffsetY.toInt()) }
                .drawWithContent {
                    val strokeWidth = 10.dp.toPx()
                    val radius = size.minDimension / 2

                    drawCircle(
                        color = thumbColor.copy(alpha = thumbAlpha),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                }
        )
    }
}
