package `in`.hridayan.ashell.core.presentation.components.scrollbar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A draggable circular scroll thumb that enables smooth fast-scrolling on a [LazyListState].
 */
@Composable
fun DraggableScrollThumb(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thumbSize: Int = 48
) {
    var stableAvgSize by remember { mutableFloatStateOf(0f) }

    val scrollProgress by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            if (info.totalItemsCount == 0 || info.visibleItemsInfo.isEmpty()) 0f
            else {
                val firstVisibleItem = info.visibleItemsInfo.first()
                val totalItemsCount = info.totalItemsCount
                val viewportHeight = info.viewportEndOffset - info.viewportStartOffset

                val visibleItems = info.visibleItemsInfo
                val currentAvg = visibleItems.map { it.size }.average().toFloat()
                
                if (stableAvgSize == 0f) stableAvgSize = currentAvg
                
                val estimatedTotalHeight = stableAvgSize * totalItemsCount
                val currentScrollOffset = (firstVisibleItem.index * stableAvgSize) - firstVisibleItem.offset
                val totalScrollableRange = (estimatedTotalHeight - viewportHeight).coerceAtLeast(1f)
                (currentScrollOffset / totalScrollableRange).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            val info = listState.layoutInfo
            if (info.visibleItemsInfo.isNotEmpty()) {
                val currentAvg = info.visibleItemsInfo.map { it.size }.average().toFloat()
                stableAvgSize = if (stableAvgSize == 0f) currentAvg else stableAvgSize * 0.95f + currentAvg * 0.05f
            }
        }
    }

    DraggableScrollThumbImpl(
        scrollProgress = scrollProgress,
        isScrollInProgress = listState.isScrollInProgress,
        onDrag = { deltaPx, trackRange ->
            val info = listState.layoutInfo
            val vis = info.visibleItemsInfo
            if (vis.isNotEmpty()) {
                val avgSize = vis.map { it.size }.average().toFloat()
                val totalContentHeight = avgSize * info.totalItemsCount
                val viewportHeight = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
                val scrollableRange = (totalContentHeight - viewportHeight).coerceAtLeast(1f)
                
                val scrollDelta = deltaPx * (scrollableRange / trackRange)
                listState.dispatchRawDelta(scrollDelta)
            }
        },
        modifier = modifier,
        thumbSize = thumbSize
    )
}

/**
 * A draggable circular scroll thumb for [ScrollState] (regular Column/Row).
 */
@Composable
fun DraggableScrollThumb(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    thumbSize: Int = 48
) {
    val scrollProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue == 0) 0f
            else (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0f, 1f)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    DraggableScrollThumbImpl(
        scrollProgress = scrollProgress,
        isScrollInProgress = scrollState.isScrollInProgress,
        onDrag = { deltaPx, trackRange ->
            if (scrollState.maxValue > 0) {
                val scrollDelta = deltaPx * (scrollState.maxValue.toFloat() / trackRange)
                coroutineScope.launch {
                    scrollState.scrollBy(scrollDelta)
                }
            }
        },
        modifier = modifier,
        thumbSize = thumbSize
    )
}

@Composable
private fun DraggableScrollThumbImpl(
    scrollProgress: Float,
    isScrollInProgress: Boolean,
    onDrag: (deltaPx: Float, trackRange: Float) -> Unit,
    modifier: Modifier = Modifier,
    thumbSize: Int = 48
) {
    val thumbSizeDp = thumbSize.dp
    val density = LocalDensity.current
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    var isDragging by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    var showThumb by remember { mutableStateOf(false) }

    var dragProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(scrollProgress, isDragging) {
        if (!isDragging) {
            dragProgress = scrollProgress
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = dragProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumbProgress"
    )

    val effectiveProgress = if (isDragging) dragProgress else animatedProgress

    val thumbOffsetY = remember(effectiveProgress, trackHeightPx) {
        (effectiveProgress * (trackHeightPx - thumbSizePx)).coerceIn(
            0f,
            (trackHeightPx - thumbSizePx).coerceAtLeast(0f)
        )
    }

    val thumbOffsetX by animateFloatAsState(
        targetValue = if (showThumb || isDragging) 0f else thumbSizePx,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "thumbOffsetX"
    )

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 0.8f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "thumbScale"
    )

    val thumbAlpha by animateFloatAsState(
        targetValue = if (showThumb || isDragging) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbAlpha"
    )

    LaunchedEffect(isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
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
    ) {
        val thumbColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .size(thumbSizeDp)
                .graphicsLayer {
                    translationX = thumbOffsetX
                    translationY = thumbOffsetY
                    scaleX = thumbScale
                    scaleY = thumbScale
                    alpha = thumbAlpha
                }
                .pointerInput(trackHeightPx) {
                    detectVerticalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()

                            val trackRange = (trackHeightPx - thumbSizePx).coerceAtLeast(1f)
                            val newProgress = (dragProgress + dragAmount / trackRange).coerceIn(0f, 1f)
                            dragProgress = newProgress

                            onDrag(dragAmount, trackRange)
                        }
                    )
                }
                .drawWithContent {
                    val strokeWidth = 10.dp.toPx()
                    val outerRadius = (size.minDimension / 2) - (strokeWidth / 2)

                    drawCircle(
                        color = thumbColor,
                        radius = outerRadius,
                        style = Stroke(width = strokeWidth)
                    )

                    val gap = 2.dp.toPx()
                    val innerRadius = (outerRadius - strokeWidth / 2 - gap).coerceAtLeast(0f)
                    drawCircle(
                        color = thumbColor,
                        radius = innerRadius
                    )
                }
        )
    }
}
