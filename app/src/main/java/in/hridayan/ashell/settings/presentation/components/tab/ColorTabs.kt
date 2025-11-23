@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.tab

import androidx.collection.FloatFloatPair
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalTonalPalette
import `in`.hridayan.ashell.core.presentation.theme.colorLerp
import `in`.hridayan.ashell.settings.presentation.components.button.PaletteWheel
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.LookAndFeelViewModel
import kotlin.math.abs

@Composable
fun ColorTabs(
    modifier: Modifier = Modifier,
    lookAndFeelViewModel: LookAndFeelViewModel = hiltViewModel()
) {
    val tonalPalettes = LocalTonalPalette.current
    val groupedPalettes = tonalPalettes.chunked(4)
    val pagerState = rememberPagerState(initialPage = 0) { groupedPalettes.size }
    val shuffledShapes = remember { shapes.shuffled() }

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

        val smallSize = 10.dp
        val bigSize = 16.dp

        Row(
            modifier = Modifier
                .width(120.dp)
                .heightIn(min = bigSize)
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val currentPage = pagerState.currentPage
            val offset = pagerState.currentPageOffsetFraction

            val selectedColor = MaterialTheme.colorScheme.primary
            val unselectedColor = MaterialTheme.colorScheme.surfaceVariant

            repeat(groupedPalettes.size) { index ->

                val targetSize = when (index) {
                    currentPage -> lerpSize(bigSize, smallSize, abs(offset))
                    currentPage + 1 -> lerpSize(smallSize, bigSize, offset.coerceIn(0f, 1f))
                    currentPage - 1 -> lerpSize(smallSize, bigSize, -offset.coerceIn(0f, 1f))
                    else -> smallSize
                }

                val targetColor: Color = when (index) {
                    currentPage -> {
                        colorLerp(selectedColor, unselectedColor, abs(offset))
                    }

                    currentPage + 1 -> {
                        colorLerp(unselectedColor, selectedColor, offset.coerceIn(0f, 1f))
                    }

                    currentPage - 1 -> {
                        colorLerp(unselectedColor, selectedColor, (-offset).coerceIn(0f, 1f))
                    }

                    else -> unselectedColor
                }

                val animatedSize by animateDpAsState(targetSize, label = "")
                val animatedColor by animateColorAsState(targetColor, label = "")

                Box(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(animatedSize)
                            .align(Alignment.Center)
                            .drawWithCache {
                                val sizePx = this.size.minDimension
                                val baseShape = MaterialShapes.Circle.scaled(sizePx)
                                val targetShape =
                                    shuffledShapes[index % shuffledShapes.size].scaled(sizePx)

                                val morphProgress = when (index) {
                                    currentPage -> 1f - abs(offset)
                                    currentPage + 1 -> abs(offset)
                                    currentPage - 1 -> abs(offset)
                                    else -> 0f
                                }.coerceIn(0f, 1f)

                                val morph = Morph(start = baseShape, end = targetShape)

                                val roundedPolygonPath =
                                    morph.toPath(progress = morphProgress).asComposePath()

                                onDrawBehind {
                                    drawPath(
                                        path = roundedPolygonPath,
                                        color = animatedColor
                                    )
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun lerpSize(start: Dp, end: Dp, fraction: Float): Dp {
    return start + (end - start) * fraction.coerceIn(0f, 1f)
}

private fun RoundedPolygon.scaled(scale: Float): RoundedPolygon {
    return this.transformed { x, y ->
        FloatFloatPair(x * scale, y * scale)
    }
}

private val shapes = listOf(
    MaterialShapes.SoftBurst,
    MaterialShapes.Arrow,
    MaterialShapes.Cookie4Sided,
    MaterialShapes.Pill,
    MaterialShapes.Diamond,
    MaterialShapes.Pentagon,
)
