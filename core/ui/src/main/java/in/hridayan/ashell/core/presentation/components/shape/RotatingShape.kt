package `in`.hridayan.ashell.core.presentation.components.shape

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

private const val HASH_PRIME = 31

/**
 * A [Shape] that wraps a [baseShape] and applies a rotation around [Size.center]
 * dynamically evaluated from [rotationDegrees].
 */
@Immutable
class RotatingShape(
    private val baseShape: Shape,
    private val rotationDegrees: () -> Float,
) : Shape {
    private var workPath: Path? = null
    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val baseOutline = baseShape.createOutline(size, layoutDirection, density)
        val path = (workPath ?: Path().also { workPath = it }).apply { rewind() }

        when (baseOutline) {
            is Outline.Generic -> path.addPath(baseOutline.path)
            is Outline.Rounded -> path.addRoundRect(baseOutline.roundRect)
            is Outline.Rectangle -> path.addRect(baseOutline.rect)
        }

        matrix.reset()
        matrix.translate(size.center.x, size.center.y)
        matrix.rotateZ(rotationDegrees())
        matrix.translate(-size.center.x, -size.center.y)
        path.transform(matrix)

        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RotatingShape) return false
        return baseShape == other.baseShape
    }

    override fun hashCode(): Int {
        return HASH_PRIME * baseShape.hashCode()
    }
}
