package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.common.presentation.components.dialog.ConnectedDeviceDialog
import `in`.hridayan.ashell.shell.common.presentation.screens.BaseShellScreen
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.DeviceDisconnectedDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel

@Composable
fun WifiAdbScreen(
    shellViewModel: ShellViewModel = hiltViewModel(),
    wifiAdbViewModel: WifiAdbViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showConnectedDeviceDialog by rememberSaveable { mutableStateOf(false) }
    var showDisconnectedDialog by rememberSaveable { mutableStateOf(false) }
    var disconnectedDeviceName by rememberSaveable { mutableStateOf<String?>(null) }

    val wifiAdbState by wifiAdbViewModel.state.collectAsState()
    val currentDevice by wifiAdbViewModel.currentDevice.collectAsState()

    // Get device name from current device, or "None" if not connected
    val isConnected = wifiAdbState is WifiAdbState.ConnectSuccess
    val connectedDeviceName = if (isConnected) {
        currentDevice?.deviceName ?: context.getString(R.string.none)
    } else {
        context.getString(R.string.none)
    }

    // Watch for disconnect state changes (from heartbeat)
    LaunchedEffect(wifiAdbState) {
        if (wifiAdbState is WifiAdbState.Disconnected) {
            disconnectedDeviceName = currentDevice?.deviceName
            showDisconnectedDialog = true
        }
    }

    val modeButtonText = stringResource(R.string.wifi_adb)
    val modeButtonOnClick: () -> Unit = {
        showConnectedDeviceDialog = true
    }

    val runCommandIfPermissionGranted: () -> Unit = {
        shellViewModel.runWifiAdbCommand()
    }

    BaseShellScreen(
        modeButtonText = modeButtonText,
        modeButtonOnClick = modeButtonOnClick,
        runCommandIfPermissionGranted = runCommandIfPermissionGranted
    )

    if (showConnectedDeviceDialog) {
        ConnectedDeviceDialog(
            connectedDevice = connectedDeviceName,
            onDismiss = { showConnectedDeviceDialog = false },
            showModeSwitchButton = false
        )
    }

    if (showDisconnectedDialog) {
        DeviceDisconnectedDialog(
            deviceName = disconnectedDeviceName,
            onDismiss = { showDisconnectedDialog = false }
        )
    }
}