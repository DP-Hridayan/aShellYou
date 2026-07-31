@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.tab


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.LocalPaletteStyle
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.LocalTonalPalette
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.common.data.provider.SeedColor
import `in`.hridayan.ashell.core.common.domain.model.PaletteStyle
import `in`.hridayan.ashell.settings.presentation.components.palette.PaletteWheel
import `in`.hridayan.shapeindicators.ShapeIndicatorRow

@Composable
fun ColorTabs(
    modifier: Modifier = Modifier,
    onClickTab: (SeedColor) -> Unit = {},
    onClickMonochromeTab: () -> Unit = {},
    onClickCreateTheme: () -> Unit = {}
) {
    val tonalPalettes = LocalTonalPalette.current
    val paletteStyle = LocalPaletteStyle.current
    val isMonochromePalette = paletteStyle == PaletteStyle.MONOCHROME
    val settings = LocalSettings.current
    val isDynamicColor = settings[SettingsKeys.DynamicColors]
    val userGeneratedColorSchemeApplied = settings[SettingsKeys.UserGeneratedColorSchemeApplied]

    BoxWithConstraints(modifier = modifier) {
        val availablePagerWidth = maxWidth * 0.75f // Pager takes 3f out of 4f total weight
        val itemWidth = 75.dp // 70.dp for the PaletteWheel + 5.dp padding approx
        val calculatedChunkSize = maxOf(1, (availablePagerWidth / itemWidth).toInt())
        val groupedPalettes = tonalPalettes.chunked(calculatedChunkSize)
        val pagerState = rememberPagerState(initialPage = 0) { groupedPalettes.size }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = isMonochromePalette,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                exit = ExitTransition.None
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PaletteWheel(
                        modifier = Modifier.size(70.dp),
                        seedColor = tonalPalettes.first().colors,
                        onClick = onClickMonochromeTab,
                        isChecked = !isDynamicColor && !userGeneratedColorSchemeApplied,
                    )
                }
            }

            AnimatedVisibility(
                visible = !isMonochromePalette,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                exit = ExitTransition.None
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedIconButton(
                            onClick = onClickCreateTheme,
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape,
                            border = BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = "Create Custom Theme",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .height(50.dp)
                            .padding(horizontal = 5.dp)
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(3f)
                    ) { page ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            groupedPalettes[page].forEach { palette ->
                                val isChecked =
                                    LocalSeedColor.current.seed == palette.colors.seed
                                // Also need to check if custom theme is not applied, but for now we just rely on onClick to reset it

                                PaletteWheel(
                                    modifier = Modifier.size(70.dp),
                                    seedColor = palette.colors,
                                    onClick = { onClickTab(palette.colors) },
                                    isChecked = isChecked && !isDynamicColor && !userGeneratedColorSchemeApplied,
                                )
                            }

                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                visible = !isMonochromePalette,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                exit = ExitTransition.None
            ) {
                ShapeIndicatorRow(
                    pagerState = pagerState,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    shuffleShapes = true,
                )
            }
        }
    }
}
