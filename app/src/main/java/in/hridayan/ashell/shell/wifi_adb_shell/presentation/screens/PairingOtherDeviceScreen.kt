@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.core.utils.askUserToEnableWifi
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.core.utils.openDeveloperOptions
import `in`.hridayan.ashell.core.utils.registerNetworkCallback
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.core.utils.unregisterNetworkCallback
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.ConnectionSuccessDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.PairConnectFailedDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.ReconnectFailedDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.image.QRImage
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.item.SavedDeviceItem
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom

private enum class PairingTab(val titleRes: Int) {
    QrPair(R.string.qr_pair),
    ManualPair(R.string.manual_pair),
    SavedDevices(R.string.saved_devices)
}

@Composable
fun PairingOtherDeviceScreen(
    modifier: Modifier = Modifier,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var isWifiConnected by remember { mutableStateOf(context.isConnectedToWifi()) }
    val wifiAdbState by viewModel.state.collectAsState()
    val savedDevices by viewModel.savedDevices.collectAsState()
    val currentDevice by viewModel.currentDevice.collectAsState()

    val tabs = PairingTab.entries
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }

    // Track if reconnect was manually cancelled (to avoid showing dialog on cancel)
    var wasReconnectCancelled by remember { mutableStateOf(false) }
    var lastReconnectingDeviceId by remember { mutableStateOf<String?>(null) }

    // Refresh saved devices when pairing or connection succeeds
    LaunchedEffect(wifiAdbState) {
        val state = wifiAdbState
        when (state) {
            is WifiAdbState.ConnectSuccess -> {
                wasReconnectCancelled = false
                if (pagerState.currentPage != PairingTab.SavedDevices.ordinal) {
                    dialogManager.show(DialogKey.Pair.ConnectionSuccess)
                }
            }

            is WifiAdbState.Reconnecting -> {
                // Track which device we're reconnecting to
                lastReconnectingDeviceId = state.device
                wasReconnectCancelled = false
            }

            is WifiAdbState.ConnectFailed -> {
                // Only show dialog if reconnect wasn't manually cancelled
                if (!wasReconnectCancelled) {
                    // For other devices, don't show Dev Options button
                    val failedDevice = savedDevices.find { it.id == state.device }
                    val showDevOptions = failedDevice?.isOwnDevice == true
                    dialogManager.show(DialogKey.Pair.ReconnectFailed(showDevOptionsButton = showDevOptions))
                }
                wasReconnectCancelled = false
            }

            is WifiAdbState.WirelessDebuggingOff -> {
                // Only show dialog if reconnect wasn't manually cancelled (for own device)
                if (!wasReconnectCancelled) {
                    dialogManager.show(DialogKey.Pair.ReconnectFailed(showDevOptionsButton = true))
                }
                wasReconnectCancelled = false
            }

            is WifiAdbState.PairConnectFailed -> {
                // Show pairing connect failed dialog (different from reconnect failed)
                dialogManager.show(DialogKey.Pair.PairConnectFailed)
            }

            else -> {}
        }
    }

    DisposableEffect(Unit) {
        val callback = registerNetworkCallback(context) { isConnected ->
            isWifiConnected = isConnected
        }

        onDispose {
            unregisterNetworkCallback(context, callback)
        }
    }

    BackHandler {
        coroutineScope.launch(Dispatchers.Default) { viewModel.stopMdnsDiscovery() }
        navController.popBackStack()
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isWifiConnected = context.isConnectedToWifi()
                }
            }
        )
    }

    val onClickWifiEnableButton: () -> Unit = withHaptic {
        context.askUserToEnableWifi()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            if (!isWifiConnected) {
                WifiEnableCard(
                    onClickButton = onClickWifiEnableButton,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)
                )
            } else {
                IconWithTextCard(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
                    text = stringResource(R.string.turn_off_mobile_data),
                    icon = painterResource(R.drawable.ic_warning)
                )
            }

            // Tab Row
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(stringResource(tab.titleRes)) }
                    )
                }
            }

            // Horizontal Pager with tabs
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (tabs[page]) {
                    PairingTab.SavedDevices -> SavedDevicesTab(
                        savedDevices = savedDevices,
                        currentDevice = currentDevice,
                        wifiAdbState = wifiAdbState,
                        isWifiConnected = isWifiConnected,
                        onReconnect = { device ->
                            // If already reconnecting to a different device, mark as cancelled
                            if (lastReconnectingDeviceId != null && lastReconnectingDeviceId != device.id) {
                                wasReconnectCancelled = true
                            }
                            viewModel.reconnectToDevice(device)
                        },
                        onCancelReconnect = {
                            wasReconnectCancelled = true
                            viewModel.cancelReconnect()
                        },
                        onDisconnect = { viewModel.disconnect() },
                        onForget = { device -> viewModel.forgetDevice(device) },
                        onGoToTerminal = { navController.navigate(NavRoutes.WifiAdbScreen) }
                    )

                    PairingTab.QrPair -> QRPairTab(
                        isWifiConnected = isWifiConnected,
                        wifiAdbState = wifiAdbState
                    )

                    PairingTab.ManualPair -> ManualPairTab()
                }
            }
        }
    }

    DialogKey.Pair.ConnectionSuccess.createDialog {
        ConnectionSuccessDialog(
            device = currentDevice,
            onGoToTerminal = {
                it.dismiss()
                navController.navigate(NavRoutes.WifiAdbScreen)
            },
            onDismiss = {
                it.dismiss()
                coroutineScope.launch { pagerState.animateScrollToPage(PairingTab.SavedDevices.ordinal) }
            }
        )
    }

    DialogKey.Pair.ReconnectFailed(showDevOptionsButton = false).createDialog {
        val dialogKey = (it.activeDialog as? DialogKey.Pair.ReconnectFailed)
        ReconnectFailedDialog(
            showDevOptionsButton = dialogKey?.showDevOptionsButton ?: false,
            onConfirm = { openDeveloperOptions(context) },
            onDismiss = { it.dismiss() }
        )
    }

    DialogKey.Pair.PairConnectFailed.createDialog {
        PairConnectFailedDialog(
            onDismiss = { it.dismiss() }
        )
    }
}

@Composable
fun SavedDevicesTab(
    savedDevices: List<WifiAdbDevice>,
    currentDevice: WifiAdbDevice?,
    wifiAdbState: WifiAdbState,
    isWifiConnected: Boolean,
    onReconnect: (WifiAdbDevice) -> Unit,
    onCancelReconnect: () -> Unit,
    onDisconnect: () -> Unit,
    onForget: (WifiAdbDevice) -> Unit,
    onGoToTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (savedDevices.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_wireless),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(R.string.no_saved_devices),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.pair_device_to_start),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            items(savedDevices, key = { it.id }) { device ->
                val isCurrentDevice = currentDevice?.id == device.id
                val isReconnecting = wifiAdbState is WifiAdbState.Reconnecting &&
                        wifiAdbState.device == device.id
                val isConnected =
                    isCurrentDevice && wifiAdbState is WifiAdbState.ConnectSuccess && isWifiConnected

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SavedDeviceItem(
                        modifier = Modifier.fillMaxWidth(),
                        device = device,
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
                            onReconnect(device)
                        },
                        onForget = { onForget(device) },
                        onDisconnect = { onDisconnect() }
                    )

                    if (isReconnecting) {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            shapes = ButtonDefaults.shapes(),
                            onClick = withHaptic(HapticFeedbackType.Reject) {
                                onCancelReconnect()
                            }
                        ) {
                            AutoResizeableText(text = stringResource(R.string.cancel))
                        }
                    }

                    if (isConnected) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            shapes = ButtonDefaults.shapes(),
                            onClick = withHaptic { onGoToTerminal() }
                        ) {
                            AutoResizeableText(text = stringResource(R.string.go_to_terminal))
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun QRPairTab(
    modifier: Modifier = Modifier,
    isWifiConnected: Boolean,
    wifiAdbState: WifiAdbState,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val sessionId = remember { "ashell_you" }
    val pairingCode = remember { String.format("%06d", generatePairingCode()) }
    val qrBitmap by viewModel.qrBitmap.collectAsState()

    // Start mDNS discovery when WiFi is connected - only re-run if isWifiConnected changes
    LaunchedEffect(isWifiConnected) {
        if (isWifiConnected) viewModel.startMdnsPairing(pairingCode) else viewModel.stopMdnsDiscovery()
    }

    // Generate QR code only once when tab is first shown
    LaunchedEffect(pairingCode) {
        viewModel.generateQr(sessionId, pairingCode.toInt())
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_qr_scanner),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )

                    Text(
                        text = stringResource(R.string.qr_pair_hint),
                        style = MaterialTheme.typography.bodySmallEmphasized,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )

                    Text(
                        text = stringResource(R.string.qr_scanner_location_hint),
                        style = MaterialTheme.typography.bodySmallEmphasized,
                    )
                }
            }
        }

        item {
            qrBitmap?.let { qrBitmap ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    QRImage(
                        qrBitmap = qrBitmap,
                        modifier = Modifier.padding(25.dp),
                        isWifiConnected = isWifiConnected,
                        wifiAdbState = wifiAdbState
                    )
                }
            }
        }
    }
}

@Composable
fun ManualPairTab(
    modifier: Modifier = Modifier,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val wifiAdbState by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 25.dp),
        verticalArrangement = Arrangement.Top
    ) {
        item {
            AutoResizeableText(
                text = stringResource(R.string.pair),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 25.dp, bottom = 10.dp)
            )
        }

        item { IpAddressInputField() }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PairingPortInputField(modifier = Modifier.weight(1f))
                PairingCodeInputField(modifier = Modifier.weight(1f))
            }
        }

        item {
            val pairButtonText = wifiAdbState.let {
                when (wifiAdbState) {
                    is WifiAdbState.PairingStarted -> stringResource(R.string.pairing)
                    is WifiAdbState.PairingSuccess -> stringResource(R.string.paired)
                    else -> stringResource(R.string.pair)
                }
            }

            IconWithTextButton(
                icon = painterResource(R.drawable.ic_pair),
                text = pairButtonText,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                onClick = { viewModel.startPairingManually() }
            )
        }

        if (wifiAdbState is WifiAdbState.PairingSuccess || wifiAdbState is WifiAdbState.ConnectStarted) {
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                )
            }

            item {
                AutoResizeableText(
                    text = stringResource(R.string.connect),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 25.dp, bottom = 10.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
                ) {
                    ConnectPortInputField(modifier = Modifier.weight(1f))
                    IconWithTextButton(
                        icon = painterResource(R.drawable.ic_wireless),
                        text = stringResource(R.string.connect),
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onClick = withHaptic {
                            viewModel.startConnectingManually()
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun IpAddressInputField(
    modifier: Modifier = Modifier,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val ipAddress by viewModel.ipAddress.collectAsState()
    val error by viewModel.ipAddressError.collectAsState()
    val label =
        if (error) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.ip_address)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = ipAddress,
        onValueChange = { viewModel.onIpChange(it) },
        isError = error,
        label = {
            AutoResizeableText(
                text = label,
                modifier = Modifier.basicMarquee()
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable

fun PairingPortInputField(
    modifier: Modifier = Modifier,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val pairingPort by viewModel.pairingPort.collectAsState()
    val error by viewModel.pairingPortError.collectAsState()
    val label =
        if (error) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.port)

    OutlinedTextField(
        modifier = modifier,
        value = pairingPort,
        onValueChange = { viewModel.onPairingPortChange(it) },
        isError = error,
        label = {
            AutoResizeableText(
                text = label,
                modifier = Modifier.basicMarquee()
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun PairingCodeInputField(
    modifier: Modifier = Modifier,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val pairingCode by viewModel.pairingCode.collectAsState()
    val error by viewModel.pairingCodeError.collectAsState()
    val label =
        if (error) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.pairing_code)

    OutlinedTextField(
        modifier = modifier,
        value = pairingCode,
        onValueChange = { viewModel.onPairingCodeChange(it) },
        isError = error,
        label = {
            AutoResizeableText(
                text = label,
                modifier = Modifier.basicMarquee()
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun ConnectPortInputField(
    modifier: Modifier = Modifier,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val connectPort by viewModel.connectPort.collectAsState()
    val error by viewModel.connectPortError.collectAsState()
    val label =
        if (error) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.port)

    OutlinedTextField(
        modifier = modifier,
        value = connectPort,
        onValueChange = { viewModel.onConnectingPortChange(it) },
        isError = error,
        label = {
            AutoResizeableText(
                text = label,
                modifier = Modifier.basicMarquee()
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

fun generatePairingCode(): Int {
    val random = SecureRandom()
    return (100000 + random.nextInt(900000))
}