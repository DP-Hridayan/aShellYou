package `in`.hridayan.ashell.ai.presentation.components.bottomsheet

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Skeleton/shimmer loading placeholders that mimic the final analysis layout.
 * Provides visual continuity during AI inference — no blank screens or spinner-only states.
 */
@Composable
fun SkeletonLoadingContent(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.3f),
        ),
        start = Offset(translateAnim - 200, translateAnim - 200),
        end = Offset(translateAnim, translateAnim)
    )

    Column(modifier = modifier.padding(16.dp)) {
        // Description skeleton
        ShimmerCard(shimmerBrush, heightDp = 80)
        Spacer(Modifier.height(8.dp))

        // Danger level skeleton
        Row(modifier = Modifier.fillMaxWidth()) {
            ShimmerBox(shimmerBrush, widthDp = 120, heightDp = 36)
            Spacer(Modifier.width(8.dp))
            ShimmerBox(shimmerBrush, widthDp = 100, heightDp = 36)
        }
        Spacer(Modifier.height(8.dp))

        // Warnings skeleton
        ShimmerCard(shimmerBrush, heightDp = 60)
        Spacer(Modifier.height(8.dp))

        // Root/Reversible badges skeleton
        Row(modifier = Modifier.fillMaxWidth()) {
            ShimmerBox(shimmerBrush, widthDp = 100, heightDp = 32)
            Spacer(Modifier.width(8.dp))
            ShimmerBox(shimmerBrush, widthDp = 90, heightDp = 32)
        }
        Spacer(Modifier.height(8.dp))

        // Use cases skeleton
        ShimmerCard(shimmerBrush, heightDp = 70)
        Spacer(Modifier.height(8.dp))

        // Examples skeleton
        ShimmerCard(shimmerBrush, heightDp = 50)
    }
}

@Composable
private fun ShimmerCard(brush: Brush, heightDp: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

@Composable
private fun ShimmerBox(brush: Brush, widthDp: Int, heightDp: Int) {
    Box(
        modifier = Modifier
            .width(widthDp.dp)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
    )
}
