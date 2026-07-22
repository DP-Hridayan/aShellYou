package `in`.hridayan.ashell.core.presentation.components.selectioncontainer

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.IntOffset
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
                        }
                        delay(16.milliseconds)
                    }
                }

                drag(longPress.id) { change ->
                    change.consume()
                    pointerPosRef.value = change.position
                }

                loopJob.cancel()
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
    autoScrollSpeed: Dp = 2.dp,
    onCopy: ((success: Boolean) -> Unit)? = null,
) {
    // Observe version to sync with layout changes
    selectionState.lineInfoVersion

    val sel = selectionState.selection
    if (sel == null || !selectionState.handlesVisible) return
    val normalized = sel.normalized()

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val edgeThresholdPx = with(density) { autoScrollEdgeThreshold.toPx() }
    val scrollSpeedPx = with(density) { autoScrollSpeed.toPx() }

    val startPos = computeHandleOffset(selectionState, normalized.startLine, normalized.startOffset)
    val endPos = computeHandleOffset(selectionState, normalized.endLine, normalized.endOffset)

    fun doCopy() {
        scope.launch {
            val current = selectionState.selection?.normalized() ?: normalized
            copySelection(clipboard, items, textOf, current, onCopy)
        }
        selectionState.toolbarVisible = false
    }

    fun doSelectAll() {
        selectAll(selectionState, items)
        selectionState.toolbarVisible = false
        selectionState.toolbarPinnedEnd = SelectionEnd.START
        scope.launch { listState.scrollToItem(0) }
    }

    val toolbarAnchor = when (selectionState.toolbarPinnedEnd) {
        SelectionEnd.START -> startPos
        SelectionEnd.END -> endPos
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
        startPos?.let { pos ->
            SelectionHandle(
                positionInContainer = pos,
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
                onDragStart = { selectionState.toolbarVisible = false },
                onDragEnd = {
                    selectionState.toolbarPinnedEnd = SelectionEnd.START
                    selectionState.toolbarVisible = true
                },
                onDragTo = { rawPos ->
                    val global = rawPos + selectionState.containerWindowOrigin
                    val result = findLineOffsetOrNearestEdge(selectionState, global)
                    result?.let { (line, offset) ->
                        updateSelectionForVisualHandle(
                            selectionState,
                            isStartVisual = true,
                            line = line,
                            offset = offset
                        )
                    }
                },
            )
        }
        endPos?.let { pos ->
            SelectionHandle(
                positionInContainer = pos,
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
                onDragStart = { selectionState.toolbarVisible = false },
                onDragEnd = {
                    selectionState.toolbarPinnedEnd = SelectionEnd.END
                    selectionState.toolbarVisible = true
                },
                onDragTo = { rawPos ->
                    val global = rawPos + selectionState.containerWindowOrigin
                    val result = findLineOffsetOrNearestEdge(selectionState, global)
                    result?.let { (line, offset) ->
                        updateSelectionForVisualHandle(
                            selectionState,
                            isStartVisual = false,
                            line = line,
                            offset = offset
                        )
                    }
                },
            )
        }
        if (selectionState.toolbarVisible && toolbarAnchor != null) {
            SelectionToolbar(
                anchorInContainer = toolbarAnchor,
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
    positionInContainer: Offset,
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

    val currentPosition by rememberUpdatedState(positionInContainer)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragTo by rememberUpdatedState(onDragTo)

    Box(
        modifier = Modifier
            .offset {
                val x =
                    if (isStartVisual) positionInContainer.x - handleTouchPx else positionInContainer.x
                IntOffset(x.toInt(), positionInContainer.y.toInt())
            }
            .size(handleTouchSizeDp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()

                    var isDragging = false
                    var raw = currentPosition
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
    anchorInContainer: Offset,
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
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Row {
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
 * A drop-in replacement for [LazyColumn] that provides cross-line text selection support.
 * This component handles selection gestures, highlights, handles, and a toolbar
 * automatically, while avoiding crashes associated with standard [SelectionContainer]
 * inside lazy layouts.
 *
 * Example usage:
 * ```kotlin
 * LazySelectionContainer(
 *     items = messages,
 *     textOf = { it.content },
 * ) { selectionState ->
 *     itemsIndexed(messages) { index, message ->
 *         Text(
 *             text = message.content,
 *             modifier = Modifier.allowTextSelection(index, message.content, selectionState)
 *         )
 *     }
 * }
 * ```
 *
 * @param items The list of data items.
 * @param textOf A function to extract selectable text from an item.
 * @param modifier The modifier to be applied to the container.
 * @param listState The state of the underlying [LazyColumn].
 * @param selectionState The state holder for the selection.
 * @param contentPadding Padding to be applied around the [LazyColumn].
 * @param reverseLayout Whether to reverse the layout direction.
 * @param verticalArrangement Vertical arrangement for the items.
 * @param horizontalAlignment Horizontal alignment for the items.
 * @param flingBehavior Fling behavior for scrolling.
 * @param userScrollEnabled Whether user-initiated scrolling is enabled.
 * @param handleColor The color of selection handles.
 * @param autoScrollEdgeThreshold Threshold for triggering edge auto-scroll.
 * @param autoScrollSpeed Speed of auto-scrolling.
 * @param onCopy Callback invoked after a copy operation completes.
 * @param content The DSL for defining list items, providing access to [SelectionState].
 */
@Composable
fun <T> LazySelectionContainer(
    items: List<T>,
    textOf: (T) -> String,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    selectionState: SelectionState = rememberSelectionState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    handleColor: Color = MaterialTheme.colorScheme.primary,
    autoScrollEdgeThreshold: Dp = 48.dp,
    autoScrollSpeed: Dp = 2.dp,
    onCopy: ((success: Boolean) -> Unit)? = null,
    content: LazyListScope.(selectionState: SelectionState) -> Unit,
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            modifier = Modifier
                .textSelectionGestures(
                    selectionState = selectionState,
                    items = items,
                    textOf = textOf,
                    listState = listState,
                    autoScrollEdgeThreshold = autoScrollEdgeThreshold,
                    autoScrollSpeed = autoScrollSpeed
                ),
        ) {
            content(selectionState)
        }

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