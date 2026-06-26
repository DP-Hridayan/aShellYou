@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.LocalPaletteStyle
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalTonalPalette
import `in`.hridayan.ashell.core.data.local.provider.SeedColor
import `in`.hridayan.ashell.core.domain.model.PaletteStyle
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.components.palette.PaletteWheel
import `in`.hridayan.shapeindicators.ShapeIndicatorRow

@Composable
fun ColorTabs(
    modifier: Modifier = Modifier,
    onClickTab: (SeedColor) -> Unit = {},
    onClickMonochromeTab: () -> Unit = {}
) {
    val tonalPalettes = LocalTonalPalette.current
    val groupedPalettes = tonalPalettes.chunked(4)
    val pagerState = rememberPagerState(initialPage = 0) { groupedPalettes.size }
    val paletteStyle = LocalPaletteStyle.current
    val isMonochromePalette = paletteStyle == PaletteStyle.MONOCHROME
    val isDynamicColor = LocalSettings.current[SettingsKeys.DynamicColors]

    Column(
        modifier = modifier,
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
                    isChecked = !isDynamicColor,
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
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    groupedPalettes[page].forEach { palette ->
                        val isChecked = LocalSeedColor.current.primary == palette.colors.primary

                        PaletteWheel(
                            modifier = Modifier.size(70.dp),
                            seedColor = palette.colors,
                            onClick = { onClickTab(palette.colors) },
                            isChecked = isChecked && !isDynamicColor,
                        )
                    }

                }
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterHorizontally),
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
