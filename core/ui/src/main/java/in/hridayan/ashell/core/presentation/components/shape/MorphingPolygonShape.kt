package `in`.hridayan.ashell.core.presentation.components.shape

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toPath
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.transformed
import android.graphics.Matrix as AndroidMatrix

private const val HASH_PRIME = 31
private const val ZERO_FLOAT = 0f
private const val DEFAULT_SCALE = 1f

/**
 * A [Shape] that renders an interpolated [Morph] between two polygon geometries
 * at a given [progress] value from 0f to 1f, proportionally scaled to fit within
 * and centered to the layout [Size].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MorphingPolygonShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = morph.toPath(progress)
        val bounds = path.getBounds()
        val scaleX = if (bounds.width > ZERO_FLOAT) size.width / bounds.width else DEFAULT_SCALE
        val scaleY = if (bounds.height > ZERO_FLOAT) size.height / bounds.height else DEFAULT_SCALE
        val scale = minOf(scaleX, scaleY)

        val matrix = Matrix()
        matrix.scale(scale, scale)
        path.transform(matrix)

        val offset = size.center - path.getBounds().center
        path.translate(offset)
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MorphingPolygonShape) return false
        return morph == other.morph && progress == other.progress
    }

    override fun hashCode(): Int {
        var result = morph.hashCode()
        result = HASH_PRIME * result + progress.hashCode()
        return result
    }
}

/**
 * Returns a new [RoundedPolygon] rotated by [degrees] around its center.
 */
fun RoundedPolygon.rotate(degrees: Float): RoundedPolygon {
    val matrix = AndroidMatrix().apply {
        postRotate(degrees, centerX, centerY)
    }
    return transformed(matrix).normalized()
}
