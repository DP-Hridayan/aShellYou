package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.AuthenticationTimeout
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.settingsgraph.model.RadioButtonOption

@Composable
fun AuthenticationTimeoutDialog(
    onDismiss: () -> Unit,
) {
    val settings = LocalSettings.current

    val timeoutOptions = getTimeoutOptions()

    val selected = settings[SettingsKeys.AuthenticationTimeout]
    var tempSelected by remember { mutableIntStateOf(selected) }

    LaunchedEffect(selected) {
        tempSelected = selected
    }

    DialogContainer(
        onDismiss = onDismiss
    ) {
        AutoResizeableText(
            text = stringResource(R.string.authentication_timeout),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        timeoutOptions.forEachIndexed { index, option ->
            val shape = getRoundedShape(index, timeoutOptions.size)

            val isSelected = option.value == tempSelected

            val cardColors = if (isSelected) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            val finalShape = if (isSelected) {
                CustomCardShape(50)
            } else {
                shape
            }

            option.labelString?.let { label ->
                CustomCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    shape = finalShape,
                    colors = cardColors,
                    onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                        tempSelected = option.value
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 20.dp)
                    ) {

                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )


                        Spacer(Modifier.weight(1f))

                        RadioButton(
                            selected = isSelected,
                            onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                                tempSelected = option.value
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        val buttonGroupItems = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                text = stringResource(R.string.cancel),
                onClick = { onDismiss() }
            ),
            ButtonGroupItem(
                text = stringResource(R.string.apply),
                onClick = {
                    settings.set(
                        key = SettingsKeys.AuthenticationTimeout,
                        value = tempSelected
                    )
                    onDismiss()
                }
            )
        )

        OverflowButtonGroup(
            items = buttonGroupItems,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
private fun getTimeoutOptions(): List<RadioButtonOption> {
    val immediateLabel = stringResource(R.string.timeout_immediate)
    val min1Label = pluralStringResource(R.plurals.timeout_minutes, 1, 1)
    val min5Label = pluralStringResource(R.plurals.timeout_minutes, 5, 5)
    val min10Label = pluralStringResource(R.plurals.timeout_minutes, 10, 10)
    val neverLabel = stringResource(R.string.timeout_never)

    val finalTimeoutOptions =
        remember(immediateLabel, min1Label, min5Label, min10Label, neverLabel) {
            listOf(
                RadioButtonOption(AuthenticationTimeout.IMMEDIATE, immediateLabel),
                RadioButtonOption(AuthenticationTimeout.MIN_1, min1Label),
                RadioButtonOption(AuthenticationTimeout.MIN_5, min5Label),
                RadioButtonOption(AuthenticationTimeout.MIN_10, min10Label),
                RadioButtonOption(AuthenticationTimeout.NEVER, neverLabel)
            )
        }

    return finalTimeoutOptions
}