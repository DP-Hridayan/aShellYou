@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.settings.presentation.model.PreferenceItem
import `in`.hridayan.ashell.settings.presentation.model.SettingsType
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun IntPreferenceItemView(
    modifier: Modifier = Modifier,
    item: PreferenceItem,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    if (!item.isLayoutVisible) return

    val weakHaptic = LocalWeakHaptic.current

    val selected =
        settingsViewModel.getInt(key = item.key).collectAsState(initial = item.key.default as Int)

    val onSelectedChange: (Int) -> Unit = {
        settingsViewModel.setInt(key = item.key, value = it)
    }

    val intItems = item as PreferenceItem.IntPreferenceItem

    if (item.type == SettingsType.RadioGroup) {
        Column(modifier = modifier) {
            intItems.radioOptions.forEachIndexed { index, option ->
                val shape = getRoundedShape(index, intItems.radioOptions.size)

                RoundedCornerCard(
                    modifier = Modifier.fillMaxWidth(),
                    roundedCornerShape = shape
                )
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                                onSelectedChange(option.value)
                            })
                            .padding(vertical = 8.dp, horizontal = 20.dp)
                    ) {
                        Text(
                            text = stringResource(option.labelResId),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.weight(1f))

                        RadioButton(
                            selected = (option.value == selected.value),
                            onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                                onSelectedChange(option.value)
                            }
                        )
                    }
                }
            }
        }
    }

    if (item.type == SettingsType.SingleSelectButtonGroups) {
        val options = item.buttonGroupOptions

        Row(
            modifier = modifier.padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            options.forEachIndexed { index, option ->
                ToggleButton(
                    checked = option.value == selected.value,
                    onCheckedChange = {
                        onSelectedChange(option.value)
                        weakHaptic()
                    },
                    modifier = Modifier.weight(1f),
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }
                ) {
                    option.iconResId?.let {
                        Icon(
                            painter = painterResource(it),
                            contentDescription = null
                        )
                    }

                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))

                    option.labelResId?.let {
                        Text(stringResource(it))
                    }
                }
            }
        }
    }
}