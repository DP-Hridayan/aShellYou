@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.image

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import `in`.hridayan.ashell.core.presentation.components.modifier.rotatingClip

private const val INITIAL_ROTATION_DEGREES = 0f
private const val TARGET_ROTATION_DEGREES = 360f
private const val DEFAULT_ROTATION_DURATION_MILLIS = 20_000

/**
 * Renders a profile image clipped to an infinitely rotating [MaterialShapes.Cookie12Sided] boundary
 * while keeping the image itself stationary.
 */
@Composable
fun ProfilePic(
    modifier: Modifier = Modifier,
    model: Any?,
    size: Dp,
    rotationDurationMillis: Int = DEFAULT_ROTATION_DURATION_MILLIS,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile_pic_infinite_transition")
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = INITIAL_ROTATION_DEGREES,
        targetValue = TARGET_ROTATION_DEGREES,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = rotationDurationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "profile_pic_rotation_degrees",
    )

    AsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .rotatingClip(
                polygon = MaterialShapes.Cookie12Sided,
                rotationDegrees = { rotationDegrees }
            ),
    )
}
