@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.askUserToEnableWifi
import `in`.hridayan.ashell.core.utils.createAppNotificationSettingsIntent
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.core.utils.isNotificationPermissionGranted
import `in`.hridayan.ashell.core.utils.openDeveloperOptions
import `in`.hridayan.ashell.core.utils.registerNetworkCallback
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.core.utils.unregisterNetworkCallback
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.GrantNotificationAccessDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.ReconnectFailedDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.item.SavedDeviceItem
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.service.SelfPairingService

@Composable
fun PairingOwnDeviceScreen(
    modifier: Modifier = Modifier,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val dialogManager = LocalDialogManager.current
    val navController = LocalNavController.current

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var isWifiConnected by remember { mutableStateOf(context.isConnectedToWifi()) }
    var hasNotificationAccess by remember { mutableStateOf(isNotificationPermissionGranted(context)) }
    val lazyListState = rememberLazyListState()
    val savedDevices by viewModel.savedDevices.collectAsState()
    val wifiAdbState by viewModel.state.collectAsState()
    val ownDevice = savedDevices.filter { it.isOwnDevice }.getOrNull(0)
    val currentDevice by viewModel.currentDevice.collectAsState()

    DisposableEffect(Unit) {
        val callback = registerNetworkCallback(context) { isConnected ->
            isWifiConnected = isConnected
        }

        onDispose {
            unregisterNetworkCallback(context, callback)
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    hasNotificationAccess = isNotificationPermissionGranted(context)
                    isWifiConnected = context.isConnectedToWifi()
                    dialogManager.dismiss()
                }
            }
        )
    }

    LaunchedEffect(wifiAdbState) {
        if (wifiAdbState is WifiAdbState.WirelessDebuggingOff) {
            dialogManager.show(DialogKey.Pair.ReconnectFailed)
        }
    }

    LaunchedEffect(isWifiConnected, ownDevice) {
        if (!isWifiConnected && ownDevice != null) {
            viewModel.setDeviceDisconnected(ownDevice.id)
        }
    }

    val notificationSettingsIntent = createAppNotificationSettingsIntent(context)

    val onClickNotificationButton: () -> Unit = withHaptic {
        context.startActivity(notificationSettingsIntent)
    }

    val onClickWifiEnableButton: () -> Unit = withHaptic {
        context.askUserToEnableWifi()
    }

    val openDeveloperOptionsWithConditions: () -> Unit = withHaptic {
        if (!hasNotificationAccess) {
            dialogManager.show(DialogKey.Pair.GrantNotificationAccess)
            return@withHaptic
        }

        if (!isWifiConnected) {
            showToast(
                context,
                context.getString(R.string.connect_to_wifi_network)
            )
            return@withHaptic
        }

        openDeveloperOptions(context)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            LargeTopAppBar(
                title = {
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    val expandedFontSize = 33.sp
                    val collapsedFontSize = 20.sp

                    val fontSize = lerp(expandedFontSize, collapsedFontSize, collapsedFraction)
                    Text(
                        modifier = modifier.basicMarquee(),
                        text = stringResource(R.string.pairing),
                        maxLines = 1,
                        fontSize = fontSize,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.05.em
                    )
                },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior,
            )
        }) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(start = 15.dp, end = 15.dp, top = 15.dp),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            if (!isWifiConnected) item {
                WifiEnableCard(
                    onClickButton = onClickWifiEnableButton,
                    modifier = Modifier.animateItem()
                )
            }

            item {
                ownDevice?.let {
                    val isCurrentDevice = currentDevice?.id == it.id
                    val isReconnecting = wifiAdbState is WifiAdbState.Reconnecting &&
                            (wifiAdbState as WifiAdbState.Reconnecting).device == it.id
                    val isConnected =
                        isCurrentDevice && wifiAdbState is WifiAdbState.ConnectSuccess && isWifiConnected

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SavedDeviceItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            device = it,
                            isConnected = isConnected,
                            isReconnecting = isReconnecting,
                            onReconnect = {
                                if (!isWifiConnected) {
                                    showToast(
                                        context,
                                        context.getString(R.string.connect_to_wifi_network)
                                    )
                                    return@SavedDeviceItem
                                }

                                viewModel.reconnectToDevice(it)
                            },
                            onForget = { device -> viewModel.forgetDevice(device) },
                            onDisconnect = { viewModel.disconnect() }
                        )

                        if (isReconnecting) {
                            OutlinedButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                shapes = ButtonDefaults.shapes(),
                                onClick = withHaptic(HapticFeedbackType.Reject) {
                                    viewModel.cancelReconnect()
                                }) {
                                AutoResizeableText(text = stringResource(R.string.cancel))
                            }
                        }

                        if (isConnected) {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                shapes = ButtonDefaults.shapes(),
                                onClick = withHaptic { navController.navigate(NavRoutes.WifiAdbScreen) }) {
                                AutoResizeableText(text = stringResource(R.string.go_to_terminal))
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                        )
                    }
                }
            }

            item {
                AutoResizeableText(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    text = stringResource(R.string.hint),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!hasNotificationAccess) item {
                NotificationAccessRequestCard(
                    onClickButton = onClickNotificationButton,
                    modifier = Modifier.animateItem()
                )
            } else
                item {
                    NotificationPairingHintCard(modifier = Modifier.animateItem())
                }

            item { NotificationStyleIconWithTextCard(modifier = Modifier.animateItem()) }

            item {
                Instructions(
                    onClickDevOptionsButton = {
                        openDeveloperOptionsWithConditions()
                        SelfPairingService.start(context)
                    },
                    modifier = Modifier.animateItem()
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )
            }
        }
    }

    DialogKey.Pair.GrantNotificationAccess.createDialog {
        GrantNotificationAccessDialog(
            onDismiss = { it.dismiss() },
            onConfirm = { onClickNotificationButton() })
    }

    DialogKey.Pair.ReconnectFailed.createDialog {
        ReconnectFailedDialog(
            onDismiss = { it.dismiss() },
            onConfirm = { openDeveloperOptionsWithConditions() })
    }
}

@Composable
fun NotificationAccessRequestCard(
    modifier: Modifier = Modifier,
    onClickButton: () -> Unit = {}
) {
    IconWithTextCard(
        modifier = modifier,
        icon = painterResource(R.drawable.ic_notification_error),
        text = stringResource(R.string.notification_access_not_granted),
        content = {
            Button(
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentColor = MaterialTheme.colorScheme.errorContainer
                ),
                onClick = { onClickButton() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_open_in_new),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(10.dp))
                AutoResizeableText(
                    text = stringResource(R.string.notification_settings),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    )
}

@Composable
fun NotificationPairingHintCard(modifier: Modifier = Modifier) {
    var cardHeight by remember { mutableStateOf(0.dp) }
    val screenDensity = LocalDensity.current

    IconWithTextCard(
        modifier = modifier.onGloballyPositioned { coordinates ->
            cardHeight = with(screenDensity) { coordinates.size.height.toDp() }
        },
        shape = RoundedCornerShape(cardHeight / 2),
        icon = painterResource(R.drawable.ic_notification),
        text = stringResource(R.string.pairing_notification_hint),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        content = {})
}

@Composable
fun NotificationStyleIconWithTextCard(modifier: Modifier = Modifier) {
    IconWithTextCard(
        modifier = modifier,
        icon = painterResource(R.drawable.ic_warning),
        text = stringResource(R.string.notification_style_error)
    )
}

@Composable
fun WifiEnableCard(modifier: Modifier = Modifier, onClickButton: () -> Unit) {
    IconWithTextCard(
        modifier = modifier,
        icon = painterResource(R.drawable.ic_no_wifi),
        text = stringResource(R.string.wifi_connection_required),
        content = {
            Button(
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentColor = MaterialTheme.colorScheme.errorContainer
                ),
                onClick = { onClickButton() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wifi_settings),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(10.dp))
                AutoResizeableText(
                    text = stringResource(R.string.enable_wifi),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    )
}

@Composable
fun Instructions(modifier: Modifier = Modifier, onClickDevOptionsButton: () -> Unit) {
    Column(
        modifier = modifier,
    ) {
        AutoResizeableText(
            modifier = Modifier.padding(vertical = 15.dp, horizontal = 5.dp),
            text = stringResource(R.string.instructions),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        RoundedCornerCard(
            roundedCornerShape = getRoundedShape(index = 0, size = 3),
            paddingValues = PaddingValues(horizontal = 0.dp, vertical = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_counter_one),
                    contentDescription = null
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.wireless_debugging_guide_1),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.wireless_debugging_important_notice),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    IconWithTextButton(
                        modifier = Modifier.padding(top = 5.dp),
                        icon = painterResource(R.drawable.ic_open_in_new),
                        text = stringResource(R.string.developer_options),
                        onClick = { onClickDevOptionsButton() }
                    )
                }
            }
        }

        RoundedCornerCard(
            roundedCornerShape = getRoundedShape(index = 1, size = 3),
            paddingValues = PaddingValues(horizontal = 0.dp, vertical = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_counter_two),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.wireless_debugging_guide_2),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        RoundedCornerCard(
            roundedCornerShape = getRoundedShape(index = 2, size = 3),
            paddingValues = PaddingValues(horizontal = 0.dp, vertical = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_counter_three),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.wireless_debugging_guide_3),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}