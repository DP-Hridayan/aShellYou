package `in`.hridayan.ashell.ai.presentation.components.loadingindicator

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun BouncyDotsLoadingIndicator(
    modifier: Modifier = Modifier,
    dotColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val dotsCount = 4
    val animationDelay = 150
    val infiniteTransition = rememberInfiniteTransition(label = "BouncyDots")

    val dotAnimations = (0 until dotsCount).map { index ->
        val delayMillis = index * animationDelay
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 300, delayMillis = delayMillis, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "DotAnimation$index"
        )
    }

    Row(modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        dotAnimations.forEachIndexed { index, anim ->
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = anim.value.dp.toPx() }
                    .size(8.dp)
                    .background(color = dotColor, shape = CircleShape)
            )
            if (index < dotsCount - 1) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}
