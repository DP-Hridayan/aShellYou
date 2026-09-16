package `in`.hridayan.ashell.adbsideload.presentation.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.adbsideload.presentation.model.SideloadDeviceUiState
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R

@Composable
fun SideloadDeviceCard(
    device: SideloadDeviceUiState,
    modifier: Modifier = Modifier,
) {
    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CustomCardShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isReady) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConnectionIcon(isDetected = device.isDetected, isReady = device.isReady)
            ConnectionInfo(device = device)
        }
    }
}

@Composable
private fun ConnectionIcon(isDetected: Boolean, isReady: Boolean) {
    Icon(
        imageVector = if (isDetected) Icons.Default.Usb else Icons.Default.UsbOff,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint = if (isReady) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
}

@Composable
private fun ConnectionInfo(device: SideloadDeviceUiState) {
    val titleText = when {
        device.isDetected && device.deviceName != null -> device.deviceName
        device.isDetected -> stringResource(R.string.connected)
        else -> stringResource(R.string.no_device_connected)
    }
    val subtitleText = when {
        device.isConnecting -> stringResource(R.string.connecting)
        device.isReady -> stringResource(R.string.adb_connected_verify_sideload)
        device.errorText != null -> device.errorText
        device.wrongMode -> stringResource(R.string.device_not_in_sideload_mode)
        device.awaitingPermission -> stringResource(R.string.allow_usb_access_message)
        device.isDetected -> stringResource(R.string.waiting_for_device)
        else -> stringResource(R.string.sideload_device_hint)
    }
    val contentColor = if (device.isReady) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )
        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodySmall,
            color = if (device.isReady) {
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
