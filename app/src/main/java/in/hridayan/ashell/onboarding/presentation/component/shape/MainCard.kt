package `in`.hridayan.ashell.onboarding.presentation.component.shape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun MainCard(
    modifier: Modifier = Modifier,
    scaleX: () -> Float,
    scaleY: () -> Float,
    shape: Shape,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = modifier
            .graphicsLayer {
                this.scaleX = scaleX()
                this.scaleY = scaleY()
            }
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
        contentAlignment = contentAlignment,
        content = content
    )
}
