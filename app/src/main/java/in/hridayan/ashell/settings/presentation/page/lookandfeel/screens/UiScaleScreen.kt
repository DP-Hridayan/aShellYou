@file:OptIn(ExperimentalFoundationStyleApi::class)

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
import androidx.compose.material3.CardDefaults
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
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun UiScaleScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val screenDensityMultiplier = LocalSettings.current.screenDensityMultiplier
    val fontSizeMultiplier = LocalSettings.current.fontSizeMultiplier

    SettingsScaffold(
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
                        value = screenDensityMultiplier,
                        defaultValue = SettingsKeys.SCREEN_DENSITY_MULTIPLIER.defaultValue,
                        onValueChange = { newValue ->
                            settingsViewModel.setFloat(
                                key = SettingsKeys.SCREEN_DENSITY_MULTIPLIER,
                                value = newValue
                            )
                        }
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    )
                }

                item {
                    ScaleModifyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
                        title = stringResource(R.string.font_size_multiplier),
                        description = stringResource(R.string.des_font_size),
                        icon = painterResource(R.drawable.ic_format_size),
                        value = fontSizeMultiplier,
                        defaultValue = SettingsKeys.FONT_SIZE_MULTIPLIER.defaultValue,
                        valueRange = 0.5f..2f,
                        steps = 14,
                        onValueChange = { newValue ->
                            settingsViewModel.setFloat(
                                key = SettingsKeys.FONT_SIZE_MULTIPLIER,
                                value = newValue
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

@Composable
private fun ScaleModifyCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: Painter,
    value: Float,
    defaultValue: Float = 1f,
    onValueChange: (Float) -> Unit = {},
    valueRange: ClosedFloatingPointRange<Float> = 0.5f..1.5f,
    steps : Int = 9
) {
    val locale = LocalLocale.current
    val weakHaptic = LocalWeakHaptic.current
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalSettings.current.isHapticEnabled

    var sliderValue by remember(value) { mutableFloatStateOf(value) }

    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = CardDefaults.outlinedCardBorder(),
        clickable = false
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
                        if (isHapticEnabled) haptic.performHapticFeedback(
                            HapticFeedbackType.VirtualKey
                        )
                    },
                    onValueChangeFinished = {
                        weakHaptic()
                        onValueChange(sliderValue)
                    },
                    valueRange = valueRange,
                    steps = steps
                )

                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        onValueChange(defaultValue)
                        sliderValue = defaultValue
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