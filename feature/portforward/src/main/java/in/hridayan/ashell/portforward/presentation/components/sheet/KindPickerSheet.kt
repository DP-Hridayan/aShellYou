@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.portforward.presentation.components.sheet

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.presentation.components.editor.labelRes

/** Asks what a new tunnel reaches: something on the network, or one of the connected devices. */
@Composable
fun KindPickerSheet(
    devices: Map<ExternalDeviceTransport, ExternalDeviceState.Connected>,
    onNetwork: () -> Unit,
    onDevice: (ExternalDeviceTransport) -> Unit,
    onDismiss: () -> Unit
) {
    val count = devices.size + 1
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.new_tunnel),
                style = MaterialTheme.typography.headlineSmall
            )
            KindOption(
                icon = R.drawable.ic_language,
                title = stringResource(R.string.port_forward_kind_network),
                description = stringResource(R.string.port_forward_kind_network_description),
                index = 0,
                count = count,
                onClick = onNetwork
            )
            devices.entries.forEachIndexed { index, (transport, device) ->
                KindOption(
                    icon = R.drawable.ic_otg,
                    title = stringResource(R.string.port_forward_kind_device),
                    description = stringResource(
                        R.string.port_forward_device_chip,
                        device.displayName,
                        stringResource(transport.labelRes())
                    ),
                    index = index + 1,
                    count = count,
                    onClick = { onDevice(transport) }
                )
            }
        }
    }
}

@Composable
private fun KindOption(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    index: Int,
    count: Int,
    onClick: () -> Unit
) {
    CustomCard(
        modifier = Modifier.fillMaxWidth(),
        shape = getRoundedShape(index, count),
        onClick = withHaptic { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(icon), contentDescription = null)
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
