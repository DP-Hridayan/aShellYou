package `in`.hridayan.ashell.core.presentation.components.selectioncontainer

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * A data class representing a selection across lines in a [LazyColumn].
 *
 * @property startLine The index of the item where the selection begins.
 * @property startOffset The character offset within the start item.
 * @property endLine The index of the item where the selection ends.
 * @property endOffset The character offset within the end item.
 */
data class LineSelection(
    val startLine: Int,
    val startOffset: Int,
    val endLine: Int,
    val endOffset: Int,
) {
    /**
     * Returns a normalized version of this selection where the start position
     * is guaranteed to precede or equal the end position in reading order.
     */
    fun normalized(): LineSelection =
        if (startLine < endLine || (startLine == endLine && startOffset <= endOffset)) this
        else LineSelection(endLine, endOffset, startLine, startOffset)
}

/**
 * Internal representation of a line's layout information used for selection hit-testing
 * and rendering highlights.
 */
internal data class LineInfo(val boundsInWindow: Rect, val layoutResult: TextLayoutResult)

/**
 * A generic mutable reference holder for values that need to be accessed across scopes.
 */
private class Ref<T>(var value: T)

/**
 * Represents the end of the selection that the toolbar should be anchored to.
 */
enum class SelectionEnd { START, END }

/**
 * State holder for text selection within a [LazySelectionContainer].
 * Manages selection coordinates, visibility of handles, and the selection toolbar.
 */
@Stable
class SelectionState {
    /**
     * The current selection range, or null if no text is selected.
     */
    var selection by mutableStateOf<LineSelection?>(null)

    /**
     * Controls the visibility of the selection handles.
     */
    var handlesVisible by mutableStateOf(false)

    /**
     * Controls the visibility of the selection toolbar (Copy/Select All).
     */
    var toolbarVisible by mutableStateOf(false)

    /**
     * Specifies which end of the selection the toolbar is currently anchored to.
     */
    var toolbarPinnedEnd by mutableStateOf(SelectionEnd.END)

    /**
     * Position for the magnifier loupe, in container-local coordinates.
     * Non-null only while a drag is in progress (initial selection or handle drag).
     */
    var magnifierOffset by mutableStateOf<Offset?>(null)

    /**
     * Map of currently composed line indices to their layout information.
     */
    internal val lineInfos = mutableMapOf<Int, LineInfo>()

    /**
     * A version counter used to trigger recomposition of the selection overlay
     * when line layout information changes.
     */
    internal var lineInfoVersion by mutableIntStateOf(0)
    internal var containerWindowOrigin: Offset = Offset.Zero
    internal var containerHeightPx: Float = 0f
    internal var containerWidthPx: Float = 0f

    /**
     * Clears the current selection and hides all selection UI components.
     */
    fun clear() {
        selection = null
        handlesVisible = false
        toolbarVisible = false
        magnifierOffset = null
    }
}

/**
 * Creates and remembers a [SelectionState] instance.
 */
@Composable
fun rememberSelectionState(): SelectionState = remember { SelectionState() }

/**
 * Adds text selection gesture support to a [Modifier].
 * This includes long-press to initiate selection and drag to extend it with auto-scroll support.
 *
 * @param selectionState The state holder for the selection.
 * @param items The list of items in the container.
 * @param textOf A function to extract selectable text from an item.
 * @param listState The state of the [LazyColumn].
 * @param autoScrollEdgeThreshold The distance from the edge that triggers auto-scrolling.
 * @param autoScrollSpeed The speed of auto-scrolling.
 */
fun <T> Modifier.textSelectionGestures(
    selectionState: SelectionState,
    items: List<T>,
    textOf: (T) -> String,
    listState: LazyListState,
    autoScrollEdgeThreshold: Dp = 48.dp,
    autoScrollSpeed: Dp = 4.dp,
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val edgeThresholdPx = with(density) { autoScrollEdgeThreshold.toPx() }
    val scrollSpeedPx = with(density) { autoScrollSpeed.toPx() }

    val currentItems by rememberUpdatedState(items)
    val currentTextOf by rememberUpdatedState(textOf)

    this
        .onGloballyPositioned { coords ->
            selectionState.containerWindowOrigin = coords.boundsInWindow().topLeft
            selectionState.containerHeightPx = coords.size.height.toFloat()
            selectionState.containerWidthPx = coords.size.width.toFloat()
        }
        .pointerInput(Unit) {
            val touchSlop = viewConfiguration.touchSlop
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (down.isConsumed) return@awaitEachGesture

                val longPress = awaitLongPressOrCancellation(down.id)

                if (longPress == null) {
                    var moved = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.changedToUpIgnoreConsumed()) break
                        val dx = change.position.x - down.position.x
                        val dy = change.position.y - down.position.y
                        if (kotlin.math.hypot(dx, dy) > touchSlop) moved = true
                    }
                    if (!moved && selectionState.selection != null) {
                        selectionState.clear()
                    }
                    return@awaitEachGesture
                }

                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectionState.handlesVisible = false
                selectionState.toolbarVisible = false

                val startGlobal = longPress.position + selectionState.containerWindowOrigin
                val start = findLineOffset(selectionState, startGlobal) ?: return@awaitEachGesture
                val lineText = currentTextOf(currentItems[start.first])
                val wordRange = wordBoundsAt(lineText, start.second)
                selectionState.selection =
                    LineSelection(start.first, wordRange.first, start.first, wordRange.last)

                val pointerPosRef = Ref(longPress.position)
                selectionState.magnifierOffset = computeMagnifierOffset(
                    selectionState, start.first, start.second, longPress.position.x
                )
                val loopJob = coroutineScope.launch {
                    while (isActive) {
                        autoScrollIfNearEdge(
                            listState, pointerPosRef.value.y, selectionState.containerHeightPx,
                            edgeThresholdPx, scrollSpeedPx
                        )
                        val globalPos = pointerPosRef.value + selectionState.containerWindowOrigin
                        val end = findLineOffsetOrNearestEdge(selectionState, globalPos)
                        if (end != null) {
                            selectionState.selection = selectionState.selection?.copy(
                                endLine = end.first, endOffset = end.second
                            )
                            selectionState.magnifierOffset = computeMagnifierOffset(
                                selectionState, end.first, end.second, pointerPosRef.value.x
                            )
                        }
                        delay(16.milliseconds)
                    }
                }

                drag(longPress.id) { change ->
                    change.consume()
                    pointerPosRef.value = change.position
                }

                loopJob.cancel()
                selectionState.magnifierOffset = null
                val finalSelection = selectionState.selection
                val hasSelection = finalSelection != null
                selectionState.handlesVisible = hasSelection
                selectionState.toolbarVisible = hasSelection
                if (finalSelection != null) {
                    selectionState.toolbarPinnedEnd =
                        if (finalSelection == finalSelection.normalized()) SelectionEnd.END else SelectionEnd.START
                }
            }
        }
}

/**
 * Registers an item's layout for text selection and renders its selection highlight.
 * Must be attached to each selectable [Text] component.
 *
 * @param index The index of the item in the list.
 * @param text The text content of the item.
 * @param selectionState The state holder for the selection.
 * @param style The [TextStyle] used to render the text. Must match exactly for correct layout calculations.
 * @param highlightColor The color used for the selection background.
 */
@Composable
fun Modifier.allowTextSelection(
    index: Int,
    text: String,
    selectionState: SelectionState,
    style: TextStyle = TextStyle.Default,
    highlightColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
): Modifier = composed {
    val textMeasurer = rememberTextMeasurer()
    val windowBounds = remember(index) { Ref(Rect.Zero) }
    val measuredWidth = remember(index) { Ref(-1) }
    var cachedLayout by remember(index) { mutableStateOf<TextLayoutResult?>(null) }

    DisposableEffect(index) {
        onDispose {
            selectionState.lineInfos.remove(index)
            selectionState.lineInfoVersion++
        }
    }

    this
        .onGloballyPositioned { coords ->
            windowBounds.value = coords.boundsInWindow()
            val width = coords.size.width
            if (width > 0 && width != measuredWidth.value) {
                cachedLayout = textMeasurer.measure(
                    text = AnnotatedString(text),
                    style = style,
                    constraints = Constraints(maxWidth = width)
                )
                measuredWidth.value = width
            }
            cachedLayout?.let {
                selectionState.lineInfos[index] = LineInfo(windowBounds.value, it)
                selectionState.lineInfoVersion++
            }
        }
        .drawBehind {
            val result = cachedLayout ?: return@drawBehind
            val sel = selectionState.selection?.normalized() ?: return@drawBehind
            if (index < sel.startLine || index > sel.endLine) return@drawBehind

            val lineStart =
                (if (index == sel.startLine) sel.startOffset else 0).coerceIn(0, text.length)
            val lineEnd =
                (if (index == sel.endLine) sel.endOffset else text.length).coerceIn(0, text.length)
            if (lineStart == lineEnd) return@drawBehind

            val path = result.getPathForRange(minOf(lineStart, lineEnd), maxOf(lineStart, lineEnd))
            drawPath(path, color = highlightColor)
        }
}

/**
 * Overlay that renders selection handles and the selection toolbar.
 */
@Composable
fun <T> SelectionHandlesOverlay(
    selectionState: SelectionState,
    items: List<T>,
    textOf: (T) -> String,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    handleColor: Color = MaterialTheme.colorScheme.primary,
    autoScrollEdgeThreshold: Dp = 48.dp,
    autoScrollSpeed: Dp = 6.dp,
    onCopy: ((success: Boolean) -> Unit)? = null,
) {
    if (!selectionState.handlesVisible) return

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val edgeThresholdPx = with(density) { autoScrollEdgeThreshold.toPx() }
    val scrollSpeedPx = with(density) { autoScrollSpeed.toPx() }

    // The non-dragged handle's (line, offset) — captured on drag-start,
    // read by onDragTo so the anchor stays fixed even when handles cross.
    val dragAnchor = remember { Ref<Pair<Int, Int>?>(null) }

    fun doCopy() {
        scope.launch {
            val sel = selectionState.selection?.normalized() ?: return@launch
            copySelection(clipboard, items, textOf, sel, onCopy)
        }
        selectionState.toolbarVisible = false
    }

    fun doSelectAll() {
        selectAll(selectionState, items)
        selectionState.toolbarVisible = false
        selectionState.toolbarPinnedEnd = SelectionEnd.START
        scope.launch { listState.scrollToItem(0) }
    }

    val startPosProvider = remember(selectionState) {
        {
            selectionState.lineInfoVersion
            val sel = selectionState.selection?.normalized()
            if (sel != null) computeHandleOffset(
                selectionState,
                sel.startLine,
                sel.startOffset
            ) else null
        }
    }

    val endPosProvider = remember(selectionState) {
        {
            selectionState.lineInfoVersion
            val sel = selectionState.selection?.normalized()
            if (sel != null) computeHandleOffset(
                selectionState,
                sel.endLine,
                sel.endOffset
            ) else null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
        SelectionHandle(
            positionProvider = startPosProvider,
            isStartVisual = true,
            color = handleColor,
            selectionState = selectionState,
            listState = listState,
            edgeThresholdPx = edgeThresholdPx,
            scrollSpeedPx = scrollSpeedPx,
            onTap = {
                selectionState.toolbarVisible =
                    !(selectionState.toolbarVisible && selectionState.toolbarPinnedEnd == SelectionEnd.START)
                selectionState.toolbarPinnedEnd = SelectionEnd.START
            },
            onDragStart = {
                val norm = selectionState.selection?.normalized()
                dragAnchor.value = norm?.let { it.endLine to it.endOffset }
                selectionState.toolbarVisible = false
            },
            onDragEnd = {
                val finalSel = selectionState.selection
                // After crossover the dragged position is the normalized
                // end, so pin the toolbar there; otherwise keep it at START.
                selectionState.toolbarPinnedEnd =
                    if (finalSel != null && finalSel != finalSel.normalized()) SelectionEnd.END
                    else SelectionEnd.START
                selectionState.toolbarVisible = true
                selectionState.magnifierOffset = null
                dragAnchor.value = null
            },
            onDragTo = { rawPos ->
                val global = rawPos + selectionState.containerWindowOrigin
                val result = findLineOffsetOrNearestEdge(selectionState, global)
                val anchor = dragAnchor.value
                if (result != null && anchor != null) {
                    selectionState.magnifierOffset = computeMagnifierOffset(
                        selectionState, result.first, result.second, rawPos.x
                    )
                    // Dragged position = start, anchor = end.
                    // Normalization swaps them for display if they cross.
                    selectionState.selection = LineSelection(
                        startLine = result.first,
                        startOffset = result.second,
                        endLine = anchor.first,
                        endOffset = anchor.second,
                    )
                } else if (result != null) {
                    // Fallback before anchor is captured (loopJob fires
                    // before onDragStart on the very first frame).
                    updateSelectionForVisualHandle(
                        selectionState,
                        isStartVisual = true,
                        line = result.first,
                        offset = result.second
                    )
                }
            },
        )

        SelectionHandle(
            positionProvider = endPosProvider,
            isStartVisual = false,
            color = handleColor,
            selectionState = selectionState,
            listState = listState,
            edgeThresholdPx = edgeThresholdPx,
            scrollSpeedPx = scrollSpeedPx,
            onTap = {
                selectionState.toolbarVisible =
                    !(selectionState.toolbarVisible && selectionState.toolbarPinnedEnd == SelectionEnd.END)
                selectionState.toolbarPinnedEnd = SelectionEnd.END
            },
            onDragStart = {
                val norm = selectionState.selection?.normalized()
                dragAnchor.value = norm?.let { it.startLine to it.startOffset }
                selectionState.toolbarVisible = false
            },
            onDragEnd = {
                val finalSel = selectionState.selection
                // After crossover the dragged position is the normalized
                // start, so pin the toolbar there; otherwise keep it at END.
                selectionState.toolbarPinnedEnd =
                    if (finalSel != null && finalSel != finalSel.normalized()) SelectionEnd.START
                    else SelectionEnd.END
                selectionState.toolbarVisible = true
                selectionState.magnifierOffset = null
                dragAnchor.value = null
            },
            onDragTo = { rawPos ->
                val global = rawPos + selectionState.containerWindowOrigin
                val result = findLineOffsetOrNearestEdge(selectionState, global)
                val anchor = dragAnchor.value
                if (result != null && anchor != null) {
                    selectionState.magnifierOffset = computeMagnifierOffset(
                        selectionState, result.first, result.second, rawPos.x
                    )
                    // Anchor = start, dragged position = end.
                    // Normalization swaps them for display if they cross.
                    selectionState.selection = LineSelection(
                        startLine = anchor.first,
                        startOffset = anchor.second,
                        endLine = result.first,
                        endOffset = result.second,
                    )
                } else if (result != null) {
                    // Fallback before anchor is captured.
                    updateSelectionForVisualHandle(
                        selectionState,
                        isStartVisual = false,
                        line = result.first,
                        offset = result.second
                    )
                }
            },
        )

        if (selectionState.toolbarVisible) {
            val toolbarAnchorProvider = remember(selectionState) {
                {
                    when (selectionState.toolbarPinnedEnd) {
                        SelectionEnd.START -> startPosProvider()
                        SelectionEnd.END -> endPosProvider()
                    }
                }
            }

            SelectionToolbar(
                anchorProvider = toolbarAnchorProvider,
                containerWidthPx = selectionState.containerWidthPx,
                containerHeightPx = selectionState.containerHeightPx,
                onCopy = ::doCopy,
                onSelectAll = ::doSelectAll,
            )
        }
    }
}

private val startHandleShape =
    RoundedCornerShape(
        topStartPercent = 50,
        topEndPercent = 0,
        bottomEndPercent = 50,
        bottomStartPercent = 50
    )
private val endHandleShape =
    RoundedCornerShape(
        topStartPercent = 0,
        topEndPercent = 50,
        bottomEndPercent = 50,
        bottomStartPercent = 50
    )

/**
 * A draggable selection handle.
 */
@Composable
private fun SelectionHandle(
    positionProvider: () -> Offset?,
    isStartVisual: Boolean,
    color: Color,
    selectionState: SelectionState,
    listState: LazyListState,
    edgeThresholdPx: Float,
    scrollSpeedPx: Float,
    onTap: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragTo: (rawPositionInContainer: Offset) -> Unit,
) {
    val density = LocalDensity.current
    val handleVisualSizeDp = 28.dp
    val handleTouchSizeDp = 48.dp
    val coroutineScope = rememberCoroutineScope()
    val handleTouchPx = with(density) { handleTouchSizeDp.toPx() }

    val currentPositionProvider by rememberUpdatedState(positionProvider)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragTo by rememberUpdatedState(onDragTo)

    Box(
        modifier = Modifier
            .layout { measurable, constraints ->
                val pos = currentPositionProvider()
                if (pos == null) {
                    layout(0, 0) {}
                } else {
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        val x = if (isStartVisual) pos.x - handleTouchPx else pos.x
                        placeable.placeRelative(x.toInt(), pos.y.toInt())
                    }
                }
            }
            .size(handleTouchSizeDp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()

                    var isDragging = false
                    var raw = currentPositionProvider() ?: Offset.Zero
                    val pointerPosRef = Ref(raw)

                    val loopJob = coroutineScope.launch {
                        while (isActive) {
                            autoScrollIfNearEdge(
                                listState, pointerPosRef.value.y, selectionState.containerHeightPx,
                                edgeThresholdPx, scrollSpeedPx
                            )
                            currentOnDragTo(pointerPosRef.value)
                            delay(16.milliseconds)
                        }
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.changedToUpIgnoreConsumed()) {
                            change.consume()
                            break
                        }
                        val delta = change.positionChange()
                        change.consume()
                        if (!isDragging) {
                            isDragging = true
                            currentOnDragStart()
                        }
                        raw += delta
                        pointerPosRef.value = raw
                    }

                    loopJob.cancel()
                    if (isDragging) currentOnDragEnd() else currentOnTap()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .align(if (isStartVisual) Alignment.TopEnd else Alignment.TopStart)
                .size(handleVisualSizeDp)
                .clip(if (isStartVisual) startHandleShape else endHandleShape)
                .background(color)
        )
    }
}

/**
 * A floating toolbar providing actions like Copy and Select All for the current selection.
 */
@Composable
private fun SelectionToolbar(
    anchorProvider: () -> Offset?,
    containerWidthPx: Float,
    containerHeightPx: Float,
    onCopy: () -> Unit,
    onSelectAll: () -> Unit,
) {
    val density = LocalDensity.current
    val textStyle = LocalTextStyle.current
    val gapPx = with(density) { textStyle.lineHeight.toPx() }
    val handleTouchPx = with(density) { 48.dp.toPx() }

    Layout(
        content = {
            Surface(
                shape = RoundedCornerShape(50),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                    SelectionToolbarActionButton(
                        name = stringResource(R.string.copy),
                        onClick = onCopy
                    )

                    SelectionToolbarActionButton(
                        name = stringResource(R.string.select_all),
                        onClick = onSelectAll
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { measurables, constraints ->
        val anchorInContainer =
            anchorProvider() ?: return@Layout layout(constraints.maxWidth, constraints.maxHeight) {}

        val placeable = measurables.first().measure(Constraints())

        val fitsAbove = anchorInContainer.y - gapPx - placeable.height >= 0f
        val y = if (fitsAbove) {
            anchorInContainer.y - gapPx - placeable.height
        } else {
            anchorInContainer.y + maxOf(gapPx, handleTouchPx)
        }
        val clampedY = y.coerceIn(0f, (containerHeightPx - placeable.height).coerceAtLeast(0f))
        val x = (anchorInContainer.x - placeable.width / 2f)
            .coerceIn(0f, (containerWidthPx - placeable.width).coerceAtLeast(0f))

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(x.toInt(), clampedY.toInt())
        }
    }
}

@Composable
private fun SelectionToolbarActionButton(
    modifier: Modifier = Modifier,
    name: String,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
            onClick()
        },
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(name)
    }
}

/** Returns the word bounds around the given character offset. */
private fun wordBoundsAt(text: String, offset: Int): IntRange {
    if (text.isEmpty()) return 0..0
    val clamped = offset.coerceIn(0, text.length)
    val probeIndex = when {
        clamped < text.length && text[clamped].isLetterOrDigit() -> clamped
        clamped > 0 && text[clamped - 1].isLetterOrDigit() -> clamped - 1
        else -> -1
    }
    if (probeIndex == -1) {
        val end = (clamped + 1).coerceAtMost(text.length)
        return clamped.coerceAtMost(end)..end
    }
    var start = probeIndex
    while (start > 0 && text[start - 1].isLetterOrDigit()) start--
    var end = probeIndex + 1
    while (end < text.length && text[end].isLetterOrDigit()) end++
    return start..end
}

/** Triggers list scrolling when the pointer is near the top or bottom edges. */
private suspend fun autoScrollIfNearEdge(
    listState: LazyListState,
    pointerYInContainer: Float,
    containerHeightPx: Float,
    edgeThresholdPx: Float,
    scrollSpeedPx: Float,
) {
    if (containerHeightPx <= 0f || edgeThresholdPx <= 0f) return
    val distanceFromTop = pointerYInContainer
    val distanceFromBottom = containerHeightPx - pointerYInContainer
    when {
        distanceFromTop < edgeThresholdPx -> {
            val factor = 1f - (distanceFromTop / edgeThresholdPx).coerceIn(0f, 1f)
            listState.scrollBy(-scrollSpeedPx * factor)
        }

        distanceFromBottom < edgeThresholdPx -> {
            val factor = 1f - (distanceFromBottom / edgeThresholdPx).coerceIn(0f, 1f)
            listState.scrollBy(scrollSpeedPx * factor)
        }
    }
}

private fun updateSelectionForVisualHandle(
    selectionState: SelectionState,
    isStartVisual: Boolean,
    line: Int,
    offset: Int,
) {
    val current = selectionState.selection ?: return
    val swapped = current != current.normalized()
    val writeRawStart = if (isStartVisual) !swapped else swapped
    val newSel = if (writeRawStart) current.copy(startLine = line, startOffset = offset)
    else current.copy(endLine = line, endOffset = offset)
    selectionState.selection = newSel
}

private fun computeHandleOffset(
    selectionState: SelectionState,
    line: Int,
    charOffset: Int,
): Offset? {
    val info = selectionState.lineInfos[line] ?: return null
    val textLength = info.layoutResult.layoutInput.text.length
    val cursorRect = info.layoutResult.getCursorRect(charOffset.coerceIn(0, textLength))
    val windowPoint = info.boundsInWindow.topLeft + Offset(cursorRect.left, cursorRect.bottom)
    val local = windowPoint - selectionState.containerWindowOrigin
    if (local.y < 0f || local.y > selectionState.containerHeightPx) {
        return null
    }
    return local
}

private fun <T> selectAll(selectionState: SelectionState, items: List<T>) {
    if (items.isEmpty()) return
    selectionState.selection = LineSelection(0, 0, items.lastIndex, Int.MAX_VALUE)
    selectionState.handlesVisible = true
}

private suspend fun <T> copySelection(
    clipboard: Clipboard,
    items: List<T>,
    textOf: (T) -> String,
    selection: LineSelection,
    onResult: ((success: Boolean) -> Unit)?,
) {
    val text = buildSelectedText(items, textOf, selection)
    val success = if (text.isEmpty()) {
        false
    } else {
        try {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Selected text", text)))
            true
        } catch (e: Exception) {
            false
        }
    }
    onResult?.invoke(success)
}

private fun findLineOffset(state: SelectionState, globalPos: Offset): Pair<Int, Int>? {
    for ((index, info) in state.lineInfos) {
        val b = info.boundsInWindow
        if (globalPos.y in b.top..b.bottom) {
            val local = Offset(
                x = (globalPos.x - b.left).coerceIn(0f, b.width),
                y = (globalPos.y - b.top).coerceIn(0f, b.height)
            )
            val offset = info.layoutResult.getOffsetForPosition(local)
            return index to offset
        }
    }
    return null
}

private fun findLineOffsetOrNearestEdge(state: SelectionState, globalPos: Offset): Pair<Int, Int>? {
    findLineOffset(state, globalPos)?.let { return it }
    if (state.lineInfos.isEmpty()) {
        return null
    }
    val top = state.lineInfos.entries.minByOrNull { it.value.boundsInWindow.top } ?: return null
    val bottom =
        state.lineInfos.entries.maxByOrNull { it.value.boundsInWindow.bottom } ?: return null
    val result = when {
        globalPos.y <= top.value.boundsInWindow.top -> top.key to 0
        globalPos.y >= bottom.value.boundsInWindow.bottom ->
            bottom.key to bottom.value.layoutResult.layoutInput.text.length

        else -> null
    }
    return result
}

private fun <T> buildSelectedText(
    items: List<T>,
    textOf: (T) -> String,
    sel: LineSelection
): String {
    val norm = sel.normalized()
    val sb = StringBuilder()
    for (i in norm.startLine..norm.endLine) {
        val line = items.getOrNull(i)?.let(textOf) ?: continue
        val piece = when {
            norm.startLine == norm.endLine -> {
                val s = norm.startOffset.coerceIn(0, line.length)
                val e = norm.endOffset.coerceIn(0, line.length)
                line.substring(minOf(s, e), maxOf(s, e))
            }

            i == norm.startLine -> line.substring(norm.startOffset.coerceIn(0, line.length))
            i == norm.endLine -> line.substring(0, norm.endOffset.coerceIn(0, line.length))
            else -> line
        }
        sb.append(piece)
        if (i != norm.endLine) sb.append("\n")
    }
    return sb.toString()
}

/**
 * Computes the magnifier source-centre for a resolved [lineIndex] + [charOffset].
 * X is set to [fingerX] so the loupe tracks the finger horizontally, while
 * Y is snapped to the vertical centre of the text line — the magnifier only
 * jumps vertically when the selection moves to a different line, matching
 * native Android behaviour.
 */
private fun computeMagnifierOffset(
    selectionState: SelectionState,
    lineIndex: Int,
    charOffset: Int,
    fingerX: Float,
): Offset? {
    val info = selectionState.lineInfos[lineIndex] ?: return null
    val textLength = info.layoutResult.layoutInput.text.length
    val safeOffset = charOffset.coerceIn(0, textLength)
    val cursorRect = info.layoutResult.getCursorRect(safeOffset)
    val lineCenterY = (cursorRect.top + cursorRect.bottom) / 2f
    val yInContainer =
        info.boundsInWindow.top + lineCenterY - selectionState.containerWindowOrigin.y
    return Offset(fingerX, yInContainer)
}

/**
 * Wrapper composable that adds cross-line text selection to any [LazyColumn]
 * placed inside [content], analogous to how [SelectionContainer] wraps
 * ordinary composables.
 *
 * Place your own [LazyColumn] (or any scrollable list) inside [content],
 * and apply [allowTextSelection] to each selectable item. The wrapper
 * automatically provides gesture detection, selection handles, a magnifier
 * loupe, and a copy/select-all toolbar.
 *
 * Example usage:
 * ```kotlin
 * val listState = rememberLazyListState()
 *
 * LazySelectionContainer(
 *     items = messages,
 *     textOf = { it.content },
 *     listState = listState,
 * ) { selectionState ->
 *     LazyColumn(state = listState) {
 *         itemsIndexed(messages) { index, message ->
 *             Text(
 *                 text = message.content,
 *                 modifier = Modifier.allowTextSelection(
 *                     index, message.content, selectionState
 *                 )
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * @param items The list of data items backing the selectable content.
 * @param textOf A function to extract selectable text from an item.
 * @param listState The [LazyListState] of the [LazyColumn] inside [content].
 *   Must be the **same** instance passed to the inner [LazyColumn] so that
 *   auto-scroll and handle positioning work correctly.
 * @param modifier Modifier applied to the outer container.
 * @param selectionState The state holder for the selection.
 * @param handleColor The color of selection drag handles.
 * @param autoScrollEdgeThreshold Distance from container edges at which
 *   auto-scroll activates during a drag.
 * @param autoScrollSpeed Speed of auto-scrolling near container edges.
 * @param magnifierVerticalOffset How far above the drag point the magnifier
 *   loupe is positioned.
 * @param onCopy Callback invoked after a copy operation completes.
 * @param content Your composable content — typically a [LazyColumn]. Receives
 *   [SelectionState] so each item can apply [allowTextSelection].
 */
@Composable
fun <T> LazySelectionContainer(
    modifier: Modifier = Modifier,
    items: List<T>,
    textOf: (T) -> String,
    listState: LazyListState,
    selectionState: SelectionState = rememberSelectionState(),
    handleColor: Color = MaterialTheme.colorScheme.primary,
    autoScrollEdgeThreshold: Dp = 48.dp,
    autoScrollSpeed: Dp = 6.dp,
    magnifierVerticalOffset: Dp = 64.dp,
    onCopy: ((success: Boolean) -> Unit)? = null,
    content: @Composable (selectionState: SelectionState) -> Unit,
) {
    Box(
        modifier = modifier
            .magnifier(
                sourceCenter = { selectionState.magnifierOffset ?: Offset.Unspecified },
                magnifierCenter = {
                    val offset = selectionState.magnifierOffset
                        ?: return@magnifier Offset.Unspecified
                    offset.copy(y = offset.y - magnifierVerticalOffset.toPx())
                },
                cornerRadius = 24.dp
            )
            .textSelectionGestures(
                selectionState = selectionState,
                items = items,
                textOf = textOf,
                listState = listState,
                autoScrollEdgeThreshold = autoScrollEdgeThreshold,
                autoScrollSpeed = autoScrollSpeed,
            )
    ) {
        content(selectionState)

        SelectionHandlesOverlay(
            selectionState = selectionState,
            items = items,
            textOf = textOf,
            listState = listState,
            handleColor = handleColor,
            autoScrollEdgeThreshold = autoScrollEdgeThreshold,
            autoScrollSpeed = autoScrollSpeed,
            onCopy = onCopy,
        )
    }
}