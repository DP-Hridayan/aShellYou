package `in`.hridayan.ashell.shell.local_adb_shell.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.utils.DeviceUtils
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.shell.domain.model.ShellState
import `in`.hridayan.ashell.shell.local_adb_shell.presentation.components.dialog.ConnectedDeviceDialog
import `in`.hridayan.ashell.shell.presentation.screens.BaseShellScreen
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel

@Composable
fun LocalAdbScreen(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val hasShizukuPermission by shellViewModel.shizukuPermissionState.collectAsState()
    val isShizukuInstalled = remember { shellViewModel.isShizukuInstalled() }

    val localAdbMode = LocalSettings.current.localAdbMode
    var showConnectedDeviceDialog by rememberSaveable { mutableStateOf(false) }

    val shellState = shellViewModel.shellState.collectAsState()

    val runCommandIfPermissionGranted: () -> Unit =
        remember(localAdbMode, hasShizukuPermission, isShizukuInstalled) {
            {
                when (localAdbMode) {
                    LocalAdbWorkingMode.BASIC -> shellViewModel.runBasicCommand()

                    LocalAdbWorkingMode.SHIZUKU -> {
                        if (!isShizukuInstalled) {
                            //show shizuku dialog
                            return@remember
                        }
                        if (!hasShizukuPermission) {
                            shellViewModel.requestShizukuPermission()
                        } else {
                            shellViewModel.runShizukuCommand()
                        }
                    }

                    LocalAdbWorkingMode.ROOT -> {
                        shellViewModel.runRootCommand()
                    }
                }
            }
        }

    val modeButtonOnClick: () -> Unit = remember(shellState.value) {
        {
            if (shellState.value == ShellState.Busy) {
                showToast(context, context.getString(R.string.abort_command))
            } else {
                showConnectedDeviceDialog = true
            }
        }
    }

    val modeButtonText = when (localAdbMode) {
        LocalAdbWorkingMode.BASIC -> stringResource(R.string.basic_shell)
        LocalAdbWorkingMode.SHIZUKU -> stringResource(R.string.shizuku)
        LocalAdbWorkingMode.ROOT -> stringResource(R.string.root)
        else -> {
            ""
        }
    }

    BaseShellScreen(
        modifier = modifier,
        runCommandIfPermissionGranted = runCommandIfPermissionGranted,
        modeButtonOnClick = modeButtonOnClick,
        modeButtonText = modeButtonText,
        extraContent = {
            if (showConnectedDeviceDialog) {
                ConnectedDeviceDialog(
                    onDismiss = {
                        showConnectedDeviceDialog = false
                    },
                    connectedDevice = DeviceUtils.DEVICE_MODEL
                )
            }
        }
    )
}