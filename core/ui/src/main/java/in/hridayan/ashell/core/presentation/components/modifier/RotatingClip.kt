package `in`.hridayan.ashell.core.presentation.components.modifier

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import `in`.hridayan.ashell.core.presentation.components.shape.RotatingShape

private val DEFAULT_BORDER_WIDTH = 0.dp

/**
 * Clips the composable using a [shape] that rotates around its center driven by [rotationDegrees]
 * without rotating the composable content itself.
 *
 * @param shape The shape used for clipping.
 * @param rotationDegrees A lambda returning the rotation angle in degrees.
 * @param borderWidth The optional stroke width for an outline border along the rotating cut.
 * @param borderColor The color of the optional outline border.
 */
fun Modifier.rotatingClip(
    shape: Shape,
    rotationDegrees: () -> Float,
    borderWidth: Dp = DEFAULT_BORDER_WIDTH,
    borderColor: Color = Color.Unspecified,
): Modifier {
    val rotatingShape = RotatingShape(
        baseShape = shape,
        rotationDegrees = rotationDegrees,
    )

    val layerModifier = Modifier.graphicsLayer {
        clip = true
        this.shape = rotatingShape
    }

    if (borderWidth <= DEFAULT_BORDER_WIDTH || !borderColor.isSpecified) {
        return this.then(layerModifier)
    }

    val borderModifier = Modifier.drawWithContent {
        drawContent()
        val outline = rotatingShape.createOutline(size, layoutDirection, this)
        if (outline is Outline.Generic) {
            drawPath(
                path = outline.path,
                color = borderColor,
                style = Stroke(width = borderWidth.toPx()),
            )
        }
    }

    return this.then(borderModifier).then(layerModifier)
}

/**
 * Clips the composable using a [polygon] that rotates around its center driven by [rotationDegrees]
 * without rotating the composable content itself.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.rotatingClip(
    polygon: RoundedPolygon,
    rotationDegrees: () -> Float,
    borderWidth: Dp = DEFAULT_BORDER_WIDTH,
    borderColor: Color = Color.Unspecified,
): Modifier {
    val baseShape = polygon.toShape()
    return this.rotatingClip(
        shape = baseShape,
        rotationDegrees = rotationDegrees,
        borderWidth = borderWidth,
        borderColor = borderColor,
    )
}

/**
 * Clips the composable using a [shape] rotated by a static [degrees] angle
 * without rotating the composable content itself.
 */
fun Modifier.rotatingClip(
    shape: Shape,
    degrees: Float,
    borderWidth: Dp = DEFAULT_BORDER_WIDTH,
    borderColor: Color = Color.Unspecified,
): Modifier = rotatingClip(
    shape = shape,
    rotationDegrees = { degrees },
    borderWidth = borderWidth,
    borderColor = borderColor,
)

/**
 * Clips the composable using a [polygon] rotated by a static [degrees] angle
 * without rotating the composable content itself.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.rotatingClip(
    polygon: RoundedPolygon,
    degrees: Float,
    borderWidth: Dp = DEFAULT_BORDER_WIDTH,
    borderColor: Color = Color.Unspecified,
): Modifier = rotatingClip(
    polygon = polygon,
    rotationDegrees = { degrees },
    borderWidth = borderWidth,
    borderColor = borderColor,
)
