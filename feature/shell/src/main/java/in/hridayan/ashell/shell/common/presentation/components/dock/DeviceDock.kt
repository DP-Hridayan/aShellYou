package `in`.hridayan.ashell.shell.common.presentation.components.dock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.tooltip.TooltipContent
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.common.presentation.model.DeviceActionId

private val DragThreshold = 24.dp
private val DockEdgePadding = 8.dp
private val HandleWidth = 4.dp
private val HandleHeight = 48.dp
private val HandleActiveWidth = 8.dp
private val HandleActiveHeight = 56.dp
private val HandleTouchWidth = 48.dp
private val HandleTouchHeight = 72.dp

/**
 * Actions for the connected device, docked on the screen's end edge.
 *
 * Collapsed, only a slim handle shows, so the output keeps its width. The handle opens and closes
 * the dock by tap, by a horizontal drag on the handle itself, or by an accessibility action. A free
 * swipe from the screen edge is deliberately not used, because with gesture navigation that swipe
 * belongs to the system's back gesture.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeviceDock(
    actions: List<DeviceActionId>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAction: (DeviceActionId) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(end = DockEdgePadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DockHandle(expanded = expanded, onExpandedChange = onExpandedChange)

        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            VerticalFloatingToolbar(
                expanded = true,
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
            ) {
                actions.forEach { action ->
                    DockButton(action = action, onClick = { onAction(action) })
                }
            }
        }
    }
}

/**
 * A pill that grows while it is pressed or dragged, with one haptic tick as the press lands. It has
 * no ripple: the growth is the feedback, and a ripple would fill the whole rectangular touch target
 * around the slim pill.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DockHandle(expanded: Boolean, onExpandedChange: (Boolean) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isActive = isPressed || isDragged

    HandleHaptics(isActive)

    val sizeSpec = MaterialTheme.motionScheme.fastSpatialSpec<Dp>()
    val width by animateDpAsState(if (isActive) HandleActiveWidth else HandleWidth, sizeSpec)
    val height by animateDpAsState(if (isActive) HandleActiveHeight else HandleHeight, sizeSpec)
    val color by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    )

    Box(
        modifier = Modifier
            .systemGestureExclusion()
            .sizeIn(minWidth = HandleTouchWidth, minHeight = HandleTouchHeight)
            .handleGestures(expanded, onExpandedChange, interactionSource),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .background(color = color, shape = CircleShape)
        )
    }
}

@Composable
private fun HandleHaptics(isActive: Boolean) {
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    LaunchedEffect(isActive) {
        if (isActive && isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }
}

@Composable
private fun Modifier.handleGestures(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    interactionSource: MutableInteractionSource
): Modifier {
    val thresholdPx = with(LocalDensity.current) { DragThreshold.toPx() }
    val towardsCentreSign = if (LocalLayoutDirection.current == LayoutDirection.Ltr) -1f else 1f
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val toggleLabel = stringResource(
        if (expanded) R.string.hide_device_tools else R.string.show_device_tools
    )
    val stateLabel = stringResource(R.string.device_tools)

    return this
        .draggable(
            orientation = Orientation.Horizontal,
            interactionSource = interactionSource,
            state = rememberDraggableState { delta -> dragTotal += delta },
            onDragStarted = { dragTotal = 0f },
            onDragStopped = {
                val towardsCentre = dragTotal * towardsCentreSign
                if (towardsCentre > thresholdPx && !expanded) onExpandedChange(true)
                if (towardsCentre < -thresholdPx && expanded) onExpandedChange(false)
            }
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClickLabel = toggleLabel,
            role = Role.Button,
            onClick = { onExpandedChange(!expanded) }
        )
        .semantics {
            stateDescription = stateLabel
            customActions = listOf(
                CustomAccessibilityAction(toggleLabel) {
                    onExpandedChange(!expanded)
                    true
                }
            )
        }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DockButton(action: DeviceActionId, onClick: () -> Unit) {
    val label = stringResource(action.labelRes())
    val click = withHaptic(HapticFeedbackType.VirtualKey, onClick)

    TooltipContent(text = label) {
        if (action == DeviceActionId.SCREEN) {
            FilledIconButton(
                onClick = click,
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                DockIcon(action = action, label = label)
            }
        } else {
            IconButton(
                onClick = click,
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                DockIcon(action = action, label = label)
            }
        }
    }
}

@Composable
private fun DockIcon(action: DeviceActionId, label: String) {
    when (action) {
        DeviceActionId.SCREEN -> Icon(Icons.Rounded.CastConnected, contentDescription = label)
        DeviceActionId.FILES -> Icon(painterResource(R.drawable.ic_directory), contentDescription = label)
        DeviceActionId.SHARE_OUTPUT -> Icon(painterResource(R.drawable.ic_share), contentDescription = label)
        DeviceActionId.SAVE_OUTPUT -> Icon(painterResource(R.drawable.ic_save), contentDescription = label)
        DeviceActionId.ASK_AI -> Icon(painterResource(R.drawable.ic_help), contentDescription = label)
    }
}

private fun DeviceActionId.labelRes(): Int = when (this) {
    DeviceActionId.SCREEN -> R.string.screen
    DeviceActionId.FILES -> R.string.file_browser
    DeviceActionId.SHARE_OUTPUT -> R.string.share
    DeviceActionId.SAVE_OUTPUT -> R.string.save
    DeviceActionId.ASK_AI -> R.string.adb_agent
}
