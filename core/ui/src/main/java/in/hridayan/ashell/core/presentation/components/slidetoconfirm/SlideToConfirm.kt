package `in`.hridayan.ashell.core.presentation.components.slidetoconfirm

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A "slide to unlock" style button for high-stakes user confirmations.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param onConfirm Callback triggered when the slider reaches the end.
 * @param initialText Text displayed in the track before confirmation.
 * @param finalText Text displayed in the track once [confirmed] is true.
 * @param trackHeight The total height of the slider track.
 * @param thumbPadding Spacing between the track edge and the sliding thumb.
 * @param confirmed Whether the action has been completed. Locks the slider and changes the icon.
 * @param enabled Whether the component is interactive.
 * @param containerColor Background color of the slider track.
 * @param contentColor Color of the track text.
 * @param thumbContainerColor Background color of the sliding thumb.
 * @param thumbContentColor Color of the icon inside the thumb.
 * @param disabledAlpha Alpha value applied to colors when [enabled] is false.
 * @param disabledContainerColor Track color when disabled.
 * @param disabledContentColor Text color when disabled.
 * @param disabledThumbContainerColor Thumb color when disabled.
 * @param disabledThumbContentColor Thumb icon color when disabled.
 */
@Composable
fun SlideToConfirm(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    initialText: String = "Slide to confirm",
    finalText: String = "Confirmed",
    trackHeight: Dp = 64.dp,
    thumbPadding: Dp = 6.dp,
    confirmed: Boolean = false,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    thumbContainerColor: Color = MaterialTheme.colorScheme.primary,
    thumbContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledAlpha: Float = 0.38f,
    disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant
        .copy(alpha = disabledAlpha)
        .compositeOver(containerColor),
    disabledContentColor: Color = contentColorFor(disabledContainerColor).copy(alpha = disabledAlpha),
    disabledThumbContainerColor: Color = MaterialTheme.colorScheme.inverseSurface
        .copy(alpha = disabledAlpha)
        .compositeOver(containerColor),
    disabledThumbContentColor: Color = contentColorFor(disabledThumbContainerColor).copy(alpha = disabledAlpha)
) {
    val density = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()

    var trackWidth by remember { mutableFloatStateOf(0f) }
    var thumbOffset by remember { mutableFloatStateOf(0f) }

    val thumbPaddingPx = with(density) { thumbPadding.toPx() }
    val thumbSize = trackHeight - (thumbPadding * 2)
    val thumbSizePx = with(density) { thumbSize.toPx() }

    val maxOffset = (trackWidth - thumbSizePx - (thumbPaddingPx * 2)).coerceAtLeast(0f)

    LaunchedEffect(confirmed, maxOffset) {
        if (confirmed) thumbOffset = maxOffset
    }

    LaunchedEffect(confirmed) {
        if (!confirmed && thumbOffset > 0f) {
            animate(
                initialValue = thumbOffset,
                targetValue = 0f,
                animationSpec = tween(durationMillis = 250)
            ) { value, _ -> thumbOffset = value }
        }
    }

    LaunchedEffect(enabled) {
        if (!enabled && !confirmed && thumbOffset > 0f) {
            animate(
                initialValue = thumbOffset,
                targetValue = 0f,
                animationSpec = tween(durationMillis = 250)
            ) { value, _ -> thumbOffset = value }
        }
    }

    val finalContainerColor = if (enabled) containerColor else disabledContainerColor
    val finalContentColor = if (enabled) contentColor else disabledContentColor
    val finalThumbContainerColor = if (enabled) thumbContainerColor else disabledThumbContainerColor
    val finalThumbContentColor = if (enabled) thumbContentColor else disabledThumbContentColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(RoundedCornerShape(50))
            .background(finalContainerColor)
            .onSizeChanged { trackWidth = it.width.toFloat() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = thumbPadding)
                .height(thumbSize)
                .graphicsLayer {
                    alpha = if (maxOffset > 0f) (thumbOffset / maxOffset).coerceIn(0f, 1f) else 0f
                }
                .layout { measurable, constraints ->
                    val width = (thumbOffset + thumbSizePx).roundToInt()
                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = width,
                            maxWidth = width
                        )
                    )
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
                .clip(RoundedCornerShape(50))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        Text(
            text = if (confirmed) finalText else initialText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = finalContentColor,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha =
                        if (confirmed) 1f else 1f - (if (maxOffset > 0f) (thumbOffset / maxOffset).coerceIn(
                            0f,
                            1f
                        ) else 0f)
                }
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((thumbOffset + thumbPaddingPx).roundToInt(), 0) }
                .size(thumbSize)
                .background(finalThumbContainerColor, CircleShape)
                .pointerInput(confirmed, enabled) {
                    if (confirmed || !enabled) return@pointerInput

                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (enabled && thumbOffset >= maxOffset * 0.85f) {
                                thumbOffset = maxOffset
                                onConfirm()
                            } else {
                                coroutineScope.launch {
                                    animate(
                                        initialValue = thumbOffset,
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 250)
                                    ) { value, _ -> thumbOffset = value }
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            thumbOffset = (thumbOffset + dragAmount).coerceIn(0f, maxOffset)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (confirmed) Icons.Rounded.Check else Icons.Rounded.DoubleArrow,
                contentDescription = "Slide to confirm",
                tint = finalThumbContentColor
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SlideToConfirmPreview() {
    var statusText by remember { mutableStateOf("Waiting for user...") }
    var isConfirmed by remember { mutableStateOf(false) }
    var isEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = statusText)

        SlideToConfirm(
            confirmed = isConfirmed,
            enabled = isEnabled,
            onConfirm = {
                statusText = "Action triggered"
                isConfirmed = true
            }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Button(
                onClick = {
                    isConfirmed = false
                    statusText = "Reset requested"
                }
            ) {
                Text("Reset Slider")
            }

            androidx.compose.material3.Button(
                onClick = { isEnabled = !isEnabled }
            ) {
                Text(if (isEnabled) "Disable Slider" else "Enable Slider")
            }
        }
    }
}
