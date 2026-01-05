@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.item.SavedDeviceItem

@Composable
fun SavedDevicesSection(
    modifier: Modifier = Modifier,
    savedDevices: List<WifiAdbDevice>,
    currentDevice: WifiAdbDevice?,
    wifiAdbState: WifiAdbState,
    onReconnect: (WifiAdbDevice) -> Unit,
    onForget: (WifiAdbDevice) -> Unit,
    onDisconnect: () -> Unit
) {
    AnimatedVisibility(
        visible = savedDevices.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.saved_devices),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                savedDevices.forEach { device ->
                    val isCurrentDevice = currentDevice?.id == device.id
                    val isReconnecting = wifiAdbState is WifiAdbState.Reconnecting &&
                            wifiAdbState.device == device.id

                    SavedDeviceItem(
                        device = device,
                        isConnected = isCurrentDevice && wifiAdbState is WifiAdbState.ConnectSuccess,
                        isReconnecting = isReconnecting,
                        onReconnect = { onReconnect(device) },
                        onForget = { onForget(device) },
                        onDisconnect = onDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

