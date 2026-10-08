package `in`.hridayan.ashell.core.presentation.components.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import `in`.hridayan.ashell.core.presentation.components.shape.MorphingCornerShape
import `in`.hridayan.ashell.core.presentation.components.shape.MorphingPolygonShape
import `in`.hridayan.ashell.core.presentation.theme.AshellYouAnimationSpecs

private const val FIRST_SHAPE_PROGRESS = 0f
private const val SECOND_SHAPE_PROGRESS = 1f
private const val SWITCH_THRESHOLD = 0.5f

private val DEFAULT_ELEVATION = 0.dp
private val RECTANGLE_CORNER_RADIUS = 0.dp

/**
 * Clips the composable using a shape that smoothly morphs between [firstShape] and [secondShape]
 * polygon geometries driven by an [AshellYouAnimationSpecs.springFloat] spring animation based on [toggled].
 *
 * @param firstShape The polygon when [toggled] is false.
 * @param secondShape The polygon when [toggled] is true.
 * @param toggled Whether the shape should morph to [secondShape].
 * @param elevation The elevation shadow to cast with the morphing shape.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.morphClip(
    firstShape: RoundedPolygon,
    secondShape: RoundedPolygon,
    toggled: Boolean,
    elevation: Dp = DEFAULT_ELEVATION,
): Modifier {
    val morph = remember(firstShape, secondShape) {
        Morph(firstShape, secondShape)
    }

    val progressState = animateFloatAsState(
        targetValue = if (toggled) SECOND_SHAPE_PROGRESS else FIRST_SHAPE_PROGRESS,
        animationSpec = AshellYouAnimationSpecs.springFloat,
        label = "morph_clip_progress",
    )

    return this.graphicsLayer {
        val progress = progressState.value
        shadowElevation = elevation.toPx()
        clip = true
        shape = MorphingPolygonShape(
            morph = morph,
            progress = progress,
        )
    }
}

/**
 * Clips the composable using a shape that smoothly morphs between [firstShape] and [secondShape]
 * driven by an [AshellYouAnimationSpecs.springFloat] spring animation based on [toggled].
 *
 * @param firstShape The shape when [toggled] is false.
 * @param secondShape The shape when [toggled] is true.
 * @param toggled Whether the shape should morph to [secondShape].
 * @param elevation The elevation shadow to cast with the morphing shape.
 */
@Composable
fun Modifier.morphClip(
    firstShape: CornerBasedShape,
    secondShape: CornerBasedShape,
    toggled: Boolean,
    elevation: Dp = DEFAULT_ELEVATION,
): Modifier {
    val progressState = animateFloatAsState(
        targetValue = if (toggled) SECOND_SHAPE_PROGRESS else FIRST_SHAPE_PROGRESS,
        animationSpec = AshellYouAnimationSpecs.springFloat,
        label = "morph_clip_progress",
    )

    return this.graphicsLayer {
        val progress = progressState.value
        shadowElevation = elevation.toPx()
        clip = true
        shape = MorphingCornerShape(
            firstShape = firstShape,
            secondShape = secondShape,
            progress = progress,
        )
    }
}

/**
 * Clips the composable using a shape that morphs between [firstShape] and [secondShape]
 * driven by an [AshellYouAnimationSpecs.springFloat] spring animation based on [toggled].
 *
 * If both shapes are [CornerBasedShape] (or [RectangleShape]), smooth corner interpolation
 * is performed. Otherwise, the shapes toggle at the midpoint threshold.
 *
 * @param firstShape The shape when [toggled] is false.
 * @param secondShape The shape when [toggled] is true.
 * @param toggled Whether the shape should morph to [secondShape].
 * @param elevation The elevation shadow to cast with the morphing shape.
 */
@Composable
fun Modifier.morphClip(
    firstShape: Shape,
    secondShape: Shape,
    toggled: Boolean,
    elevation: Dp = DEFAULT_ELEVATION,
): Modifier {
    val firstCornerShape = firstShape.toCornerBasedShape()
    val secondCornerShape = secondShape.toCornerBasedShape()

    if (firstCornerShape != null && secondCornerShape != null) {
        return morphClip(
            firstShape = firstCornerShape,
            secondShape = secondCornerShape,
            toggled = toggled,
            elevation = elevation,
        )
    }

    val progressState = animateFloatAsState(
        targetValue = if (toggled) SECOND_SHAPE_PROGRESS else FIRST_SHAPE_PROGRESS,
        animationSpec = AshellYouAnimationSpecs.springFloat,
        label = "morph_clip_progress",
    )

    return this.graphicsLayer {
        val progress = progressState.value
        shadowElevation = elevation.toPx()
        clip = true
        shape = if (progress < SWITCH_THRESHOLD) firstShape else secondShape
    }
}

private fun Shape.toCornerBasedShape(): CornerBasedShape? = when (this) {
    is CornerBasedShape -> this
    RectangleShape -> RoundedCornerShape(RECTANGLE_CORNER_RADIUS)
    else -> null
}
