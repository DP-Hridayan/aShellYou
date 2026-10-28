package `in`.hridayan.ashell.shell.otg_adb_shell.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.AdbFileBrowserConnectionMode
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.dialog.OtgDeviceWaitingDialog
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.ui.otg.OtgViewModel
import `in`.hridayan.ashell.shell.common.presentation.model.DeviceConnection
import `in`.hridayan.ashell.shell.common.presentation.screens.BaseShellScreen
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.DeviceTunnelsViewModel
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel

@Composable
fun OtgAdbScreen(
    shellViewModel: ShellViewModel = hiltViewModel(),
    otgViewModel: OtgViewModel = hiltViewModel(),
    deviceTunnelsViewModel: DeviceTunnelsViewModel = hiltViewModel()
) {
    val tunnelSummary by deviceTunnelsViewModel.summary.collectAsState()
    val res = LocalResources.current
    val navController = LocalNavController.current
    var showOtgDeviceWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var connectedDevice by rememberSaveable { mutableStateOf(res.getString(R.string.none)) }
    val otgState by otgViewModel.state.collectAsState()

    val isConnected = otgState is OtgState.Connected

    /**
     * Do not bother why we need this [disconnected] variable.
     * It is just a dirty setup for properly syncing the otg states after reconnection
     */
    var disconnected by rememberSaveable { mutableStateOf(false) }

    val runCommandIfPermissionGranted: () -> Unit = {
        if (otgState is OtgState.Connected) {
            shellViewModel.runOtgCommand()
        } else {
            otgViewModel.startScan()
            showOtgDeviceWaitingDialog = true
            shellViewModel.onCommandTextFieldChange(
                newValue = TextFieldValue(""),
                isError = true,
                errorMessage = res.getString(R.string.waiting_for_device)
            )
        }
    }

    LaunchedEffect(otgState) {
        connectedDevice = when (otgState) {
            is OtgState.DeviceFound -> (otgState as OtgState.DeviceFound).deviceName
            is OtgState.Connected -> (otgState as OtgState.Connected).deviceName
            else -> res.getString(R.string.none)
        }

        if (!(otgState is OtgState.Connected || otgState is OtgState.DeviceFound) && !disconnected) {
            showOtgDeviceWaitingDialog = true
            disconnected = true
        }

        if (otgState is OtgState.Connected) disconnected = false
    }

    BaseShellScreen(
        modeText = stringResource(R.string.otg),
        connectedDevice = connectedDevice,
        showLocalAdbModes = false,
        runCommandIfPermissionGranted = runCommandIfPermissionGranted,
        deviceConnection = if (isConnected) DeviceConnection.OTHER_DEVICE else DeviceConnection.NONE,
        onOpenScreen = { navController.navigate(NavRoutes.MirrorScreen(ExternalDeviceTransport.OTG)) },
        onOpenPorts = { navController.navigate(NavRoutes.PortsScreen(ExternalDeviceTransport.OTG)) },
        portsBadgeCount = tunnelSummary.runningOn(ExternalDeviceTransport.OTG),
        onOpenFiles = {
            navController.navigate(
                NavRoutes.FileBrowserScreen(
                    deviceAddress = connectedDevice,
                    connectionMode = AdbFileBrowserConnectionMode.OTG_ADB,
                    isOwnDevice = false
                )
            )
        }
    )

    if (showOtgDeviceWaitingDialog) {
        OtgDeviceWaitingDialog(
            onDismiss = { showOtgDeviceWaitingDialog = false },
            onConfirm = {
                showOtgDeviceWaitingDialog = false
                if (disconnected) {
                    otgViewModel.startScan()
                }
            }
        )
    }
}
