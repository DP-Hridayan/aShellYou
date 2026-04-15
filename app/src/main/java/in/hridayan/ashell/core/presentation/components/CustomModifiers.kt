package `in`.hridayan.ashell.core.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dashedBorder(
    color: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    cornerRadius: Dp = 0.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 6.dp
) = this.then(
    Modifier.drawBehind {
        val stroke = strokeWidth.toPx()
        val dash = dashLength.toPx()
        val gap = gapLength.toPx()

        drawRoundRect(
            color = color,
            size = size,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(dash, gap)
                )
            )
        )
    }
)