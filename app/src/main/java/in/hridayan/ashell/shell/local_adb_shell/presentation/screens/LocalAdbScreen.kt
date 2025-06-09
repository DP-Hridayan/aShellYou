package `in`.hridayan.ashell.shell.local_adb_shell.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.shell.presentation.screens.BaseShellScreen
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel

@Composable
fun LocalAdbScreen(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val hasPermission by shellViewModel.shizukuPermissionState.collectAsState()
    val isShizukuInstalled = remember { shellViewModel.isShizukuInstalled() }

    val localAdbMode = LocalSettings.current.localAdbMode

    val runCommandIfPermissionGranted: () -> Unit = remember {
        {
            when (localAdbMode) {
                LocalAdbWorkingMode.BASIC -> shellViewModel.runCommand()

                LocalAdbWorkingMode.SHIZUKU -> {
                    if (!isShizukuInstalled) {
                        //show shizuku dialog
                        return@remember
                    }
                    if (!hasPermission) {
                        shellViewModel.requestShizukuPermission()
                    } else {
                        shellViewModel.runCommand()
                    }
                }

                LocalAdbWorkingMode.ROOT -> {
                    shellViewModel.runCommand()
                }
            }
        }
    }

    BaseShellScreen(
        modifier = modifier,
        runCommandIfPermissionGranted = runCommandIfPermissionGranted
    )
}