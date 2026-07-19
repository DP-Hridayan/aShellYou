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
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Cross-line text selection for [LazyColumn] that avoids the crash caused by
 * [SelectionContainer] holding references to layout nodes that LazyColumn disposes on scroll.
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

/** Which visual handle (post-normalization start or end) the toolbar is
 *  currently pinned to. */
enum class SelectionEnd { START, END }

/** Holds the current selection and per-line layout bookkeeping. */
@Stable
class SelectionState {
    var selection by mutableStateOf<LineSelection?>(null)

    /** Whether the drag handles should be shown. */
    var handlesVisible by mutableStateOf(false)

    /** Whether the copy/select-all popup should be shown. Separate from
     *  [handlesVisible] so the popup can auto-appear when a selection
     *  gesture ends, and auto-hide while a handle is actively being
     *  dragged, without affecting handle visibility itself. */
    var toolbarVisible by mutableStateOf(false)

    /** Which visual handle the popup is anchored to. Defaults to whichever
     *  end the most recent gesture actually moved (so it naturally lands on
     *  the "active" end even if a backward drag flipped start/end); a tap on
     *  a handle pins it there explicitly until the next gesture changes it. */
    var toolbarPinnedEnd by mutableStateOf(SelectionEnd.END)

    internal val lineInfos = mutableMapOf<Int, LineInfo>()

    /** Bumped whenever a line's [LineInfo] is added, updated, or removed --
     *  lineInfos itself is a plain, non-observable map by design (keying
     *  Compose state per-line would be needless overhead), so this is the
     *  actual signal the overlay reads to know it needs to recompute handle
     *  positions. Deliberately not LazyListState's own firstVisibleItemIndex
     *  / firstVisibleItemScrollOffset / layoutInfo: those change on every
     *  scrolled pixel regardless of whether a *selection-relevant* line
     *  moved, which is both wasteful and exactly what triggers Compose's
     *  "frequently changing value" lint. */
    internal var lineInfoVersion by mutableIntStateOf(0)
    internal var containerWindowOrigin: Offset = Offset.Zero
    internal var containerHeightPx: Float = 0f
    internal var containerWidthPx: Float = 0f

    fun clear() {
        selection = null
        handlesVisible = false
        toolbarVisible = false
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
    autoScrollSpeed: Dp = 4.dp,
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
            selectionState.containerWidthPx = coords.size.width.toFloat()
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
                selectionState.toolbarVisible = false

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
                val finalSelection = selectionState.selection
                val hasSelection = finalSelection != null
                selectionState.handlesVisible = hasSelection
                // Previously only set by a handle-drag/tap -- the initial
                // long-press-drag selection never turned this on, so the
                // popup silently never appeared the first time.
                selectionState.toolbarVisible = hasSelection
                if (finalSelection != null) {
                    // endLine/endOffset is literally "wherever the drag last
                    // was" -- if that's still in reading-order after the
                    // anchor, normalizing leaves it as the end; if the user
                    // dragged backwards past the anchor, normalizing swaps it
                    // to become the start. Either way, pin to whichever role
                    // it ended up as, so the popup follows the finger.
                    selectionState.toolbarPinnedEnd =
                        if (finalSelection == finalSelection.normalized()) SelectionEnd.END else SelectionEnd.START
                }
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
    // Reading this subscribes the overlay to actual line-bounds changes
    // (scroll, resize, content changes) so handle positions stay in sync --
    // lineInfos itself is a plain, non-observable map by design.
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
        // Select-all sets the selection to the true first/last line, but if
        // the list is scrolled away from the top, line 0 isn't composed yet
        // so its handle has nothing to position itself against. Scrolling to
        // the top makes the start handle visibly land on the real selection
        // start instead of appearing stuck where the old selection was, and
        // also puts it where the pinned popup will actually be visible.
        scope.launch { listState.scrollToItem(0) }
    }

    // The popup stays pinned to whichever handle it was last set to -- if
    // that specific handle's line scrolls off-screen, the popup hides with
    // it rather than jumping onto the other, still-visible handle, and
    // reappears on its own once that handle is back on screen.
    val toolbarAnchor = when (selectionState.toolbarPinnedEnd) {
        SelectionEnd.START -> startPos
        SelectionEnd.END -> endPos
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
        // Both handles are ALWAYS composed here, even when their position is
        // currently null (line off-screen) -- SelectionHandle itself decides
        // whether to render/accept touches. Composing them conditionally
        // (only when non-null) was what broke dragging: as soon as a line
        // briefly left the visible range mid-drag, the composable holding
        // that gesture's coroutine got disposed, silently killing the drag.
        SelectionHandle(
            positionInContainer = startPos,
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
                findLineOffsetOrNearestEdge(selectionState, global)?.let { (line, offset) ->
                    updateSelectionForVisualHandle(
                        selectionState,
                        isStartVisual = true,
                        line = line,
                        offset = offset
                    )
                }
            },
        )
        SelectionHandle(
            positionInContainer = endPos,
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
                findLineOffsetOrNearestEdge(selectionState, global)?.let { (line, offset) ->
                    updateSelectionForVisualHandle(
                        selectionState,
                        isStartVisual = false,
                        line = line,
                        offset = offset
                    )
                }
            },
        )
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

/** Android-style handle shapes: a circle with one corner left sharp, which
 *  is the tip that actually touches the selected character. The start
 *  handle's tip is its top-right corner (the ball hangs down-left, staying
 *  clear of the selected text to its right); the end handle mirrors it with
 *  the tip at top-left. */
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

@Composable
private fun SelectionHandle(
    positionInContainer: Offset?,
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
    // The visible dot stays small, but the touch target is much bigger --
    // Material's own guidance calls for a 48dp minimum touch target, and
    // with only the dot itself as the hit area, a finger landing a few px
    // off it missed the handle entirely and fell through to the LazyColumn
    // underneath, which started a brand new long-press word-selection right
    // at that point -- which is what made "resuming" a selection by
    // dragging a handle feel like the initial raw-finger-tracking drag
    // instead of a real handle drag.
    val handleVisualSizeDp = 28.dp
    val handleTouchSizeDp = 48.dp
    val coroutineScope = rememberCoroutineScope()
    val handleTouchPx = with(density) { handleTouchSizeDp.toPx() }

    // This composable must never be conditionally removed from the tree
    // based on positionInContainer being null (e.g. its line briefly
    // scrolling out of the visible range during auto-scroll) -- doing that
    // tears down this pointerInput's coroutine mid-gesture, silently
    // killing an in-progress drag. Instead, we keep it alive at its last
    // known position and collapse it to zero size, which naturally blocks
    // *new* touches without affecting a pointer id already being tracked --
    // once a drag has claimed a pointer, Compose keeps routing that pointer
    // to it regardless of the node's current size, the same way a drag
    // already keeps working when your finger moves outside the handle's
    // bounds.
    val lastKnownPosRef = remember { Ref(positionInContainer ?: Offset.Zero) }
    if (positionInContainer != null) lastKnownPosRef.value = positionInContainer
    val effectivePos = lastKnownPosRef.value
    val visible = positionInContainer != null

    // pointerInput(Unit) never restarts, so reads must go through
    // rememberUpdatedState to avoid capturing stale values/callbacks.
    val currentPosition by rememberUpdatedState(effectivePos)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragTo by rememberUpdatedState(onDragTo)

    Box(
        modifier = Modifier
            // The touch box shares its tip corner with the visual ball --
            // top-right for the start handle, top-left for the end handle --
            // so enlarging it for an easier grab doesn't shift where the tip
            // actually is.
            .offset {
                val x = if (isStartVisual) effectivePos.x - handleTouchPx else effectivePos.x
                IntOffset(x.toInt(), effectivePos.y.toInt())
            }
            .size(if (visible) handleTouchSizeDp else 0.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Claim this pointer immediately -- otherwise LazyColumn's
                    // own scroll gesture (which sits underneath, covering the
                    // same area) wins the arbitration and the handle never
                    // receives drag movement at all.
                    down.consume()

                    var isDragging = false
                    // Start exactly at the handle's current tip position, not
                    // at the raw touch point -- every subsequent finger-delta
                    // is added on top of this, so the tip tracks the finger's
                    // *movement* 1:1 while preserving whatever offset existed
                    // between the touch point and the tip at grab time. This
                    // is what makes it feel like dragging the handle, rather
                    // than teleporting the selection to under the finger.
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
                    if (isDragging) currentOnDragEnd() else currentOnTap()
                }
            }
    ) {
        if (visible) {
            Box(
                modifier = Modifier
                    .align(if (isStartVisual) Alignment.TopEnd else Alignment.TopStart)
                    .size(handleVisualSizeDp)
                    .clip(if (isStartVisual) startHandleShape else endHandleShape)
                    .background(color)
            )
        }
    }
}

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
        // Measure the popup at its own natural size, not stretched to fill
        // the container -- fillMaxSize above is only so this Layout's own
        // bounds span the full container to place the popup anywhere in it.
        val placeable = measurables.first().measure(Constraints())

        val fitsAbove = anchorInContainer.y - gapPx - placeable.height >= 0f
        val y = if (fitsAbove) {
            anchorInContainer.y - gapPx - placeable.height
        } else {
            anchorInContainer.y + gapPx
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

/**
 * [isStartVisual] identifies which handle the user is dragging (the one
 * rendered at the normalized start, vs. the one at the normalized end) --
 * not which raw field to write. Those are only the same field when the
 * selection hasn't been flipped by a backward drag; when it has, the visual
 * start handle is actually sitting on the raw *end* field (normalization
 * swapped them for display without touching the underlying selection), so
 * writing straight to raw startLine/startOffset would silently move the
 * wrong endpoint.
 */
private fun updateSelectionForVisualHandle(
    selectionState: SelectionState,
    isStartVisual: Boolean,
    line: Int,
    offset: Int,
) {
    val current = selectionState.selection ?: return
    val swapped = current != current.normalized()
    val writeRawStart = if (isStartVisual) !swapped else swapped
    selectionState.selection =
        if (writeRawStart) current.copy(startLine = line, startOffset = offset)
        else current.copy(endLine = line, endOffset = offset)
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
    // Hide (rather than render at a stale/frozen spot) once the anchor has
    // actually scrolled outside the container's own bounds. This is a plain
    // geometric check against coordinates we've already computed -- earlier
    // this cross-referenced listState's visibleItemsInfo by index instead,
    // which silently assumed `line` equals LazyListItemInfo.index. That only
    // holds for a bare itemsIndexed with nothing else mixed into the list;
    // if it doesn't hold, the check fails for every line, every handle
    // permanently reads as "off-screen", and every touch on it falls
    // through to the LazyColumn's own long-press gesture underneath instead
    // of dragging the handle at all.
    if (local.y < 0f || local.y > selectionState.containerHeightPx) return null
    return local
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
    val bottom =
        state.lineInfos.entries.maxByOrNull { it.value.boundsInWindow.bottom } ?: return null
    return when {
        globalPos.y <= top.value.boundsInWindow.top -> top.key to 0
        globalPos.y >= bottom.value.boundsInWindow.bottom ->
            bottom.key to bottom.value.layoutResult.layoutInput.text.length

        else -> null
    }
}

/** Reads from the source list, not the composed-only cache, so lines that
 *  scrolled offscreen during the drag still copy correctly. */
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