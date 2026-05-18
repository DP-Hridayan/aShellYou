@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.palette

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.LocalPaletteStyle
import `in`.hridayan.ashell.core.data.local.provider.SeedColor
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.AshellYouAnimationSpecs
import `in`.hridayan.ashell.core.presentation.theme.color.getPaletteKeyColors

@Composable
fun PaletteWheel(
    modifier: Modifier = Modifier,
    seedColor: SeedColor = SeedColorProvider.seed,
    isChecked: Boolean = false,
    onClick: () -> Unit
) {
    val checkedIconScale by animateFloatAsState(
        targetValue = if (isChecked) 1f else 0f,
        animationSpec = if (isChecked) AshellYouAnimationSpecs.springFloat else tween(
            durationMillis = 300,
            easing = LinearEasing
        ),
        label = "Check Scale Animation"
    )

    // Derive display colors from the primary seed + current palette style.
    // These are always at tone 40 and independent of light/dark mode.
    val paletteStyle = LocalPaletteStyle.current
    val keyColors = remember(seedColor.primary, paletteStyle) {
        getPaletteKeyColors(seedColor.primary, paletteStyle)
    }

    CustomCard(
        modifier = modifier,
        onClick = withHaptic { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .clip(CircleShape)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(color = keyColors.primary)
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
                            .background(color = keyColors.secondary)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(color = keyColors.tertiary)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
                    .scale(checkedIconScale)
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