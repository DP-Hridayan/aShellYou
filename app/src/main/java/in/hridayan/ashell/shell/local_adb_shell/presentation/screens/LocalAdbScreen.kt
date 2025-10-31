package `in`.hridayan.ashell.shell.local_adb_shell.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.common.constants.SHIZUKU_PACKAGE_NAME
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.domain.model.SharedTextHolder
import `in`.hridayan.ashell.core.presentation.utils.ToastUtils.makeToast
import `in`.hridayan.ashell.core.utils.DeviceUtils
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.core.utils.isAppInstalled
import `in`.hridayan.ashell.core.utils.launchApp
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.shell.local_adb_shell.presentation.components.dialog.ShizukuUnavailableDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.ConnectedDeviceDialog
import `in`.hridayan.ashell.shell.presentation.model.ShellState
import `in`.hridayan.ashell.shell.presentation.screens.BaseShellScreen
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

@Composable
fun LocalAdbScreen(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val hasShizukuPermission by shellViewModel.shizukuPermissionState.collectAsState()
    var isShizukuInstalled by rememberSaveable {
        mutableStateOf(
            context.isAppInstalled(
                SHIZUKU_PACKAGE_NAME
            )
        )
    }
    var hasRootAccess by rememberSaveable { mutableStateOf(false) }
    val localAdbMode = LocalSettings.current.localAdbMode
    var showConnectedDeviceDialog by rememberSaveable { mutableStateOf(false) }
    var showShizukuUnavailableDialog by rememberSaveable { mutableStateOf(false) }
    val states by shellViewModel.states.collectAsState()

    val runCommandIfPermissionGranted: () -> Unit =
        remember(localAdbMode, hasShizukuPermission, isShizukuInstalled) {
            {
                when (localAdbMode) {
                    LocalAdbWorkingMode.BASIC -> shellViewModel.runBasicCommand()

                    LocalAdbWorkingMode.SHIZUKU -> {
                        if (!Shizuku.pingBinder()) {
                            showShizukuUnavailableDialog = true
                            return@remember
                        }
                        if (!hasShizukuPermission) {
                            shellViewModel.requestShizukuPermission()
                        } else {
                            shellViewModel.runShizukuCommand()
                        }
                    }

                    LocalAdbWorkingMode.ROOT -> {
                        scope.launch {
                            scope.launch {
                                val hasRoot = withContext(Dispatchers.IO) {
                                    shellViewModel.hasRootAccess()
                                }
                                hasRootAccess = hasRoot

                                if (!hasRootAccess) {
                                    withContext(Dispatchers.Main) {
                                        makeToast(
                                            context,
                                            context.getString(R.string.no_root_access)
                                        )
                                        shellViewModel.onCommandTextFieldChange(
                                            newValue =
                                                TextFieldValue(""),
                                            isError = true,
                                            errorMessage = context.getString(R.string.no_root_access)
                                        )
                                    }
                                } else {
                                    shellViewModel.runRootCommand()
                                }
                            }

                        }
                    }
                }
            }
        }

    val modeButtonOnClick: () -> Unit = remember(states.shellState) {
        {
            if (states.shellState == ShellState.Busy) {
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

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isShizukuInstalled = context.isAppInstalled(SHIZUKU_PACKAGE_NAME)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        SharedTextHolder.text?.let {
            shellViewModel.onCommandTextFieldChange(newValue = TextFieldValue(it))
            shellViewModel.updateTextFieldSelection()
            SharedTextHolder.text = null
        }
    }

    BaseShellScreen(
        modifier = modifier,
        runCommandIfPermissionGranted = runCommandIfPermissionGranted,
        modeButtonOnClick = modeButtonOnClick,
        modeButtonText = modeButtonText,
    )

    if (showConnectedDeviceDialog) {
        ConnectedDeviceDialog(
            onDismiss = {
                showConnectedDeviceDialog = false
            },
            connectedDevice = DeviceUtils.DEVICE_MODEL
        )
    }

    if (showShizukuUnavailableDialog) {
        ShizukuUnavailableDialog(
            onDismiss = { showShizukuUnavailableDialog = false },
            onConfirm = {
                if (isShizukuInstalled) context.launchApp(SHIZUKU_PACKAGE_NAME)
                else UrlUtils.openUrl(
                    url = UrlConst.URL_SHIZUKU_SITE,
                    context = context
                )
            })
    }
}