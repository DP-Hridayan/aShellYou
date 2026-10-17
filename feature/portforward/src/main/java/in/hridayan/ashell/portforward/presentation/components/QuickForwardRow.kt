package `in`.hridayan.ashell.portforward.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.DevicePreset
import `in`.hridayan.ashell.portforward.presentation.components.editor.labelRes

private const val LOCALHOST = "localhost"
private const val MIN_PORT = 1
private const val MAX_PORT = 65535
private const val MAX_PORT_DIGITS = 5

private fun String.isPortInput(): Boolean = all { it.isDigit() } && length <= MAX_PORT_DIGITS

@Composable
private fun OpensAt(localPort: Int) {
    Text(stringResource(R.string.port_forward_opens_at, "$LOCALHOST:$localPort"))
}

/**
 * Forwards a device port in one step: type it and press Start, or pick a preset. "More options"
 * opens the full editor.
 */
@Composable
fun QuickForwardRow(
    device: ExternalDeviceState.Connected,
    onForward: (Int) -> Unit,
    onPreset: (DevicePreset) -> Unit,
    onMoreOptions: () -> Unit
) {
    var port by rememberSaveable { mutableStateOf("") }
    val parsed = port.toIntOrNull()?.takeIf { it in MIN_PORT..MAX_PORT }
    val forward = {
        parsed?.let(onForward)
        port = ""
    }
    CustomCard(modifier = Modifier.fillMaxWidth(), clickable = false) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.port_forward_quick_forward_title, device.displayName),
                style = MaterialTheme.typography.titleSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = port,
                    onValueChange = { text -> if (text.isPortInput()) port = text },
                    label = { Text(stringResource(R.string.port_forward_device_port)) },
                    supportingText = parsed?.let { localPort -> { OpensAt(localPort) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { forward() })
                )
                Button(
                    enabled = parsed != null,
                    onClick = withHaptic { forward() }
                ) { Text(stringResource(R.string.start)) }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DevicePreset.entries.forEach { preset ->
                    AssistChip(
                        onClick = withHaptic { onPreset(preset) },
                        label = { Text(stringResource(preset.labelRes())) }
                    )
                }
                TextButton(
                    onClick = withHaptic { onMoreOptions() }
                ) { Text(stringResource(R.string.port_forward_more_options)) }
            }
        }
    }
}
