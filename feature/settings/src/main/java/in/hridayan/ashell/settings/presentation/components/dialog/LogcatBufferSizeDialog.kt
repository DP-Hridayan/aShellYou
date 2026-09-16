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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.LogcatBufferSize
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
import `in`.hridayan.ashell.settings.presentation.model.logcatBufferSizeLabel
import `in`.hridayan.settingsgraph.model.RadioButtonOption

private const val SELECTED_CARD_SHAPE_PERCENT = 50

@Composable
fun LogcatBufferSizeDialog(
    onDismiss: () -> Unit,
) {
    val settings = LocalSettings.current

    val bufferSizeOptions = getBufferSizeOptions()

    val selected = settings[SettingsKeys.LogcatBufferLimit]
    var tempSelected by remember { mutableIntStateOf(selected) }

    LaunchedEffect(selected) {
        tempSelected = selected
    }

    DialogContainer(
        onDismiss = onDismiss
    ) {
        AutoResizeableText(
            text = stringResource(R.string.log_buffer_size),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        bufferSizeOptions.forEachIndexed { index, option ->
            BufferSizeOptionCard(
                option = option,
                isSelected = option.value == tempSelected,
                shape = getRoundedShape(index, bufferSizeOptions.size),
                onSelect = { tempSelected = option.value },
            )
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
                        key = SettingsKeys.LogcatBufferLimit,
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
private fun BufferSizeOptionCard(
    option: RadioButtonOption,
    isSelected: Boolean,
    shape: CustomCardShape,
    onSelect: () -> Unit,
) {
    val label = option.labelString ?: return

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

    val finalShape = if (isSelected) CustomCardShape(SELECTED_CARD_SHAPE_PERCENT) else shape

    CustomCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = finalShape,
        colors = cardColors,
        onClick = withHaptic(HapticFeedbackType.ToggleOn) { onSelect() }
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
                onClick = withHaptic(HapticFeedbackType.ToggleOn) { onSelect() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun getBufferSizeOptions(): List<RadioButtonOption> {
    val labels = LogcatBufferSize.ALL.map { logcatBufferSizeLabel(it) }

    return remember(labels) {
        LogcatBufferSize.ALL.mapIndexed { index, megabytes ->
            RadioButtonOption(megabytes, labels[index])
        }
    }
}
