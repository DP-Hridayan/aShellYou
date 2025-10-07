@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.home.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.constants.UrlConst.URL_OTG_INSTRUCTIONS
import `in`.hridayan.ashell.core.common.constants.UrlConst.URL_WIRELESS_DEBUGGING_INSTRUCTIONS
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.button.OutlinedIconButtonWithText
import `in`.hridayan.ashell.core.presentation.components.card.NavigationCard
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.home.presentation.component.card.DeviceInfoCard
import `in`.hridayan.ashell.home.presentation.component.card.RebootOptionsCard
import `in`.hridayan.ashell.home.presentation.component.card.SystemSettings
import `in`.hridayan.ashell.home.presentation.component.dialog.RebootOptionsDialog
import `in`.hridayan.ashell.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.presentation.component.dialog.PairModeChooseDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    var showRebootOptionsDialog by rememberSaveable { mutableStateOf(false) }
    var showPairModeChooseDialog by rememberSaveable { mutableStateOf(false) }

    val onClickWifiAdbPairButton: () -> Unit = {
        showPairModeChooseDialog = true
    }
    val onClickWifiAdbStartButton: () -> Unit = {
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
                SettingsButton(onClick = {
                    weakHaptic()
                    navController.navigate(NavRoutes.SettingsScreen)
                })
            }

            LocalAdbCard(
                modifier = Modifier.padding(top = 10.dp),
                onClick = {
                    navController.navigate(NavRoutes.LocalAdbScreen)
                }
            )

            WirelessDebuggingCard(
                onClickStart = onClickWifiAdbStartButton,
                onClickPair = onClickWifiAdbPairButton
            )
            OtgAdbCard()
            QuickToolsCard(onClickRebootOptions = { showRebootOptionsDialog = true })
        }
    }


    if (showRebootOptionsDialog) {
        RebootOptionsDialog(onDismiss = { showRebootOptionsDialog = false })
    }

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
            })
    }
}

@Composable
fun SettingsButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Image(
        painter = painterResource(id = R.drawable.ic_settings),
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
fun AppNameText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier
            .alpha(0.9f)
    )
}

@Composable
fun QuickToolsCard(
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

@Composable
fun WirelessDebuggingCard(
    modifier: Modifier = Modifier,
    onClickPair: () -> Unit = {},
    onClickStart: () -> Unit = {}
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current

    NavigationCard(
        title = stringResource(R.string.adb_via_wireless_debugging),
        description = stringResource(R.string.adb_via_wireless_debugging_summary),
        icon = painterResource(R.drawable.ic_wireless),
        showNavigationArrowIcon = false,
        modifier = modifier
    ) {
        OutlinedIconButtonWithText(
            modifier = Modifier.padding(top = 35.dp),
            text = stringResource(R.string.instructions),
            painter = painterResource(R.drawable.ic_open_in_new),
            onClick = {
                weakHaptic()
                UrlUtils.openUrl(
                    url = URL_WIRELESS_DEBUGGING_INSTRUCTIONS,
                    context = context
                )
            })


        IconWithTextButton(
            icon = painterResource(R.drawable.ic_pair),
            text = stringResource(R.string.pair),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            contentDescription = null,
            modifier = Modifier.padding(vertical = 5.dp),
            onClick = {
                weakHaptic()
                onClickPair()
            })

        IconWithTextButton(
            icon = painterResource(R.drawable.ic_play),
            text = stringResource(R.string.start),
            contentDescription = null,
            onClick = {
                weakHaptic()
                onClickStart()
            })
    }
}

@Composable
fun OtgAdbCard(modifier: Modifier = Modifier) {
    NavigationCard(
        title = stringResource(R.string.adb_through_otg),
        description = stringResource(R.string.adb_through_otg_summary),
        icon = painterResource(R.drawable.ic_otg),
        modifier = modifier,
        onClick = { },
        content = {
            OtgInstructionButton(Modifier.padding(top = 35.dp))
        })
}


@Composable
fun WirelessDebuggingInstructionButton(modifier: Modifier = Modifier) {

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