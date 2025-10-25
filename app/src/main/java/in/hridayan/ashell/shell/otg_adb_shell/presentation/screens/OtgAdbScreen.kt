package `in`.hridayan.ashell.shell.otg_adb_shell.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.components.dialog.OtgDeviceWaitingDialog
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.viewmodel.OtgViewModel
import `in`.hridayan.ashell.shell.presentation.components.dialog.ConnectedDeviceDialog
import `in`.hridayan.ashell.shell.presentation.screens.BaseShellScreen
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel

@Composable
fun OtgAdbScreen(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel(),
    otgViewModel: OtgViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showConnectedDeviceDialog by rememberSaveable { mutableStateOf(false) }
    var showOtgDeviceWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var connectedDevice by rememberSaveable { mutableStateOf(context.getString(R.string.none)) }

    val otgState by otgViewModel.state.collectAsState()
    val modeButtonText = stringResource(R.string.otg)
    val modeButtonOnClick: () -> Unit = {
        if (otgState is OtgState.Connected) {
            connectedDevice = (otgState as OtgState.Connected).deviceName
            showConnectedDeviceDialog = true
        } else {
            showOtgDeviceWaitingDialog = true
        }
    }


    BaseShellScreen(
        modeButtonText = modeButtonText,
        modeButtonOnClick = modeButtonOnClick
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
            onConfirm = { showOtgDeviceWaitingDialog = false }
        )
    }
}