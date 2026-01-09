package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
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
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    var showConnectedDeviceDialog by rememberSaveable { mutableStateOf(false) }
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

    LaunchedEffect(wifiAdbState) {
        when (wifiAdbState) {
            is WifiAdbState.Disconnected -> {
                disconnectedDeviceName = currentDevice?.deviceName
                dialogManager.show(DialogKey.WifiAdbScreen.DeviceDisconnected)
            }

            is WifiAdbState.ConnectSuccess -> {}

            else -> {}
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
        runCommandIfPermissionGranted = runCommandIfPermissionGranted,
        extraButtonContent = if (isConnected) {
            {
                IconButton(
                    onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        val deviceAddr = currentDevice?.let { "${it.ip}:${it.port}" } ?: ""
                        navController.navigate(NavRoutes.FileBrowserScreen(deviceAddr))
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_directory),
                        contentDescription = stringResource(R.string.file_browser),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else null
    )

    if (showConnectedDeviceDialog) {
        ConnectedDeviceDialog(
            connectedDevice = connectedDeviceName,
            onDismiss = { showConnectedDeviceDialog = false },
            showModeSwitchButton = false
        )
    }

    DialogKey.WifiAdbScreen.DeviceDisconnected.createDialog {
        DeviceDisconnectedDialog(
            onDismiss = { it.dismiss() }
        )
    }
}
