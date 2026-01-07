@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.ReconnectFailedDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.item.SavedDeviceItem
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel

@Composable
fun SavedDevicesBottomSheet(
    onDismiss: () -> Unit,
    onGoToTerminal: () -> Unit,
    viewModel: WifiAdbViewModel = hiltViewModel(),
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val savedDevices by viewModel.savedDevices.collectAsState()
    val currentDevice by viewModel.currentDevice.collectAsState()
    val wifiAdbState by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Track if reconnect was manually cancelled (to avoid showing dialog on cancel)
    var wasReconnectCancelled by remember { mutableStateOf(false) }
    var lastReconnectingDeviceId by remember { mutableStateOf<String?>(null) }

    // Track dialog visibility with state instead of DialogManager
    var showReconnectFailedDialog by remember { mutableStateOf(false) }
    var showDevOptionsButton by remember { mutableStateOf(false) }

    // Handle state changes for showing dialogs
    LaunchedEffect(wifiAdbState) {
        val state = wifiAdbState
        when (state) {
            is WifiAdbState.Reconnecting -> {
                lastReconnectingDeviceId = state.device
                wasReconnectCancelled = false
            }

            is WifiAdbState.ConnectSuccess -> {
                wasReconnectCancelled = false
                showReconnectFailedDialog = false
            }

            is WifiAdbState.ConnectFailed -> {
                if (!wasReconnectCancelled) {
                    val failedDevice = savedDevices.find { it.id == state.device }
                    showDevOptionsButton = failedDevice?.isOwnDevice == true
                    showReconnectFailedDialog = true
                }
                wasReconnectCancelled = false
            }

            is WifiAdbState.WirelessDebuggingOff -> {
                if (!wasReconnectCancelled) {
                    showDevOptionsButton = true
                    showReconnectFailedDialog = true
                }
                wasReconnectCancelled = false
            }

            else -> {}
        }
    }

    val isReconnecting = wifiAdbState is WifiAdbState.Reconnecting

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.choose_a_device),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 25.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedDevices, key = { it.id }) { device ->
                    val isThisDeviceReconnecting = isReconnecting &&
                            (wifiAdbState as? WifiAdbState.Reconnecting)?.device == device.id
                    val isConnected = currentDevice?.id == device.id &&
                            wifiAdbState is WifiAdbState.ConnectSuccess

                    SavedDeviceItem(
                        device = device,
                        isConnected = isConnected,
                        isReconnecting = isThisDeviceReconnecting,
                        onReconnect = {
                            // Reset state before new reconnect attempt
                            WifiAdbConnection.updateState(WifiAdbState.None)
                            showReconnectFailedDialog = false

                            // If already reconnecting to a different device, mark as cancelled
                            if (lastReconnectingDeviceId != null && lastReconnectingDeviceId != device.id) {
                                wasReconnectCancelled = true
                            }
                            viewModel.reconnectToDevice(device)
                        },
                        onDisconnect = { viewModel.disconnect() },
                        onForget = { viewModel.forgetDevice(device) },
                        onClick = {
                            if (isConnected) {
                                onGoToTerminal()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))
        }
    }

    // Reconnect Failed Dialog - using state-driven visibility
    if (showReconnectFailedDialog) {
        ReconnectFailedDialog(
            showDevOptionsButton = showDevOptionsButton,
            onConfirm = {
                showReconnectFailedDialog = false
                // Reset state to allow retry
                WifiAdbConnection.updateState(WifiAdbState.None)
            },
            onDismiss = {
                showReconnectFailedDialog = false
                // Reset state to allow retry
                WifiAdbConnection.updateState(WifiAdbState.None)
            }
        )
    }
}


