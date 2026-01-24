@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.bottomsheet

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.core.utils.registerNetworkCallback
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.core.utils.unregisterNetworkCallback
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbEvent
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.ReconnectFailedDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.item.SavedDeviceItem
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.utils.WirelessDebuggingUtils

@Composable
fun SavedDevicesBottomSheet(
    onDismiss: () -> Unit,
    onGoToTerminal: () -> Unit,
    viewModel: WifiAdbViewModel = hiltViewModel(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val res = LocalResources.current
    val savedDevices by viewModel.savedDevices.collectAsState()
    val currentDevice by viewModel.currentDevice.collectAsState()
    val wifiAdbState by viewModel.state.collectAsState()

    var isWifiConnected by remember { mutableStateOf(context.isConnectedToWifi()) }

    var wasReconnectCancelled by remember { mutableStateOf(false) }
    var lastReconnectingDeviceId by remember { mutableStateOf<String?>(null) }

    var showReconnectFailedDialog by remember { mutableStateOf(false) }
    var showDevOptionsButton by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val callback = registerNetworkCallback(context) { isConnected ->
            isWifiConnected = isConnected
        }

        onDispose {
            unregisterNetworkCallback(context, callback)
        }
    }

    LaunchedEffect(wifiAdbState) {
        when (val state = wifiAdbState) {
            is WifiAdbState.Reconnecting -> {
                lastReconnectingDeviceId = state.deviceId
                wasReconnectCancelled = false
            }

            is WifiAdbState.Connected -> {
                wasReconnectCancelled = false
                showReconnectFailedDialog = false
            }

            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        WifiAdbConnection.events.collect { event ->
            when (event) {
                is WifiAdbEvent.ReconnectFailed -> {
                    if (!wasReconnectCancelled) {
                        val failedDevice = savedDevices.find { it.id == event.deviceId }
                        showDevOptionsButton =
                            failedDevice?.isOwnDevice == true || event.requiresPairing
                        showReconnectFailedDialog = true
                    }
                    wasReconnectCancelled = false
                }

                is WifiAdbEvent.WirelessDebuggingOff -> {
                    if (!wasReconnectCancelled) {
                        showDevOptionsButton = true
                        showReconnectFailedDialog = true
                    }
                    wasReconnectCancelled = false
                }

                is WifiAdbEvent.ReconnectSuccess -> {
                    wasReconnectCancelled = false
                    showReconnectFailedDialog = false
                }

                else -> {}
            }
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
                            (wifiAdbState as? WifiAdbState.Reconnecting)?.deviceId == device.id
                    val isConnected = currentDevice?.id == device.id &&
                            (wifiAdbState is WifiAdbState.Connected || viewModel.isConnected())

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SavedDeviceItem(
                            device = device,
                            isConnected = isConnected,
                            isReconnecting = isThisDeviceReconnecting,
                            onReconnect = {
                                // Check WiFi connectivity first
                                if (!isWifiConnected) {
                                    showToast(
                                        context,
                                        res.getString(R.string.connect_to_wifi_network)
                                    )
                                    return@SavedDeviceItem
                                }

                                // Reset state before new reconnect attempt
                                WifiAdbConnection.updateState(WifiAdbState.Idle)
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
                                } else {
                                    showToast(context, res.getString(R.string.reconnect_the_device))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Cancel button when reconnecting
                        if (isThisDeviceReconnecting) {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                shapes = ButtonDefaults.shapes(),
                                onClick = withHaptic(HapticFeedbackType.Reject) {
                                    wasReconnectCancelled = true
                                    viewModel.cancelReconnect()
                                }
                            ) {
                                AutoResizeableText(text = stringResource(R.string.cancel))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))
        }
    }

    if (showReconnectFailedDialog) {
        ReconnectFailedDialog(
            showDevOptionsButton = showDevOptionsButton,
            onConfirm = {

                WifiAdbConnection.updateState(WifiAdbState.Idle)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WirelessDebuggingUtils.ensureWirelessDebuggingAndReconnect(context, onSuccess = {
                        showReconnectFailedDialog = false
                    },
                        onFailed = {})
                }
            },
            onDismiss = {
                showReconnectFailedDialog = false
                WifiAdbConnection.updateState(WifiAdbState.Idle)
            }
        )
    }
}
