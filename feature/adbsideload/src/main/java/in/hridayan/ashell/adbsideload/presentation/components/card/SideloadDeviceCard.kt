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
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R

@Composable
fun SideloadDeviceCard(
    isDetected: Boolean,
    isConnecting: Boolean,
    deviceName: String?,
    modifier: Modifier = Modifier,
) {
    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CustomCardShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDetected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConnectionIcon(isDetected = isDetected)
            ConnectionInfo(
                isDetected = isDetected,
                isConnecting = isConnecting,
                deviceName = deviceName
            )
        }
    }
}

@Composable
private fun ConnectionIcon(isDetected: Boolean) {
    Icon(
        imageVector = if (isDetected) Icons.Default.Usb else Icons.Default.UsbOff,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint = if (isDetected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ConnectionInfo(
    isDetected: Boolean,
    isConnecting: Boolean,
    deviceName: String?,
) {
    val titleText = when {
        isDetected && deviceName != null -> deviceName
        isDetected -> stringResource(R.string.connected)
        else -> stringResource(R.string.no_device_connected)
    }
    val subtitleText = when {
        isConnecting -> stringResource(R.string.connecting)
        isDetected -> stringResource(R.string.adb_connected_verify_sideload)
        else -> stringResource(R.string.sideload_device_hint)
    }
    val contentColor = if (isDetected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )
        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDetected)
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
