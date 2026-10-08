package `in`.hridayan.ashell.mirror.presentation.components.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.DeviceKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val BallTouchSize = 48.dp
private val BallVisualSize = 40.dp
private val ControlsMargin = 8.dp
private val BallElevation = 6.dp
private val PanelGap = 8.dp
private val PanelPadding = 12.dp
private val PanelSpacing = 8.dp
private val PanelElevation = 3.dp
private val PanelBorder = 1.dp
private const val BALL_IDLE_ALPHA = 0.35f
private const val PANEL_ALPHA = 0.85f
private const val DEFAULT_BALL_HEIGHT_FRACTION = 0.4f
private const val HALF = 0.5f
private val BallIdleDelay = 2_500.milliseconds
private val PanelIdleDelay = 10.seconds

/** Everything the fullscreen controls need to act, grouped so the composables stay small. */
data class FullscreenControlActions(
    val onTogglePanel: () -> Unit,
    val onDismissPanel: () -> Unit,
    val onKey: (DeviceKey) -> Unit,
    val onExpandQuickSettings: () -> Unit,
    val onOpenQuality: () -> Unit,
    val onExitFullscreen: () -> Unit,
    val onLeave: () -> Unit
)

/**
 * The only on-screen controls while the mirror is fullscreen: a draggable floating ball and the
 * panel it opens.
 *
 * In fullscreen every touch on the video and this phone's Back go to the device, so this ball is
 * how the user leaves. It is therefore never hidden: it fades to a floor opacity when idle, can't be
 * dragged off-screen or under a display cutout, and snaps to the nearest side edge. While the panel
 * is open, a tap anywhere outside it only closes the panel and never reaches the device.
 */
@Composable
fun FullscreenControls(
    isPanelOpen: Boolean,
    keysEnabled: Boolean,
    isWide: Boolean,
    actions: FullscreenControlActions,
    modifier: Modifier = Modifier
) {
    var isBallAtEnd by rememberSaveable { mutableStateOf(true) }
    var ballHeightFraction by rememberSaveable { mutableFloatStateOf(DEFAULT_BALL_HEIGHT_FRACTION) }
    var panelUses by remember { mutableIntStateOf(0) }
    val dismissPanel by rememberUpdatedState(actions.onDismissPanel)

    LaunchedEffect(isPanelOpen, panelUses) {
        if (!isPanelOpen) return@LaunchedEffect
        delay(PanelIdleDelay)
        actions.onDismissPanel()
    }

    if (isPanelOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { dismissPanel() } }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(ControlsMargin)
    ) {
        val ballPx = with(LocalDensity.current) { BallTouchSize.toPx() }
        val maxX = (constraints.maxWidth - ballPx).coerceAtLeast(0f)
        val maxY = (constraints.maxHeight - ballPx).coerceAtLeast(0f)

        AnimatedVisibility(visible = isPanelOpen, enter = fadeIn(), exit = fadeOut()) {
            ControlPanel(
                anchor = PanelAnchor(
                    isBallAtEnd = isBallAtEnd,
                    ballCentreY = ballHeightFraction * maxY + ballPx * HALF,
                    ballSizePx = ballPx,
                    area = IntSize(constraints.maxWidth, constraints.maxHeight)
                ),
                keysEnabled = keysEnabled,
                isWide = isWide,
                actions = actions,
                onUse = { panelUses++ }
            )
        }

        FloatingBall(
            isPanelOpen = isPanelOpen,
            placement = BallPlacement(isBallAtEnd, ballHeightFraction, maxX, maxY),
            onToggle = actions.onTogglePanel,
            onSettled = { atEnd, fraction ->
                isBallAtEnd = atEnd
                ballHeightFraction = fraction
            }
        )
    }
}

/**
 * Taps and drags are told apart in one gesture handler: a press that moves past the touch slop is a
 * drag and never also counts as a tap, which separate click and drag handlers on nested nodes can't
 * guarantee.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FloatingBall(
    isPanelOpen: Boolean,
    placement: BallPlacement,
    onToggle: () -> Unit,
    onSettled: (isAtEnd: Boolean, heightFraction: Float) -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    val label = stringResource(R.string.mirror_controls)
    val isAtEnd = placement.isAtEnd
    val heightFraction = placement.heightFraction
    val maxX = placement.maxX
    val maxY = placement.maxY
    val x = remember { Animatable(if (isAtEnd) maxX else 0f) }
    var y by remember { mutableFloatStateOf(heightFraction * maxY) }
    var isDragging by remember { mutableStateOf(false) }
    var touches by remember { mutableIntStateOf(0) }
    var isIdle by remember { mutableStateOf(false) }

    LaunchedEffect(isAtEnd, maxX, maxY, heightFraction) {
        if (isDragging) return@LaunchedEffect
        x.snapTo(if (isAtEnd) maxX else 0f)
        y = heightFraction * maxY
    }
    LaunchedEffect(touches, isPanelOpen) {
        isIdle = false
        if (isPanelOpen) return@LaunchedEffect
        delay(BallIdleDelay)
        isIdle = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (isIdle && !isDragging) BALL_IDLE_ALPHA else 1f,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()
    )

    val currentOnToggle by rememberUpdatedState(onToggle)
    val currentOnSettled by rememberUpdatedState(onSettled)
    val toggle by rememberUpdatedState {
        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        currentOnToggle()
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(x.value.roundToInt(), y.roundToInt()) }
            .size(BallTouchSize)
            .alpha(alpha)
            .systemGestureExclusion()
            .semantics {
                role = Role.Button
                contentDescription = label
                onClick(label) {
                    onToggle()
                    true
                }
            }
            .pointerInput(maxX, maxY) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    touches++
                    val slopChange = awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                    if (slopChange == null) {
                        if (currentEvent.changes.none { it.pressed }) toggle()
                        return@awaitEachGesture
                    }

                    isDragging = true
                    drag(slopChange.id) { change ->
                        val delta = change.positionChange()
                        scope.launch { x.snapTo((x.value + delta.x).coerceIn(0f, maxX)) }
                        y = (y + delta.y).coerceIn(0f, maxY)
                        change.consume()
                    }
                    isDragging = false
                    touches++

                    val atEnd = x.value > maxX * HALF
                    scope.launch { x.animateTo(if (atEnd) maxX else 0f) }
                    currentOnSettled(atEnd, if (maxY > 0f) y / maxY else 0f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(BallVisualSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shadowElevation = BallElevation
        ) {
            Box(contentAlignment = Alignment.Center) {
                Crossfade(targetState = isPanelOpen) { open ->
                    Icon(imageVector = if (open) Icons.Rounded.Close else Icons.Rounded.Tune, contentDescription = null)
                }
            }
        }
    }
}

/**
 * Where the ball rests: on the end or start edge, at a fraction of the available height, within an
 * area whose top-left corner is the origin and whose far corner is ([maxX], [maxY]).
 */
private data class BallPlacement(
    val isAtEnd: Boolean,
    val heightFraction: Float,
    val maxX: Float,
    val maxY: Float
)

/**
 * Where the panel sits: beside the ball, on the side facing the screen's centre, vertically centred
 * on the ball and kept inside [area].
 */
private data class PanelAnchor(
    val isBallAtEnd: Boolean,
    val ballCentreY: Float,
    val ballSizePx: Float,
    val area: IntSize
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ControlPanel(
    anchor: PanelAnchor,
    keysEnabled: Boolean,
    isWide: Boolean,
    actions: FullscreenControlActions,
    onUse: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val gapPx = with(LocalDensity.current) { PanelGap.toPx() }

    Surface(
        modifier = Modifier
            .offset { panelOffset(anchor, size, gapPx) }
            .onSizeChanged { size = it },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = PANEL_ALPHA),
        border = BorderStroke(PanelBorder, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = PanelElevation
    ) {
        Column(
            modifier = Modifier.padding(PanelPadding),
            verticalArrangement = Arrangement.spacedBy(PanelSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PanelHeader(onExitFullscreen = actions.onExitFullscreen, onLeave = actions.onLeave)
            DeviceKeyBar(
                vertical = isWide,
                enabled = keysEnabled,
                onKey = { key ->
                    onUse()
                    actions.onKey(key)
                },
                onExpandQuickSettings = {
                    onUse()
                    actions.onExpandQuickSettings()
                },
                onOpenQuality = actions.onOpenQuality,
                contained = false
            )
        }
    }
}

private fun panelOffset(anchor: PanelAnchor, size: IntSize, gapPx: Float): IntOffset {
    val x = if (anchor.isBallAtEnd) {
        anchor.area.width - anchor.ballSizePx - gapPx - size.width
    } else {
        anchor.ballSizePx + gapPx
    }
    val maxY = (anchor.area.height - size.height).coerceAtLeast(0).toFloat()
    val y = (anchor.ballCentreY - size.height * HALF).coerceIn(0f, maxY)
    return IntOffset(x.roundToInt().coerceAtLeast(0), y.roundToInt())
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PanelHeader(onExitFullscreen: () -> Unit, onLeave: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(PanelSpacing)) {
        FilledTonalButton(onClick = withHaptic(block = onExitFullscreen), shapes = ButtonDefaults.shapes()) {
            Icon(
                imageVector = Icons.Rounded.FullscreenExit,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.exit_full_screen))
        }
        TextButton(onClick = withHaptic(block = onLeave), shapes = ButtonDefaults.shapes()) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.leave_mirror))
        }
    }
}
