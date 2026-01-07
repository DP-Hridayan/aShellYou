@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.home.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.constants.UrlConst.URL_OTG_INSTRUCTIONS
import `in`.hridayan.ashell.core.common.constants.UrlConst.URL_WIRELESS_DEBUGGING_INSTRUCTIONS
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.button.OutlinedIconButtonWithText
import `in`.hridayan.ashell.core.presentation.components.card.NavigationCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.core.presentation.utils.ToastUtils.makeToast
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.home.presentation.component.card.DeviceInfoCard
import `in`.hridayan.ashell.home.presentation.component.card.RebootOptionsCard
import `in`.hridayan.ashell.home.presentation.component.card.SystemSettings
import `in`.hridayan.ashell.home.presentation.component.dialog.RebootOptionsDialog
import `in`.hridayan.ashell.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.components.dialog.OtgDeviceWaitingDialog
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.viewmodel.OtgViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.bottomsheet.SavedDevicesBottomSheet
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.PairModeChooseDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    otgViewModel: OtgViewModel = hiltViewModel(),
    wifiAdbViewModel: WifiAdbViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    val otgState by otgViewModel.state.collectAsState()
    val savedDevices by wifiAdbViewModel.savedDevices.collectAsState()
    var showSavedDevicesBottomSheet by rememberSaveable { mutableStateOf(false) }

    val onClickOtgAdbCard: () -> Unit = {
        if (otgState is OtgState.Connected) {
            navController.navigate(NavRoutes.OtgAdbScreen)
        } else {
            otgViewModel.startScan()
            dialogManager.show(DialogKey.Home.OtgDeviceWaiting)
        }
    }

    val onClickRebootOptions: () -> Unit = {
        scope.launch {
            val hasRoot = withContext(Dispatchers.IO) {
                viewModel.requestRootAccess()
            }

            if (!hasRoot) {
                makeToast(context, context.getString(R.string.no_root_access))
            } else {
                makeToast(context, context.getString(R.string.root_access_granted))
                dialogManager.show(DialogKey.Home.RebootOptions)
            }
        }
    }

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(Dimens.paddingExtraLarge),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                AppNameText(modifier = Modifier.weight(1f))
                SettingsButton(onClick = withHaptic {
                    navController.navigate(NavRoutes.SettingsScreen)
                })
            }

            LocalAdbCard(
                modifier = Modifier.padding(top = 10.dp),
                onClick = {
                    navController.navigate(NavRoutes.LocalAdbScreen)
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WirelessDebuggingCard(
                    onStartClick = {
                        if (savedDevices.count() == 0) {
                            showToast(context, context.getString(R.string.pair_a_device_first))
                            return@WirelessDebuggingCard
                        }

                        showSavedDevicesBottomSheet = true
                    }
                )
            }

            OtgAdbCard(
                onClickOtgAdbCard = onClickOtgAdbCard,
                otgState = otgState
            )
            //  QuickToolsCard(onClickRebootOptions = onClickRebootOptions)
        }
    }

    when (dialogManager.activeDialog) {
        DialogKey.Home.RebootOptions -> RebootOptionsDialog(onDismiss = { dialogManager.dismiss() })

        DialogKey.Home.ChooseWifiAdbPairMode -> PairModeChooseDialog(
            onDismiss = { dialogManager.dismiss() },
            onClickPairSelf = {
                dialogManager.dismiss()
                navController.navigate(NavRoutes.PairingOwnDeviceScreen)
            },
            onClickPairAnother = {
                dialogManager.dismiss()
                navController.navigate(NavRoutes.PairingOtherDeviceScreen)
            })

        DialogKey.Home.OtgDeviceWaiting -> OtgDeviceWaitingDialog(
            onDismiss = { dialogManager.dismiss() },
            onConfirm = {
                dialogManager.dismiss()
                navController.navigate(NavRoutes.OtgAdbScreen)
                otgViewModel.startScan()
            }
        )

        else -> dialogManager.dismiss()
    }

    if (showSavedDevicesBottomSheet) {
        SavedDevicesBottomSheet(
            onDismiss = { showSavedDevicesBottomSheet = false },
            onGoToTerminal = {
                showSavedDevicesBottomSheet = false
                navController.navigate(NavRoutes.WifiAdbScreen)
            }
        )
    }
}

@Composable
private fun SettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Image(
        painter = painterResource(R.drawable.ic_settings),
        contentDescription = null,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}

@Composable
private fun AppNameText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier
            .alpha(0.9f)
    )
}

@Composable
fun LocalAdbCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    NavigationCard(
        title = stringResource(R.string.local_adb),
        description = stringResource(R.string.local_adb_summary),
        icon = painterResource(R.drawable.ic_adb2),
        modifier = modifier,
        onClick = onClick,
    )
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun WirelessDebuggingCard(
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit
) {
    val context = LocalContext.current
    val dialogManager = LocalDialogManager.current

    NavigationCard(
        title = stringResource(R.string.adb_via_wireless_debugging),
        description = stringResource(R.string.adb_via_wireless_debugging_summary),
        icon = painterResource(R.drawable.ic_wireless),
        showNavigationArrowIcon = false,
        modifier = modifier
    ) {
        IconWithTextButton(
            modifier = Modifier.padding(top = 35.dp),
            icon = painterResource(R.drawable.ic_pair),
            text = stringResource(R.string.pair),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            contentDescription = null,
            onClick = withHaptic {
                dialogManager.show(DialogKey.Home.ChooseWifiAdbPairMode)
            })

        IconWithTextButton(
            modifier = Modifier.padding(vertical = 5.dp),
            icon = painterResource(R.drawable.ic_play),
            text = stringResource(R.string.start),
            contentDescription = null,
            onClick = withHaptic { onStartClick() })

        OutlinedIconButtonWithText(
            text = stringResource(R.string.instructions),
            painter = painterResource(R.drawable.ic_open_in_new),
            onClick = withHaptic {
                UrlUtils.openUrl(
                    url = URL_WIRELESS_DEBUGGING_INSTRUCTIONS,
                    context = context
                )
            })

    }
}

@Composable
fun OtgAdbCard(
    modifier: Modifier = Modifier,
    otgState: OtgState,
    onClickOtgAdbCard: () -> Unit = {}
) {
    NavigationCard(
        title = stringResource(R.string.adb_through_otg),
        description = stringResource(R.string.adb_through_otg_summary),
        icon = painterResource(R.drawable.ic_otg),
        modifier = modifier,
        onClick = withHaptic {
            onClickOtgAdbCard()
        },
        content = {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp)
            )

            if (otgState is OtgState.Connected) {
                TextButton(
                    modifier = Modifier.padding(bottom = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic {
                        onClickOtgAdbCard()
                    }
                ) {
                    Text(
                        text = otgState.deviceName,
                        style = MaterialTheme.typography.bodyMediumEmphasized
                    )
                }
            }

            OtgInstructionButton()
        })
}

@Composable
fun OtgInstructionButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    OutlinedIconButtonWithText(
        modifier = modifier,
        text = stringResource(R.string.instructions),
        painter = painterResource(R.drawable.ic_open_in_new),
        onClick = {
            UrlUtils.openUrl(
                url = URL_OTG_INSTRUCTIONS,
                context = context
            )
        })
}

@Composable
private fun QuickToolsCard(
    modifier: Modifier = Modifier,
    onClickDeviceInfo: () -> Unit = {},
    onClickSystemSettings: () -> Unit = {},
    onClickRebootOptions: () -> Unit = {}
) {
    NavigationCard(
        icon = painterResource(R.drawable.ic_handyman),
        title = stringResource(R.string.quick_tools),
        showNavigationArrowIcon = false,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeviceInfoCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RebootOptionsCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    onClick = onClickRebootOptions
                )

                SystemSettings(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
            }
        }
    }
}
