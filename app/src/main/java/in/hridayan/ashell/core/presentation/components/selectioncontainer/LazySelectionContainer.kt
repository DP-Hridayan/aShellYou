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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Cross-line text selection for [LazyColumn] that avoids the crash caused by
 * [androidx.compose.foundation.text.selection.SelectionContainer] holding
 * references to layout nodes that LazyColumn disposes on scroll.
 *
 * Use [LazySelectionContainer] for a drop-in replacement of [LazyColumn], or
 * compose [textSelectionGestures], [allowTextSelection] and
 * [SelectionHandlesOverlay] directly for custom layouts.
 */

/** A selection anchored at (startLine, startOffset) through (endLine, endOffset). */
data class LineSelection(
    val startLine: Int,
    val startOffset: Int,
    val endLine: Int,
    val endOffset: Int,
) {
    /** Returns this selection with start/end ordered so start precedes end. */
    fun normalized(): LineSelection =
        if (startLine < endLine || (startLine == endLine && startOffset <= endOffset)) this
        else LineSelection(endLine, endOffset, startLine, startOffset)
}

internal data class LineInfo(val boundsInWindow: Rect, val layoutResult: TextLayoutResult)

/** Mutable holder for values read only outside Compose's observed scopes. */
private class Ref<T>(var value: T)

private enum class HandleRole { START, END }

/** Holds the current selection and per-line layout bookkeeping. */
@Stable
class SelectionState {
    var selection by mutableStateOf<LineSelection?>(null)

    /** Whether the drag handles + popup should be shown. */
    var handlesVisible by mutableStateOf(false)

    internal val lineInfos = mutableMapOf<Int, LineInfo>()
    internal var containerWindowOrigin: Offset = Offset.Zero
    internal var containerHeightPx: Float = 0f

    fun clear() {
        selection = null
        handlesVisible = false
    }
}

@Composable
fun rememberSelectionState(): SelectionState = remember { SelectionState() }

/**
 * Long-press to start a selection (haptic feedback + snap to the word under
 * the touch), drag to extend it with auto-scroll near the edges, tap
 * elsewhere to dismiss. Attach to a [LazyColumn]'s `modifier`.
 *
 * @param textOf extracts the selectable text from an item.
 */
fun <T> Modifier.textSelectionGestures(
    selectionState: SelectionState,
    items: List<T>,
    textOf: (T) -> String,
    listState: LazyListState,
    autoScrollEdgeThreshold: Dp = 48.dp,
    autoScrollSpeed: Dp = 2.dp,
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val edgeThresholdPx = with(density) { autoScrollEdgeThreshold.toPx() }
    val scrollSpeedPx = with(density) { autoScrollSpeed.toPx() }

    // rememberUpdatedState: pointerInput is keyed on Unit so the gesture
    // coroutine never restarts (which would cancel an in-progress drag)
    // when new items arrive, e.g. streaming output.
    val currentItems by rememberUpdatedState(items)
    val currentTextOf by rememberUpdatedState(textOf)

    this
        .onGloballyPositioned { coords ->
            selectionState.containerWindowOrigin = coords.boundsInWindow().topLeft
            selectionState.containerHeightPx = coords.size.height.toFloat()
        }
        .pointerInput(Unit) {
            val touchSlop = viewConfiguration.touchSlop
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (down.isConsumed) {
                    // Already claimed by something on top (e.g. a selection
                    // handle) -- don't also start our own gesture for it.
                    return@awaitEachGesture
                }

                val longPress = awaitLongPressOrCancellation(down.id)

                if (longPress == null) {
                    // Either a quick tap or a swipe -- awaitLongPressOrCancellation
                    // can't tell us which. Read raw pointer events ourselves rather
                    // than using drag() here: drag() treats an already-consumed
                    // change as cancellation, and LazyColumn legitimately consumes
                    // the move to scroll, which would make a real swipe look like
                    // "no movement" and wrongly dismiss the selection.
                    var moved = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.changedToUpIgnoreConsumed()) break
                        val dx = change.position.x - down.position.x
                        val dy = change.position.y - down.position.y
                        if (kotlin.math.hypot(dx, dy) > touchSlop) moved = true
                        // Deliberately not consumed -- let LazyColumn scroll either way.
                    }
                    if (!moved && selectionState.selection != null) {
                        selectionState.selection = null
                        selectionState.handlesVisible = false
                    }
                    return@awaitEachGesture
                }

                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectionState.handlesVisible = false

                val startGlobal = longPress.position + selectionState.containerWindowOrigin
                val start = findLineOffset(selectionState, startGlobal) ?: return@awaitEachGesture
                val lineText = currentTextOf(currentItems[start.first])
                val wordRange = wordBoundsAt(lineText, start.second)
                selectionState.selection =
                    LineSelection(start.first, wordRange.first, start.first, wordRange.last)

                // Single loop drives both auto-scroll and selection-endpoint
                // updates from the LAST KNOWN pointer position, on a timer --
                // not gated on new touch events, so the endpoint keeps tracking
                // (and extends into newly auto-scrolled-in content) even while
                // the finger itself is stationary.
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
                selectionState.handlesVisible = selectionState.selection != null
            }
        }
}

/**
 * Registers a line's layout for selection and draws its highlight. Attach
 * to each line's `Text`/`BasicText` inside a [LazyColumn]'s items.
 *
 * @param style must match the style the text is actually rendered with —
 * this measures its own copy of [text] to compute hit-testing and highlight
 * paths, so a mismatch will visibly misalign both.
 */
fun Modifier.allowTextSelection(
    index: Int,
    text: String,
    selectionState: SelectionState,
    style: TextStyle = TextStyle.Default,
    highlightColor: Color = Color(0x663399FF),
): Modifier = composed {
    val textMeasurer = rememberTextMeasurer()
    val windowBounds = remember(index) { Ref(Rect.Zero) }
    val measuredWidth = remember(index) { Ref(-1) }
    var cachedLayout by remember(index) { mutableStateOf<TextLayoutResult?>(null) }

    DisposableEffect(index) {
        onDispose { selectionState.lineInfos.remove(index) }
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
            cachedLayout?.let { selectionState.lineInfos[index] = LineInfo(windowBounds.value, it) }
        }
        .drawBehind {
            val result = cachedLayout ?: return@drawBehind
            val sel = selectionState.selection?.normalized() ?: return@drawBehind
            if (index < sel.startLine || index > sel.endLine) return@drawBehind

            val lineStart = (if (index == sel.startLine) sel.startOffset else 0).coerceIn(0, text.length)
            val lineEnd = (if (index == sel.endLine) sel.endOffset else text.length).coerceIn(0, text.length)
            if (lineStart == lineEnd) return@drawBehind

            val path = result.getPathForRange(minOf(lineStart, lineEnd), maxOf(lineStart, lineEnd))
            drawPath(path, color = highlightColor)
        }
}

/**
 * Draggable start/end selection handles with a tap-to-open Copy/Select-all
 * popup. Place as a sibling of the [LazyColumn] inside the same [Box].
 *
 * @param onCopy optional callback reporting whether the Copy action
 * succeeded, e.g. to show a toast.
 */
@Composable
fun <T> SelectionHandlesOverlay(
    selectionState: SelectionState,
    items: List<T>,
    textOf: (T) -> String,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    handleColor: Color = Color(0xFF3399FF),
    autoScrollEdgeThreshold: Dp = 48.dp,
    autoScrollSpeed: Dp = 2.dp,
    onCopy: ((success: Boolean) -> Unit)? = null,
) {
    // Reading these subscribes this composable to scroll changes, so handle
    // positions (derived from selectionState.lineInfos, which is a plain,
    // non-observable map by design) stay in sync as the list scrolls --
    // otherwise a manual scroll leaves handles rendered at a stale spot.
    listState.firstVisibleItemIndex
    listState.firstVisibleItemScrollOffset

    val sel = selectionState.selection
    if (sel == null || !selectionState.handlesVisible) return
    val normalized = sel.normalized()

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val edgeThresholdPx = with(density) { autoScrollEdgeThreshold.toPx() }
    val scrollSpeedPx = with(density) { autoScrollSpeed.toPx() }

    var openMenuFor by remember { mutableStateOf<HandleRole?>(null) }

    val startPos = computeHandleOffset(selectionState, normalized.startLine, normalized.startOffset)
    val endPos = computeHandleOffset(selectionState, normalized.endLine, normalized.endOffset)

    fun doCopy() {
        scope.launch {
            val current = selectionState.selection?.normalized() ?: normalized
            copySelection(clipboard, items, textOf, current, onCopy)
        }
        openMenuFor = null
    }

    fun doSelectAll() {
        selectAll(selectionState, items)
        openMenuFor = null
    }

    Box(modifier = modifier.fillMaxSize().zIndex(10f)) {
        startPos?.let { pos ->
            SelectionHandle(
                positionInContainer = pos,
                color = handleColor,
                selectionState = selectionState,
                listState = listState,
                edgeThresholdPx = edgeThresholdPx,
                scrollSpeedPx = scrollSpeedPx,
                onTap = { openMenuFor = if (openMenuFor == HandleRole.START) null else HandleRole.START },
                onDragStart = { openMenuFor = null },
                onDragTo = { rawPos ->
                    val global = rawPos + selectionState.containerWindowOrigin
                    findLineOffsetOrNearestEdge(selectionState, global)?.let { (line, offset) ->
                        updateSelectionForHandle(selectionState, isStart = true, line = line, offset = offset)
                    }
                },
            )
            if (openMenuFor == HandleRole.START) {
                SelectionToolbar(anchorInContainer = pos, onCopy = ::doCopy, onSelectAll = ::doSelectAll)
            }
        }
        endPos?.let { pos ->
            SelectionHandle(
                positionInContainer = pos,
                color = handleColor,
                selectionState = selectionState,
                listState = listState,
                edgeThresholdPx = edgeThresholdPx,
                scrollSpeedPx = scrollSpeedPx,
                onTap = { openMenuFor = if (openMenuFor == HandleRole.END) null else HandleRole.END },
                onDragStart = { openMenuFor = null },
                onDragTo = { rawPos ->
                    val global = rawPos + selectionState.containerWindowOrigin
                    findLineOffsetOrNearestEdge(selectionState, global)?.let { (line, offset) ->
                        updateSelectionForHandle(selectionState, isStart = false, line = line, offset = offset)
                    }
                },
            )
            if (openMenuFor == HandleRole.END) {
                SelectionToolbar(anchorInContainer = pos, onCopy = ::doCopy, onSelectAll = ::doSelectAll)
            }
        }
    }
}

@Composable
private fun SelectionHandle(
    positionInContainer: Offset,
    color: Color,
    selectionState: SelectionState,
    listState: LazyListState,
    edgeThresholdPx: Float,
    scrollSpeedPx: Float,
    onTap: () -> Unit,
    onDragStart: () -> Unit,
    onDragTo: (rawPositionInContainer: Offset) -> Unit,
) {
    val density = LocalDensity.current
    val handleSizeDp = 24.dp
    val coroutineScope = rememberCoroutineScope()
    val handleSizePx = with(density) { handleSizeDp.toPx() }

    // pointerInput(Unit) never restarts, so reads must go through
    // rememberUpdatedState to avoid capturing stale values/callbacks.
    val currentPosition by rememberUpdatedState(positionInContainer)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragTo by rememberUpdatedState(onDragTo)

    Box(
        modifier = Modifier
            .offset {
                IntOffset((positionInContainer.x - handleSizePx / 2).toInt(), positionInContainer.y.toInt())
            }
            .size(handleSizeDp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Claim this pointer immediately -- otherwise LazyColumn's
                    // own scroll gesture (which sits underneath, covering the
                    // same area) wins the arbitration and the handle never
                    // receives drag movement at all.
                    down.consume()

                    var isDragging = false
                    var raw = currentPosition
                    val pointerPosRef = Ref(raw)

                    // Same as the main gesture: a timer-driven loop, not gated
                    // on new touch events, so auto-scroll and selection updates
                    // both keep tracking a stationary finger during auto-scroll.
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

                    drag(down.id) { change ->
                        change.consume()
                        if (!isDragging) {
                            isDragging = true
                            currentOnDragStart()
                        }
                        raw += change.positionChange()
                        pointerPosRef.value = raw
                    }

                    loopJob.cancel()
                    if (!isDragging) currentOnTap()
                }
            }
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun SelectionToolbar(anchorInContainer: Offset, onCopy: () -> Unit, onSelectAll: () -> Unit) {
    val density = LocalDensity.current
    val toolbarHeightPx = with(density) { 48.dp.toPx() }
    Box(
        modifier = Modifier.offset {
            IntOffset(anchorInContainer.x.toInt(), (anchorInContainer.y - toolbarHeightPx).toInt())
        }
    ) {
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 4.dp, shadowElevation = 4.dp) {
            Row {
                TextButton(onClick = onCopy) { Text("Copy") }
                TextButton(onClick = onSelectAll) { Text("Select all") }
            }
        }
    }
}

/** Word under [offset], or the single character there if it's not a word. */
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

private fun updateSelectionForHandle(selectionState: SelectionState, isStart: Boolean, line: Int, offset: Int) {
    val current = selectionState.selection ?: return
    selectionState.selection = if (isStart) current.copy(startLine = line, startOffset = offset)
    else current.copy(endLine = line, endOffset = offset)
}

private fun computeHandleOffset(selectionState: SelectionState, line: Int, charOffset: Int): Offset? {
    val info = selectionState.lineInfos[line] ?: return null
    val textLength = info.layoutResult.layoutInput.text.length
    val cursorRect = info.layoutResult.getCursorRect(charOffset.coerceIn(0, textLength))
    val windowPoint = info.boundsInWindow.topLeft + Offset(cursorRect.left, cursorRect.bottom)
    return windowPoint - selectionState.containerWindowOrigin
}

private fun <T> selectAll(selectionState: SelectionState, items: List<T>) {
    if (items.isEmpty()) return
    // endOffset is clamped by allowTextSelection/buildSelectedText per-line.
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

/** Scans only currently-composed lines — bounded to the visible item count. */
private fun findLineOffset(state: SelectionState, globalPos: Offset): Pair<Int, Int>? {
    for ((index, info) in state.lineInfos) {
        val b = info.boundsInWindow
        if (globalPos.y in b.top..b.bottom) {
            val local = Offset(
                x = (globalPos.x - b.left).coerceIn(0f, b.width),
                y = (globalPos.y - b.top).coerceIn(0f, b.height)
            )
            return index to info.layoutResult.getOffsetForPosition(local)
        }
    }
    return null
}

/** Like [findLineOffset], but if the position is beyond the currently
 *  composed content (e.g. the pointer sits outside the container during
 *  auto-scroll), snaps to the start of the topmost or the end of the
 *  bottommost composed line -- so a drag held past the edge keeps
 *  extending the selection into content as it scrolls in. */
private fun findLineOffsetOrNearestEdge(state: SelectionState, globalPos: Offset): Pair<Int, Int>? {
    findLineOffset(state, globalPos)?.let { return it }
    if (state.lineInfos.isEmpty()) return null
    val top = state.lineInfos.entries.minByOrNull { it.value.boundsInWindow.top } ?: return null
    val bottom = state.lineInfos.entries.maxByOrNull { it.value.boundsInWindow.bottom } ?: return null
    return when {
        globalPos.y <= top.value.boundsInWindow.top -> top.key to 0
        globalPos.y >= bottom.value.boundsInWindow.bottom ->
            bottom.key to bottom.value.layoutResult.layoutInput.text.length
        else -> null
    }
}

/** Reads from the source list, not the composed-only cache, so lines that
 *  scrolled offscreen during the drag still copy correctly. */
private fun <T> buildSelectedText(items: List<T>, textOf: (T) -> String, sel: LineSelection): String {
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
 * Drop-in replacement for [LazyColumn] with built-in text selection: gestures,
 * highlighting, drag handles and the copy/select-all popup are all wired up
 * automatically. [content] receives [SelectionState] so each item can apply
 * [allowTextSelection] — that one call per item can't be avoided, since each
 * item is its own layout node and a modifier can't reach into a sibling's scope.
 *
 * ```
 * LazySelectionContainer(
 *     items = lines,
 *     textOf = { it.text },
 *     modifier = Modifier.fillMaxSize(),
 * ) { selectionState ->
 *     itemsIndexed(lines, key = { i, _ -> i }) { index, line ->
 *         Text(
 *             text = line.text,
 *             modifier = Modifier.allowTextSelection(index, line.text, selectionState, style)
 *         )
 *     }
 * }
 * ```
 *
 * @param textOf extracts the selectable text from an item.
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
    handleColor: Color = Color(0xFF3399FF),
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
                .fillMaxSize()
                .textSelectionGestures(
                    selectionState, items, textOf, listState,
                    autoScrollEdgeThreshold, autoScrollSpeed
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
