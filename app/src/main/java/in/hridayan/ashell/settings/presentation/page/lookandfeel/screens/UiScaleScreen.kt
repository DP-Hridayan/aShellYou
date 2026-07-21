@file:OptIn(
    ExperimentalFoundationStyleApi::class,
    ExperimentalMaterial3Api::class
)

package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.skydoves.compose.stability.runtime.TraceRecomposition
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.navigateBack
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun UiScaleScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val listState = rememberLazyListState()
    val settings = LocalSettings.current

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.ui_scale),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }

                item {
                    ScaleModifyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
                        title = stringResource(R.string.screen_density_multiplier),
                        description = stringResource(R.string.des_screen_density),
                        icon = painterResource(R.drawable.ic_high_density),
                        shape = CardCornerShape.FIRST_CARD,
                        value = settings[SettingsKeys.ScreenDensityMultiplier],
                        onValueChangeFinished = { newValue ->
                            settingsViewModel.setFloat(
                                key = SettingsKeys.ScreenDensityMultiplier,
                                value = newValue
                            )
                        },
                        onValueReset = {
                            settingsViewModel.setFloat(
                                key = SettingsKeys.ScreenDensityMultiplier,
                                value = SettingsKeys.ScreenDensityMultiplier.defaultValue
                            )
                        }
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    )
                }

                item {
                    ScaleModifyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
                        title = stringResource(`in`.hridayan.ashell.core.common.R.string.font_size_multiplier),
                        description = stringResource(R.string.des_font_size),
                        icon = painterResource(R.drawable.ic_format_size),
                        shape = CardCornerShape.LAST_CARD,
                        value = settings[SettingsKeys.FontSizeMultiplier],
                        valueRange = 0.5f..2f,
                        steps = 14,
                        onValueChangeFinished = { newValue ->
                            settingsViewModel.setFloat(
                                key = SettingsKeys.FontSizeMultiplier,
                                value = newValue
                            )
                        },
                        onValueReset = {
                            settingsViewModel.setFloat(
                                key = SettingsKeys.FontSizeMultiplier,
                                value = SettingsKeys.FontSizeMultiplier.defaultValue
                            )
                        }
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        },
    )
}

@TraceRecomposition
@Composable
private fun ScaleModifyCard(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit = {},
    onValueChangeFinished: (Float) -> Unit = {},
    onValueReset: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0.5f..1.5f,
    steps: Int = 9,
    title: String,
    description: String,
    icon: Painter,
    shape: CustomCardShape = CustomCardShape(24),
    enableHaptics: Boolean = true,
) {
    val locale = LocalLocale.current
    val haptic = LocalHapticFeedback.current

    var sliderValue by remember(value) { mutableFloatStateOf(value) }

    CustomCard(
        modifier = modifier,
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    painter = icon,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(0.65f),
                        text = description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(IconButtonDefaults.smallContainerSize())
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizeableText(
                        modifier = Modifier.padding(3.dp),
                        text = String.format(locale.platformLocale, "%.2f", sliderValue),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Slider(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        if (enableHaptics) haptic.performHapticFeedback(
                            HapticFeedbackType.VirtualKey
                        )
                        onValueChange(it)
                    },
                    onValueChangeFinished = {
                        if (enableHaptics) haptic.performHapticFeedback(
                            HapticFeedbackType.VirtualKey
                        )
                        onValueChangeFinished(sliderValue)
                    },
                    valueRange = valueRange,
                    steps = steps
                )

                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    onClick = {
                        if (enableHaptics) haptic.performHapticFeedback(
                            HapticFeedbackType.VirtualKey
                        )
                        onValueReset()
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reset_settings),
                        contentDescription = null
                    )
                }
            }
        }
    }
}
