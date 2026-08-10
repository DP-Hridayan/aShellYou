package `in`.hridayan.ashell.core.presentation.components.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ChatBubbleShape(
    private val cornerRadius: Dp = 16.dp,
    private val tailWidth: Dp = 12.dp,
    private val tailHeight: Dp = 12.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val radius = with(density) { cornerRadius.toPx() }
            val tailW = with(density) { tailWidth.toPx() }
            val tailH = with(density) { tailHeight.toPx() }
            val w = size.width
            val h = size.height
            val bodyH = h - tailH

            moveTo(radius, 0f)
            lineTo(w - radius, 0f)

            // Top right corner
            arcTo(
                rect = Rect(w - 2 * radius, 0f, w, 2 * radius),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Right edge down to the sharp tip of the tail at (w, h)
            lineTo(x = w, y = h)

            // Concave curve swooping from the tip smoothly into the bottom edge
            quadraticTo(
                x1 = w,
                y1 = bodyH, // Control point for perfect tangent transition
                x2 = w - tailW,
                y2 = bodyH
            )

            // Bottom edge
            lineTo(radius, bodyH)

            // Bottom left corner
            arcTo(
                rect = Rect(0f, bodyH - 2 * radius, 2 * radius, bodyH),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Left edge
            lineTo(x = 0f, y = radius)

            // Top left corner
            arcTo(
                rect = Rect(0f, 0f, 2 * radius, 2 * radius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }

        return Outline.Generic(path)
    }
}
