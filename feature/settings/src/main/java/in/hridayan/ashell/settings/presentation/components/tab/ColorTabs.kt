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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.FeatureConfig
import `in`.hridayan.ashell.core.common.LocalPaletteStyle
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.LocalTonalPalette
import `in`.hridayan.ashell.core.common.data.provider.SeedColor
import `in`.hridayan.ashell.core.common.domain.model.PaletteStyle
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.card.CustomCardDefaults
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.components.palette.PaletteWheel
import `in`.hridayan.shapeindicators.ShapeIndicatorDefaults
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

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val aiSectionWidth =
            if (FeatureConfig.isAiEnabled) 85.dp else 0.dp // 70dp button + padding + divider
        val availablePagerWidth = maxWidth - aiSectionWidth
        val itemWidth = 70.dp + 10.dp
        val calculatedChunkSize = maxOf(1, (availablePagerWidth / itemWidth).toInt())
        val groupedPalettes = tonalPalettes.chunked(calculatedChunkSize)
        val pagerState = rememberPagerState(initialPage = 0) { groupedPalettes.size }
        val pagerContentWidth =
            (70.dp * calculatedChunkSize) + (10.dp * (calculatedChunkSize - 1).coerceAtLeast(0))

        Row(
            modifier = Modifier.wrapContentWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            if (FeatureConfig.isAiEnabled) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomCard(
                        onClick = withHaptic { onClickCreateTheme() },
                        modifier = Modifier.size(70.dp),
                        shape = CustomCardShape(50),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_format_paint),
                                contentDescription = "Create Custom Theme",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .height(50.dp)
                            .padding(horizontal = 10.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = isMonochromePalette,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                    exit = ExitTransition.None
                ) {
                    Box(
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
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .width(pagerContentWidth)
                            .clip(RoundedCornerShape(CustomCardDefaults.pressedCornerRadius))
                    ) { page ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
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

                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp),
                    visible = !isMonochromePalette,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                    exit = ExitTransition.None
                ) {
                    ShapeIndicatorRow(
                        pagerState = pagerState,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        shuffleShapes = true,
                        overflow = ShapeIndicatorDefaults.overflow(maxVisibleItems = 6)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}
