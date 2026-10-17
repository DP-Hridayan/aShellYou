@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.portforward.presentation.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.DevicePreset
import `in`.hridayan.ashell.portforward.domain.usecase.ValidateTunnelFormUseCase.FieldError
import `in`.hridayan.ashell.portforward.presentation.model.EditorDevice

private val IconContainerPadding = 10.dp
private val IconSize = 22.dp
private val ChipGap = 8.dp

/** The device a tunnel reaches, how it is connected, and whether it is connected right now. */
@Composable
fun DeviceHeader(device: EditorDevice, connected: Boolean) {
    CustomCard(modifier = Modifier.fillMaxWidth(), shape = GroupPosition.Single.card, clickable = false) {
        Row(
            modifier = Modifier.padding(
                horizontal = EditorSpacing.CardHorizontal,
                vertical = EditorSpacing.CardVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(EditorSpacing.ContentGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(IconContainerPadding)
            ) {
                Icon(
                    modifier = Modifier.size(IconSize),
                    painter = painterResource(device.transport.iconRes()),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        if (connected) device.transport.labelRes() else R.string.port_forward_device_not_connected_now
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (connected) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

/** Shortcuts that fill the device socket and local port for common tools. */
@Composable
fun PresetChips(onPreset: (DevicePreset) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = EditorSpacing.BlockGap),
        contentPadding = PaddingValues(horizontal = EditorSpacing.SectionLabelInset),
        horizontalArrangement = Arrangement.spacedBy(ChipGap)
    ) {
        items(DevicePreset.entries) { preset ->
            AssistChip(onClick = withHaptic { onPreset(preset) }, label = { Text(stringResource(preset.labelRes())) })
        }
    }
}

/** The kind of device socket, as connected toggle buttons: one choice, always visible. */
@Composable
fun EndpointTypeSelector(type: DeviceEndpointType, onTypeChange: (DeviceEndpointType) -> Unit) {
    val types = DeviceEndpointType.entries
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        types.forEachIndexed { index, option ->
            val select = withHaptic { onTypeChange(option) }
            ToggleButton(
                modifier = Modifier.weight(1f),
                checked = option == type,
                onCheckedChange = { select() },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    types.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                Text(text = stringResource(option.shortLabelRes()), maxLines = 1)
            }
        }
    }
}

/** The port, socket name, socket path or process id, labelled for the chosen socket kind. */
@Composable
fun EndpointValueField(
    type: DeviceEndpointType,
    value: String,
    error: FieldError?,
    position: GroupPosition,
    onValueChange: (String) -> Unit
) {
    if (type == DeviceEndpointType.TCP || type == DeviceEndpointType.JDWP) {
        PortField(
            value = value,
            label = stringResource(type.valueLabelRes()),
            position = position,
            isError = error != null,
            onValueChange = onValueChange
        )
    } else {
        EditorTextField(
            value = value,
            onValueChange = onValueChange,
            label = stringResource(type.valueLabelRes()),
            position = position,
            isError = error != null,
            monospace = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next)
        )
    }
}

private fun ExternalDeviceTransport.iconRes(): Int = when (this) {
    ExternalDeviceTransport.OTG -> R.drawable.ic_otg
    ExternalDeviceTransport.WIFI_ADB -> R.drawable.ic_wifi_settings
}

private fun DeviceEndpointType.shortLabelRes(): Int = when (this) {
    DeviceEndpointType.TCP -> R.string.port_forward_type_tcp_short
    DeviceEndpointType.ABSTRACT -> R.string.port_forward_type_abstract_short
    DeviceEndpointType.FILESYSTEM -> R.string.port_forward_type_filesystem_short
    DeviceEndpointType.JDWP -> R.string.port_forward_type_jdwp_short
}

fun DeviceEndpointType.labelRes(): Int = when (this) {
    DeviceEndpointType.TCP -> R.string.port_forward_type_tcp
    DeviceEndpointType.ABSTRACT -> R.string.port_forward_type_abstract
    DeviceEndpointType.FILESYSTEM -> R.string.port_forward_type_filesystem
    DeviceEndpointType.JDWP -> R.string.port_forward_type_jdwp
}

private fun DeviceEndpointType.valueLabelRes(): Int = when (this) {
    DeviceEndpointType.TCP -> R.string.port_forward_device_port
    DeviceEndpointType.ABSTRACT -> R.string.port_forward_socket_name
    DeviceEndpointType.FILESYSTEM -> R.string.port_forward_socket_path
    DeviceEndpointType.JDWP -> R.string.port_forward_process_id
}

fun DevicePreset.labelRes(): Int = when (this) {
    DevicePreset.CHROME_DEVTOOLS -> R.string.port_forward_preset_chrome_devtools
    DevicePreset.FRIDA -> R.string.port_forward_preset_frida
}

fun ExternalDeviceTransport.labelRes(): Int = when (this) {
    ExternalDeviceTransport.OTG -> R.string.port_forward_transport_usb
    ExternalDeviceTransport.WIFI_ADB -> R.string.port_forward_transport_wifi
}
