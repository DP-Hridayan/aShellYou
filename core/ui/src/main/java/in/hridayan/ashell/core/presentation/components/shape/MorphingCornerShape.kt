package `in`.hridayan.ashell.core.presentation.components.shape

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp

private const val MIN_RADIUS = 0f
private const val HALF_DIVISOR = 2f
private const val HASH_PRIME = 31

/**
 * A [Shape] that interpolates corner radii between [firstShape] and [secondShape]
 * based on a normalized [progress] value from 0f to 1f, respecting [LayoutDirection].
 */
class MorphingCornerShape(
    private val firstShape: CornerBasedShape,
    private val secondShape: CornerBasedShape,
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val maxRadius = size.minDimension / HALF_DIVISOR

        val firstTs = firstShape.topStart.toPx(size, density)
        val secondTs = secondShape.topStart.toPx(size, density)
        val ts = lerp(firstTs, secondTs, progress).coerceIn(MIN_RADIUS, maxRadius)

        val firstTe = firstShape.topEnd.toPx(size, density)
        val secondTe = secondShape.topEnd.toPx(size, density)
        val te = lerp(firstTe, secondTe, progress).coerceIn(MIN_RADIUS, maxRadius)

        val firstBe = firstShape.bottomEnd.toPx(size, density)
        val secondBe = secondShape.bottomEnd.toPx(size, density)
        val be = lerp(firstBe, secondBe, progress).coerceIn(MIN_RADIUS, maxRadius)

        val firstBs = firstShape.bottomStart.toPx(size, density)
        val secondBs = secondShape.bottomStart.toPx(size, density)
        val bs = lerp(firstBs, secondBs, progress).coerceIn(MIN_RADIUS, maxRadius)

        val isLtr = layoutDirection == LayoutDirection.Ltr
        return Outline.Rounded(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                topLeft = CornerRadius(if (isLtr) ts else te, if (isLtr) ts else te),
                topRight = CornerRadius(if (isLtr) te else ts, if (isLtr) te else ts),
                bottomRight = CornerRadius(if (isLtr) be else bs, if (isLtr) be else bs),
                bottomLeft = CornerRadius(if (isLtr) bs else be, if (isLtr) bs else be),
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MorphingCornerShape) return false
        return firstShape == other.firstShape &&
            secondShape == other.secondShape &&
            progress == other.progress
    }

    override fun hashCode(): Int {
        var result = firstShape.hashCode()
        result = HASH_PRIME * result + secondShape.hashCode()
        result = HASH_PRIME * result + progress.hashCode()
        return result
    }
}
