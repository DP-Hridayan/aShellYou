package `in`.hridayan.ashell.settings.presentation.components.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalTonalPalette
import `in`.hridayan.ashell.settings.presentation.components.button.PaletteWheel
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.LookAndFeelViewModel

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
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                groupedPalettes[page].forEach { palette ->
                    val isChecked = LocalSeedColor.current == palette.colors
                    val isDynamicColor = LocalSettings.current.isDynamicColor

                    PaletteWheel(
                        seedColor = palette.colors,
                        onClick = {
                            lookAndFeelViewModel.setSeedColor(seed = palette.colors)
                            lookAndFeelViewModel.disableDynamicColors()
                        },
                        isChecked = isChecked && !isDynamicColor,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(groupedPalettes.size) { index ->
                val isSelected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}
