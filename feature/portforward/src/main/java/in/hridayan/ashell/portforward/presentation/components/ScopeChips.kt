package `in`.hridayan.ashell.portforward.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.presentation.components.editor.labelRes
import `in`.hridayan.ashell.portforward.presentation.model.PortsScope

/**
 * Filters the list to all tunnels, network tunnels, or one device's tunnels. A device chip shows
 * while that device is connected, and stays while it is the selected scope.
 */
@Composable
fun ScopeChips(
    scope: PortsScope,
    devices: Map<ExternalDeviceTransport, ExternalDeviceState.Connected>,
    onScopeChange: (PortsScope) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScopeChip(stringResource(R.string.all), scope == PortsScope.All) { onScopeChange(PortsScope.All) }
        ScopeChip(stringResource(R.string.network), scope == PortsScope.Network) { onScopeChange(PortsScope.Network) }
        ExternalDeviceTransport.entries.forEach { transport ->
            val device = devices[transport]
            val selected = scope == PortsScope.Device(transport)
            if (device != null || selected) {
                ScopeChip(deviceLabel(transport, device), selected) { onScopeChange(PortsScope.Device(transport)) }
            }
        }
    }
}

@Composable
private fun ScopeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = withHaptic { onClick() }, label = { Text(label) })
}

@Composable
private fun deviceLabel(transport: ExternalDeviceTransport, device: ExternalDeviceState.Connected?): String {
    val transportLabel = stringResource(transport.labelRes())
    return device?.let { stringResource(R.string.port_forward_device_chip, it.displayName, transportLabel) }
        ?: transportLabel
}
