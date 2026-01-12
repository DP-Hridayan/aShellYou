@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.otg_adb_shell.presentation.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.shell.common.presentation.components.dialog.ConnectedDeviceDialog
import `in`.hridayan.ashell.shell.common.presentation.screens.BaseShellScreen
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConnectionMode
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.components.dialog.OtgDeviceWaitingDialog
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.viewmodel.OtgViewModel

@Composable
fun OtgAdbScreen(
    shellViewModel: ShellViewModel = hiltViewModel(),
    otgViewModel: OtgViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    var showConnectedDeviceDialog by rememberSaveable { mutableStateOf(false) }
    var showOtgDeviceWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var connectedDevice by rememberSaveable { mutableStateOf(context.getString(R.string.none)) }
    val otgState by otgViewModel.state.collectAsState()
    val modeButtonText = stringResource(R.string.otg)

    val isConnected = otgState is OtgState.Connected

    /**
     * Do not bother why we need this [disconnected] variable.
     * It is just a dirty setup for properly syncing the otg states after reconnection
     */
    var disconnected by rememberSaveable { mutableStateOf(false) }

    val modeButtonOnClick: () -> Unit = {
        if (otgState is OtgState.Connected || otgState is OtgState.DeviceFound) {
            showConnectedDeviceDialog = true
        } else {
            showOtgDeviceWaitingDialog = true
        }
    }

    val runCommandIfPermissionGranted: () -> Unit = {
        if (otgState is OtgState.Connected) {
            shellViewModel.runOtgCommand()
        } else {
            otgViewModel.startScan()
            showOtgDeviceWaitingDialog = true
            shellViewModel.onCommandTextFieldChange(
                newValue = TextFieldValue(""),
                isError = true,
                errorMessage = context.getString(R.string.waiting_for_device)
            )
        }
    }

    LaunchedEffect(otgState) {
        connectedDevice = when (otgState) {
            is OtgState.DeviceFound -> (otgState as OtgState.DeviceFound).deviceName
            is OtgState.Connected -> (otgState as OtgState.Connected).deviceName
            else -> context.getString(R.string.none)
        }

        if (!(otgState is OtgState.Connected || otgState is OtgState.DeviceFound) && !disconnected) {
            showOtgDeviceWaitingDialog = true
            disconnected = true
        }

        if (otgState is OtgState.Connected) disconnected = false
    }

    BaseShellScreen(
        modeButtonText = modeButtonText,
        modeButtonOnClick = modeButtonOnClick,
        runCommandIfPermissionGranted = runCommandIfPermissionGranted,
        extraButtonContent = if (isConnected) {
            {
                IconButton(
                    onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        navController.navigate(
                            NavRoutes.FileBrowserScreen(
                                deviceAddress = connectedDevice,
                                connectionMode = ConnectionMode.OTG_ADB,
                                isOwnDevice = false
                            )
                        )
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
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
            connectedDevice = connectedDevice,
            onDismiss = { showConnectedDeviceDialog = false },
            showModeSwitchButton = false
        )
    }

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