@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFlexBoxApi::class
)

package `in`.hridayan.ashell.home.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.domain.model.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.appBranding
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.home.presentation.component.dialog.RebootOptionsDialog
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootState
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootDeviceWaitingDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.viewmodel.FastbootViewModel
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.components.dialog.OtgDeviceWaitingDialog
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.viewmodel.OtgViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.bottomsheet.SavedDevicesBottomSheet
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.PairModeChooseDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    otgViewModel: OtgViewModel = hiltViewModel(),
    wifiAdbViewModel: WifiAdbViewModel = hiltViewModel(),
    fastbootViewModel: FastbootViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val res = LocalResources.current
    val settings = LocalSettings.current
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    val otgState by otgViewModel.state.collectAsState()
    val fastbootState by fastbootViewModel.state.collectAsState()
    val savedDevices by wifiAdbViewModel.savedDevices.collectAsState()
    val localAdbWorkingMode = settings[SettingsKeys.LocalAdbWorkingMode]

    var showSavedDevicesBottomSheet by rememberSaveable { mutableStateOf(false) }

    val onClickOtgAdbCard: () -> Unit = {
        if (otgState is OtgState.Connected) {
            navController.navigate(NavRoutes.OtgAdbScreen)
        } else {
            otgViewModel.startScan()
            dialogManager.show(DialogKey.Home.OtgDeviceWaiting)
        }
    }

    val onClickFastbootCard: () -> Unit = {
        if (fastbootState is FastbootState.Connected) {
            navController.navigate(NavRoutes.FastbootScreen)
        } else {
            fastbootViewModel.startScan()
            dialogManager.show(DialogKey.Home.FastbootDeviceWaiting)
        }
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            val collapsedFraction = scrollBehavior.state.collapsedFraction
            val expandedBrandingScale = 1f
            val collapsedBrandingScale = 0.7f

            val brandingScale =
                lerp(expandedBrandingScale, collapsedBrandingScale, collapsedFraction)

            MediumTopAppBar(
                scrollBehavior = scrollBehavior,
                expandedHeight = 96.dp,
                colors = TopAppBarDefaults.topAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                title = {
                    Image(
                        modifier = Modifier.graphicsLayer {
                            scaleX = brandingScale
                            scaleY = brandingScale
                        },
                        imageVector = DynamicColorImageVectors.appBranding(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit
                    )
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier,
                            onClick = withHaptic {

                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_help2),
                                contentDescription = null,
                            )
                        }

                        IconButton(
                            modifier = Modifier,
                            onClick = withHaptic {
                                navController.navigate(NavRoutes.SettingsScreen())
                            }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = null,
                            )
                        }
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = it
        ) {
            item {
                AutoResizeableText(
                    modifier = Modifier.padding(
                        top = 15.dp,
                        bottom = 5.dp,
                        start = 5.dp,
                        end = 5.dp
                    ),
                    text = stringResource(R.string.adb),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                FlexBox(
                    modifier = Modifier.fillMaxWidth(),
                    config = {
                        direction(FlexDirection.Row)
                        wrap(FlexWrap.Wrap)
                        gap(10.dp)
                        alignItems(FlexAlignItems.Stretch)
                    }
                ) {
                    LocalAdbCard(
                        modifier = Modifier.flex { grow(1f) },
                        enabledLocalAdbMode = localAdbWorkingMode,
                        onClick = withHaptic {
                            navController.navigate(NavRoutes.LocalAdbScreen)
                        }
                    )

                    OtgAdbCard(
                        modifier = Modifier.flex { grow(1f) },
                        onClick = withHaptic { onClickOtgAdbCard() },
                        otgState = otgState
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        WirelessDebuggingCard(
                            modifier = Modifier.fillMaxWidth(),
                            onStartClick = withHaptic {
                                if (savedDevices.count() == 0) {
                                    showToast(context, res.getString(R.string.pair_a_device_first))
                                    return@withHaptic
                                }

                                showSavedDevicesBottomSheet = true
                            }
                        )
                    }
                }
            }

            item {
                AutoResizeableText(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        bottom = 5.dp,
                        start = 5.dp,
                        end = 5.dp
                    ),
                    text = stringResource(R.string.fastboot_and_recovery),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                FlexBox(
                    modifier = Modifier.fillMaxWidth(),
                    config = {
                        direction(FlexDirection.Row)
                        wrap(FlexWrap.Wrap)
                        gap(10.dp)
                        alignItems(FlexAlignItems.Stretch)
                    }
                ) {
                    FastbootCard(
                        modifier = Modifier.flex { grow(1f) },
                        onClick = onClickFastbootCard,
                        fastbootState = fastbootState
                    )

                    AdbSideloadCard(
                        modifier = Modifier.flex { grow(1f) },
                    )
                }
            }

            item {
                AutoResizeableText(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        bottom = 5.dp,
                        start = 5.dp,
                        end = 5.dp
                    ),
                    text = stringResource(R.string.utilities),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                LogcatCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {})
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
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

        DialogKey.Home.FastbootDeviceWaiting -> FastbootDeviceWaitingDialog(
            onDismiss = { dialogManager.dismiss() },
            onConfirm = {
                dialogManager.dismiss()
                navController.navigate(NavRoutes.FastbootScreen)
                fastbootViewModel.startScan()
            },
            isAdbDeviceConnected = otgState is OtgState.Connected,
            adbDeviceName = (otgState as? OtgState.Connected)?.deviceName,
            onBootIntoFastboot = {
                otgViewModel.rebootToBootloader()
                dialogManager.dismiss()
                // Re-open the dialog so it scans for the device after reboot
                fastbootViewModel.startScan()
                dialogManager.show(DialogKey.Home.FastbootDeviceWaiting)
            }
        )

        else -> dialogManager.dismiss()
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

@Composable
private fun LocalAdbCard(
    modifier: Modifier = Modifier,
    enabledLocalAdbMode: Int = LocalAdbWorkingMode.BASIC,
    onClick: () -> Unit
) {
    val workingModeText = when (enabledLocalAdbMode) {
        LocalAdbWorkingMode.BASIC -> stringResource(R.string.basic_shell)
        LocalAdbWorkingMode.SHIZUKU -> stringResource(R.string.shizuku)
        LocalAdbWorkingMode.ROOT -> stringResource(R.string.root)
        else -> stringResource(R.string.none)
    }

    val detailsText = stringResource(R.string.active) + " ($workingModeText)"

    NavItemCompactCard(
        modifier = modifier,
        title = stringResource(R.string.local_adb),
        description = detailsText,
        leadingIcon = painterResource(R.drawable.ic_adb2),
        badgeText = stringResource(R.string.this_device),
        onClick = onClick,
    )
}

@Composable
private fun OtgAdbCard(
    modifier: Modifier = Modifier,
    otgState: OtgState,
    onClick: () -> Unit = {}
) {
    NavItemCompactCard(
        modifier = modifier,
        title = stringResource(R.string.adb_via_otg),
        leadingIcon = painterResource(R.drawable.ic_otg),
        badgeText = stringResource(R.string.other_device),
        iconContainerColor = MaterialTheme.colorScheme.tertiary,
        iconContentColor = MaterialTheme.colorScheme.onTertiary,
        onClick = onClick,
    )
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
private fun WirelessDebuggingCard(
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit
) {
    val dialogManager = LocalDialogManager.current

    NavItemCard(
        modifier = modifier,
        title = stringResource(R.string.adb_via_wireless_debugging),
        description = stringResource(R.string.adb_via_wireless_debugging_summary),
        leadingIcon = painterResource(R.drawable.ic_wireless),
        clickable = false,
        badges = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Badge(badgeText = stringResource(R.string.this_device))

                Badge(badgeText = stringResource(R.string.other_device))
            }
        },
        buttons = {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    direction(FlexDirection.Row)
                    wrap(FlexWrap.Wrap)
                    gap(10.dp)
                    alignItems(FlexAlignItems.Stretch)
                }
            ) {
                IconWithTextButton(
                    modifier = Modifier.flex { grow(1f) },
                    icon = painterResource(R.drawable.ic_pair),
                    text = stringResource(R.string.pair),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    contentDescription = null,
                    onClick = withHaptic {
                        dialogManager.show(DialogKey.Home.ChooseWifiAdbPairMode)
                    })

                IconWithTextButton(
                    modifier = Modifier.flex { grow(1f) },
                    icon = painterResource(R.drawable.ic_play),
                    text = stringResource(R.string.start),
                    contentDescription = null,
                    onClick = withHaptic { onStartClick() })

                /* OutlinedIconButtonWithText(
                     modifier = Modifier.flex { grow(1f) },
                     text = stringResource(R.string.instructions),
                     painter = painterResource(R.drawable.ic_open_in_new),
                     onClick = withHaptic {
                         UrlUtils.openUrl(
                             url = URL_WIRELESS_DEBUGGING_INSTRUCTIONS,
                             context = context
                         )
                     })*/
            }
        }
    )
}

@Composable
private fun FastbootCard(
    modifier: Modifier = Modifier,
    fastbootState: FastbootState,
    onClick: () -> Unit = {}
) {
    NavItemCompactCard(
        modifier = modifier,
        title = stringResource(R.string.fastboot),
        leadingIcon = painterResource(R.drawable.ic_flash_on),
        badgeText = stringResource(R.string.other_device),
        onClick = withHaptic { onClick() }
    )
}

@Composable
fun AdbSideloadCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    NavItemCompactCard(
        modifier = modifier,
        title = stringResource(R.string.adb_sideload),
        leadingIcon = painterResource(R.drawable.ic_mobile_arrow_down),
        badgeText = stringResource(R.string.other_device),
        iconContainerColor = MaterialTheme.colorScheme.tertiary,
        iconContentColor = MaterialTheme.colorScheme.onTertiary,
        onClick = withHaptic { onClick() }
    )
}

@Composable
fun LogcatCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    NavItemCard(
        modifier = modifier,
        title = stringResource(R.string.logcat),
        description = stringResource(R.string.des_logcat),
        leadingIcon = painterResource(R.drawable.ic_bug),
        onClick = withHaptic { onClick() }
    )
}

@Composable
private fun NavItemCompactCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    title: String,
    description: String = "Details",
    leadingIcon: Painter,
    trailingIcon: @Composable () -> Unit = {},
    cardColors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    iconContainerColor: Color = MaterialTheme.colorScheme.primary,
    iconContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    badgeContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    badgeContentColor: Color = MaterialTheme.colorScheme.onSurface,
    badgeText: String = "",
) {
    CustomCard(
        modifier = modifier,
        colors = cardColors,
        onClick = onClick,
        clickable = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(iconContainerColor)
                        .padding(5.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = leadingIcon,
                        contentDescription = null,
                        tint = iconContentColor
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    color = cardColors.contentColor
                )

                trailingIcon()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(iconContainerColor)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = cardColors.contentColor
                )
            }

            Badge(
                badgeText = badgeText,
                badgeContainerColor = badgeContainerColor,
                badgeContentColor = badgeContentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NavItemCompactCardPreview() {
    NavItemCompactCard(
        title = "Local ADB",
        leadingIcon = painterResource(R.drawable.ic_adb)
    )
}

@Composable
private fun NavItemCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    clickable: Boolean = true,
    title: String,
    description: String = "Details",
    leadingIcon: Painter,
    trailingIcon: @Composable () -> Unit = {},
    cardColors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    iconContainerColor: Color = MaterialTheme.colorScheme.primary,
    iconContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    badges: @Composable () -> Unit = {},
    buttons: @Composable () -> Unit = {},
) {
    CustomCard(
        modifier = modifier,
        colors = cardColors,
        onClick = onClick,
        clickable = clickable
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(iconContainerColor)
                        .padding(5.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = leadingIcon,
                        contentDescription = null,
                        tint = iconContentColor
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    color = cardColors.contentColor
                )

                trailingIcon()
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = cardColors.contentColor
            )

            badges()
            buttons()
        }
    }
}

@Composable
private fun Badge(
    modifier: Modifier = Modifier,
    badgeText: String,
    badgeContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    badgeContentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderEnabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(badgeContainerColor)
            .then(
                if (borderEnabled) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(50)
                ) else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = badgeText,
            style = MaterialTheme.typography.labelSmall,
            color = badgeContentColor
        )
    }
}

@Suppress("SameParameterValue")
private fun lerp(startValue: Float, endValue: Float, fraction: Float): Float {
    return startValue + fraction * (endValue - startValue)
}