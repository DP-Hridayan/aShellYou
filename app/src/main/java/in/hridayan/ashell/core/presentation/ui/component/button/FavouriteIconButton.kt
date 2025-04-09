package `in`.hridayan.ashell.core.presentation.ui.component.button

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun FavouriteIconButton(
    isFavorite: Boolean,
    onToggle: () -> Unit
) {
    val icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
    val tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else Color.Gray

    var popTrigger by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (popTrigger && isFavorite) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PopScale"
    )

    LaunchedEffect(isFavorite) {
        popTrigger = true
        delay(250)
        popTrigger = false
    }

    IconButton(onClick = onToggle) {
        Icon(
            imageVector = icon,
            contentDescription = "Favorite",
            tint = tint,
            modifier = Modifier
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
        )
    }
}
