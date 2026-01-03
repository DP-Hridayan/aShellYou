@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalTonalPalette
import `in`.hridayan.ashell.settings.presentation.components.palette.PaletteWheel
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.LookAndFeelViewModel
import `in`.hridayan.shapeindicators.ShapeIndicatorRow

@Composable
fun ColorTabs(
    modifier: Modifier = Modifier,
    lookAndFeelViewModel: LookAndFeelViewModel = hiltViewModel()
) {
    val tonalPalettes = LocalTonalPalette.current
    val groupedPalettes = tonalPalettes.chunked(4)
    val pagerState = rememberPagerState(initialPage = 0) { groupedPalettes.size }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                groupedPalettes[page].forEach { palette ->
                    val isChecked = LocalSeedColor.current == palette.colors
                    val isDynamicColor = LocalSettings.current.isDynamicColor

                    PaletteWheel(
                        seedColor = palette.colors,
                        onClick = {
                            lookAndFeelViewModel.setSeedColor(palette.colors)
                            lookAndFeelViewModel.disableDynamicColors()
                        },
                        isChecked = isChecked && !isDynamicColor,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        ShapeIndicatorRow(
            modifier = Modifier
                .width(120.dp)
                .align(Alignment.CenterHorizontally),
            pagerState = pagerState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            shuffleShapes = true
        )
    }
}
