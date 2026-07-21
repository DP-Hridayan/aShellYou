package `in`.hridayan.ashell.ai.presentation.components.dialog

import `in`.hridayan.ashell.core.resources.R

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType

private const val MIN_CACHE_DAYS = 1
private const val MAX_CACHE_DAYS = 365

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CacheDurationDialog(
    modifier: Modifier = Modifier,
    currentDays: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentDays.toString()) }
    val days = listOf(1, 7, 30)
    val initialSelectedIndex = days.indexOfFirst { it == currentDays }
    var selectedIndex by rememberSaveable { mutableIntStateOf(initialSelectedIndex) }

    val parsedValue = text.toIntOrNull()
    val isValid = parsedValue != null && parsedValue in MIN_CACHE_DAYS..MAX_CACHE_DAYS
    val isError = text.isNotEmpty() && !isValid

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.ai_cache_days),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp)
                ) {
                    days.forEachIndexed { index, day ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = days.size
                            ),
                            onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                selectedIndex = index
                                text = day.toString()
                            },
                            selected = index == selectedIndex,
                            label = { Text(text = stringResource(R.string.n_days, day)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { newValue ->
                        // Accept only digits
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            text = newValue
                        }

                        selectedIndex = days.indexOfFirst { it.toString() == newValue }
                    },
                    label = { Text(stringResource(R.string.ai_cache_days)) },
                    supportingText = {
                        if (isError) {
                            Text(text = "$MIN_CACHE_DAYS – $MAX_CACHE_DAYS " + stringResource(R.string.n_days))
                        }
                    },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OverflowButtonGroup(
                    modifier = Modifier.padding(top = 20.dp),
                    items = listOf(
                        ButtonGroupItem(
                            buttonConfig = ButtonConfigDefaults.defaultConfig(
                                type = ButtonType.OutlinedButton
                            ),
                            text = stringResource(R.string.cancel),
                            onClick = { onDismiss() }
                        ),
                        ButtonGroupItem(
                            enabled = isValid,
                            text = stringResource(R.string.confirm),
                            onClick = {
                                parsedValue?.let { onConfirm(it) }
                                onDismiss()
                            }
                        ),
                    )
                )
            }
        }
    }
}

