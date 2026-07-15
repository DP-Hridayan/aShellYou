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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
fun SlideToConfirm(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    initialText: String = "Slide to confirm",
    finalText: String = "Confirmed",
    trackHeight: Dp = 64.dp,
    thumbPadding: Dp = 6.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    thumbContainerColor: Color = MaterialTheme.colorScheme.primary,
    thumbContentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val coroutineScope = rememberCoroutineScope()
    var trackWidth by remember { mutableFloatStateOf(0f) }
    var thumbOffset by remember { mutableFloatStateOf(0f) }
    var isConfirmed by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    val trackHeightPx = with(density) { trackHeight.toPx() }
    val thumbPaddingPx = with(density) { thumbPadding.toPx() }

    val thumbSize = trackHeight - (thumbPadding * 2)
    val thumbSizePx = with(density) { thumbSize.toPx() }

    val maxOffset = (trackWidth - thumbSizePx - (thumbPaddingPx * 2)).coerceAtLeast(0f)
    val progress = if (maxOffset > 0f) (thumbOffset / maxOffset).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .onSizeChanged { trackWidth = it.width.toFloat() }
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = thumbPadding)
                    .width(with(density) { (thumbOffset + thumbSizePx).toDp() })
                    .height(thumbSize)
                    .alpha(progress)
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
        }

        Text(
            text = if (isConfirmed) finalText else initialText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(if (isConfirmed) 1f else 1f - progress)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((thumbOffset + thumbPaddingPx).roundToInt(), 0) }
                .size(thumbSize)
                .background(thumbContainerColor, CircleShape)
                .pointerInput(isConfirmed) {
                    if (isConfirmed) return@pointerInput

                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (thumbOffset >= maxOffset * 0.85f) {
                                isConfirmed = true
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
                imageVector = if (isConfirmed) Icons.Rounded.Check else Icons.Rounded.DoubleArrow,
                contentDescription = "Slide to confirm",
                tint = thumbContentColor
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SlideToConfirmPreview() {
    var statusText by remember { mutableStateOf("Waiting for user...") }

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = statusText)

        SlideToConfirm(
            onConfirm = {
                statusText = "Action triggered"
            }
        )
    }
}