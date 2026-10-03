@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.AdbFileBrowserConnectionMode
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.shell.common.presentation.components.bottomsheet.ConnectedDeviceBottomSheet
import `in`.hridayan.ashell.shell.common.presentation.model.DeviceConnection
import `in`.hridayan.ashell.shell.common.presentation.screens.BaseShellScreen
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.DeviceDisconnectedDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel

@Composable
fun WifiAdbScreen(
    shellViewModel: ShellViewModel = hiltViewModel(),
    wifiAdbViewModel: WifiAdbViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val res = LocalResources.current
    var showConnectedDeviceSheet by rememberSaveable { mutableStateOf(false) }
    var showDeviceDisconnectedDialog by rememberSaveable { mutableStateOf(false) }

    val wifiAdbState by wifiAdbViewModel.state.collectAsState()
    val currentDevice by wifiAdbViewModel.currentDevice.collectAsState()

    val isConnected = wifiAdbState is WifiAdbState.Connected
    val connectedDeviceName = if (isConnected) {
        currentDevice?.deviceName ?: res.getString(R.string.none)
    } else {
        res.getString(R.string.none)
    }
    val lastConnectedDevice by wifiAdbViewModel.lastConnectedDevice.collectAsState()

    LaunchedEffect(wifiAdbState) {
        when (wifiAdbState) {
            is WifiAdbState.Disconnected -> {
                showDeviceDisconnectedDialog = true
            }

            is WifiAdbState.Connected -> {}

            else -> {}
        }
    }

    val modeButtonText = stringResource(R.string.wifi_adb)
    val modeButtonOnClick: () -> Unit = {
        showConnectedDeviceSheet = true
    }

    val runCommandIfPermissionGranted: () -> Unit = {
        shellViewModel.runWifiAdbCommand()
    }

    var isReconnecting by rememberSaveable { mutableStateOf(false) }
    val navController = LocalNavController.current

    BaseShellScreen(
        modeButtonText = modeButtonText,
        modeButtonOnClick = modeButtonOnClick,
        runCommandIfPermissionGranted = runCommandIfPermissionGranted,
        deviceConnection = when {
            !isConnected -> DeviceConnection.NONE
            currentDevice?.isOwnDevice == true -> DeviceConnection.OWN_DEVICE
            else -> DeviceConnection.OTHER_DEVICE
        },
        onOpenScreen = {
            navController.navigate(NavRoutes.MirrorScreen(ExternalDeviceTransport.WIFI_ADB))
        },
        onOpenFiles = {
            navController.navigate(
                NavRoutes.FileBrowserScreen(
                    deviceAddress = currentDevice?.let { "${it.ip}:${it.port}" } ?: "",
                    connectionMode = AdbFileBrowserConnectionMode.WIFI_ADB,
                    isOwnDevice = currentDevice?.isOwnDevice ?: false
                )
            )
        },
        extraButtonContent = {
            if (!isConnected) {
                IconButton(
                    onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        if (!context.isConnectedToWifi()) {
                            showToast(
                                context,
                                res.getString(R.string.no_wifi_connection)
                            )
                            return@withHaptic
                        }

                        if (lastConnectedDevice == null) {
                            showToast(
                                context,
                                res.getString(R.string.error)
                            )
                            return@withHaptic
                        }

                        lastConnectedDevice?.let { device ->
                            isReconnecting = true
                            wifiAdbViewModel.reconnectToDeviceWithCallback(
                                device = device,
                                onSuccess = {
                                    isReconnecting = false
                                    showToast(
                                        context,
                                        res.getString(R.string.reconnect_success),
                                    )
                                },
                                onFailure = { requiresPairing ->
                                    isReconnecting = false
                                    val message = if (requiresPairing) {
                                        res.getString(R.string.reconnect_failed_requires_pairing)
                                    } else {
                                        res.getString(R.string.reconnect_failed)
                                    }
                                    showToast(context, message)
                                }
                            )
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    enabled = !isReconnecting
                ) {
                    if (isReconnecting) {
                        LoadingIndicator(
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.reconnect),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    )

    if (showConnectedDeviceSheet) {
        ConnectedDeviceBottomSheet(
            connectedDevice = connectedDeviceName,
            onDismiss = { showConnectedDeviceSheet = false },
            showLocalAdbModes = false
        )
    }

    if (showDeviceDisconnectedDialog) {
        DeviceDisconnectedDialog(
            onDismiss = {
                showDeviceDisconnectedDialog = false
                if (WifiAdbConnection.currentState is WifiAdbState.Disconnected) {
                    WifiAdbConnection.updateState(WifiAdbState.Idle)
                }
            },
            onReconnected = { showDeviceDisconnectedDialog = false }
        )
    }
}
