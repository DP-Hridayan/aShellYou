package `in`.hridayan.ashell.core.presentation.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch

@Composable
fun syncedRotationAndScale(): Pair<Float, Float> {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val durationMillis = 3000

    LaunchedEffect(Unit) {
        launch {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(
                        durationMillis = durationMillis,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
        launch {
            var toSmall = true
            while (true) {
                val target = if (toSmall) 0.7f else 1f
                toSmall = !toSmall
                scale.animateTo(
                    targetValue = target,
                    animationSpec = tween(
                        durationMillis = durationMillis,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    return rotation.value to scale.value
}
