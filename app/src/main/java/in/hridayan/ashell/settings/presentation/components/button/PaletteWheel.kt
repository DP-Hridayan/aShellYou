@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.data.provider.SeedColor
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider

@Composable
fun PaletteWheel(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    seedColor: SeedColor = SeedColorProvider.seed,
    isChecked: Boolean = false,
    onClick: () -> Unit
) {
    val weakHaptic = LocalWeakHaptic.current

    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "Check Scale Animation"
    )

    val primaryColor = modifyColorForDisplay(Color(seedColor.primary), toneFactor = 1f)
    val secondaryColor = modifyColorForDisplay(Color(seedColor.secondary), toneFactor = 1.4f)
    val tertiaryColor = modifyColorForDisplay(Color(seedColor.tertiary), toneFactor = 0.7f)

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(
                enabled = true,
                onClick = {
                    onClick()
                    weakHaptic()
                })
    ) {
        Box(
            modifier = modifier
                .padding(10.dp)
                .size(size)
                .clip(CircleShape)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(color = primaryColor)
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(color = secondaryColor)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(color = tertiaryColor)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Center),
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

fun modifyColorForDisplay(
    color: Color,
    toneFactor: Float = 1.2f,
    chromaFactor: Float = 1.15f
): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)

    hsv[1] = (hsv[1] * chromaFactor).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * toneFactor).coerceIn(0f, 1f)

    return Color(android.graphics.Color.HSVToColor(hsv))
}