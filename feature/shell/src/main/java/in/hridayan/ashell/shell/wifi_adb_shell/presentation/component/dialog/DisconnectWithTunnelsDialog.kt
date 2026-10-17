package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.DeviceTunnelsViewModel

/**
 * Wraps a Wireless Debugging disconnect so that, while port-forwarding tunnels reach the device, the
 * user confirms first. Confirmed tunnels are stopped before the disconnect, so they do not sit
 * waiting for a device the user chose to leave.
 *
 * @return the action to call instead of [onDisconnect].
 */
@Composable
fun rememberTunnelAwareDisconnect(
    deviceName: String,
    onDisconnect: () -> Unit,
    viewModel: DeviceTunnelsViewModel = hiltViewModel()
): () -> Unit {
    val summary by viewModel.summary.collectAsState()
    val tunnelCount = summary.runningOn(ExternalDeviceTransport.WIFI_ADB)
    var confirming by remember { mutableStateOf(false) }

    if (confirming) {
        DisconnectWithTunnelsDialog(
            deviceName = deviceName,
            tunnelCount = tunnelCount,
            onConfirm = {
                confirming = false
                viewModel.stopTunnels(ExternalDeviceTransport.WIFI_ADB, onDisconnect)
            },
            onDismiss = { confirming = false }
        )
    }

    return { if (tunnelCount > 0) confirming = true else onDisconnect() }
}

@Composable
private fun DisconnectWithTunnelsDialog(
    deviceName: String,
    tunnelCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        AutoResizeableText(
            text = stringResource(R.string.disconnect_device_title, deviceName),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Text(
            text = pluralStringResource(R.plurals.disconnect_stops_tunnels, tunnelCount, tunnelCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OverflowButtonGroup(
            modifier = Modifier.padding(top = 24.dp),
            items = listOf(
                ButtonGroupItem(
                    buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss
                ),
                ButtonGroupItem(text = stringResource(R.string.disconnect), onClick = onConfirm)
            )
        )
    }
}
