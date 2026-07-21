package `in`.hridayan.ashell.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.domain.model.FastbootState
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.home.presentation.screens.HomeScreen
import `in`.hridayan.ashell.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.ashell.logcat.presentation.viewmodel.LogcatViewModel
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootDeviceWaitingDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.viewmodel.FastbootViewModel
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.components.dialog.OtgDeviceWaitingDialog
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.viewmodel.OtgViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.bottomsheet.SavedDevicesBottomSheet
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.PairModeChooseDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = hiltViewModel(),
    otgViewModel: OtgViewModel = hiltViewModel(),
    wifiAdbViewModel: WifiAdbViewModel = hiltViewModel(),
    fastbootViewModel: FastbootViewModel = hiltViewModel(),
    logcatViewModel: LogcatViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val otgState by otgViewModel.state.collectAsState()
    val fastbootState by fastbootViewModel.state.collectAsState()
    val savedDevices by wifiAdbViewModel.savedDevices.collectAsState()
    val settings = LocalSettings.current
    val localAdbWorkingMode = settings[SettingsKeys.LocalAdbWorkingMode]
    val isLogcatRunning by logcatViewModel.isRunning.collectAsState()

    var showSavedDevicesBottomSheet by remember { mutableStateOf(false) }
    var showPairModeChooseDialog by remember { mutableStateOf(false) }
    var showOtgDeviceWaitingDialog by remember { mutableStateOf(false) }
    var showFastbootDeviceWaitingDialog by remember { mutableStateOf(false) }

    HomeScreen(
        localAdbWorkingMode = localAdbWorkingMode,
        savedDevicesCount = savedDevices.size,
        isLogcatRunning = isLogcatRunning,
        onOtgClick = {
            if (otgState is OtgState.Connected) {
                navController.navigate(NavRoutes.OtgAdbScreen)
            } else {
                otgViewModel.startScan()
                showOtgDeviceWaitingDialog = true
            }
        },
        onFastbootClick = {
            if (fastbootState is FastbootState.Connected) {
                navController.navigate(NavRoutes.FastbootScreen)
            } else {
                fastbootViewModel.startScan()
                showFastbootDeviceWaitingDialog = true
            }
        },
        onWifiAdbClick = {
            showSavedDevicesBottomSheet = true
        },
        onWifiAdbPairClick = {
            showPairModeChooseDialog = true
        },
        onSettingsClick = {
            navController.navigate(NavRoutes.SettingsScreen())
        },
        onLocalAdbClick = {
            navController.navigate(NavRoutes.LocalAdbScreen)
        },
        onLogcatClick = {
            navController.navigate(NavRoutes.LogcatScreen)
        },
        onReboot = { cmd ->
            homeViewModel.reboot(cmd)
        }
    )

    if (showPairModeChooseDialog) {
        PairModeChooseDialog(
            onDismiss = { showPairModeChooseDialog = false },
            onClickPairSelf = {
                showPairModeChooseDialog = false
                navController.navigate(NavRoutes.PairingOwnDeviceScreen)
            },
            onClickPairAnother = {
                showPairModeChooseDialog = false
                navController.navigate(NavRoutes.PairingOtherDeviceScreen)
            }
        )
    }

    if (showOtgDeviceWaitingDialog) {
        OtgDeviceWaitingDialog(
            onDismiss = { showOtgDeviceWaitingDialog = false },
            onConfirm = {
                showOtgDeviceWaitingDialog = false
                navController.navigate(NavRoutes.OtgAdbScreen)
                otgViewModel.startScan()
            }
        )
    }

    if (showFastbootDeviceWaitingDialog) {
        FastbootDeviceWaitingDialog(
            onDismiss = { showFastbootDeviceWaitingDialog = false },
            onConfirm = {
                showFastbootDeviceWaitingDialog = false
                navController.navigate(NavRoutes.FastbootScreen)
                fastbootViewModel.startScan()
            },
            isAdbDeviceConnected = otgState is OtgState.Connected,
            adbDeviceName = (otgState as? OtgState.Connected)?.deviceName,
            onBootIntoFastboot = {
                otgViewModel.rebootToBootloader()
                showFastbootDeviceWaitingDialog = false
                fastbootViewModel.startScan()
                showFastbootDeviceWaitingDialog = true
            }
        )
    }

    if (showSavedDevicesBottomSheet) {
        SavedDevicesBottomSheet(
            onDismiss = { showSavedDevicesBottomSheet = false },
            onGoToTerminal = {
                showSavedDevicesBottomSheet = false
                navController.navigate(NavRoutes.WifiAdbScreen())
            }
        )
    }
}
