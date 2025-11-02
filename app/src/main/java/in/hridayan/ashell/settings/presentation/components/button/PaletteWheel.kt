package `in`.hridayan.ashell.settings.presentation.components.button

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.material.color.utilities.CorePalette
import com.google.android.material.color.utilities.TonalPalette
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.SeedColorProvider
import `in`.hridayan.ashell.core.common.constants.SeedColor
import `in`.hridayan.ashell.core.presentation.utils.a1

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

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
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
                        .background(color = Color(seedColor.primary))
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
                            .background(color = Color(seedColor.secondary))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(color = Color(seedColor.tertiary))
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
                    tint = Color(seedColor.primary)
                )
            }
        }
    }
}

@SuppressLint("RestrictedApi")
private fun getColorFromSeed(seedColor: Int, tone: Int): Color {
    val palette = CorePalette.of(seedColor)
    val colorInt = palette.a1.getHct(tone.toDouble()).toInt()
    return Color(colorInt)
}
