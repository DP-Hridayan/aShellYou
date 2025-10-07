@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.LabelText
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.core.utils.askUserToEnableWifi
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.core.utils.registerNetworkCallback
import `in`.hridayan.ashell.core.utils.unregisterNetworkCallback
import `in`.hridayan.ashell.navigation.slideFadeInFromLeft
import `in`.hridayan.ashell.navigation.slideFadeInFromRight
import `in`.hridayan.ashell.navigation.slideFadeOutToLeft
import `in`.hridayan.ashell.navigation.slideFadeOutToRight
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.helper.PairUsingQR
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.image.QRImage
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel
import java.security.SecureRandom

@Composable
fun PairingOtherDeviceScreen(
    modifier: Modifier = Modifier,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val weakHaptic = LocalWeakHaptic.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var showManualPairingMenu by rememberSaveable { mutableStateOf(false) }
    var isWifiConnected by remember { mutableStateOf(context.isConnectedToWifi()) }
    val wifiAdbState by viewModel.state

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
                    isWifiConnected = context.isConnectedToWifi()
                }
            }
        )
    }

    val onClickWifiEnableButton: () -> Unit = {
        weakHaptic()
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

        if (wifiAdbState is WifiAdbState.ConnectSuccess) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ConnectionSuccessfulUi()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(innerPadding)
            ) {

                if (!isWifiConnected) item {
                    WifiEnableCard(
                        onClickButton = onClickWifiEnableButton,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    )
                }
                else item {
                    IconWithTextCard(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
                        text = stringResource(R.string.turn_off_mobile_data),
                        icon = painterResource(R.drawable.ic_warning)
                    )
                }

                item {
                    AnimatedContent(
                        targetState = showManualPairingMenu,
                        transitionSpec = {
                            if (targetState) {
                                slideFadeInFromRight() togetherWith slideFadeOutToLeft()
                            } else {
                                slideFadeInFromLeft() togetherWith slideFadeOutToRight()
                            }
                        },
                        label = "Pairing Mode Transition"
                    ) { manualMode ->
                        if (manualMode) {
                            PairManually(onClickPairUsingQR = { showManualPairingMenu = false })
                        } else {
                            QRPair(onClickPairManually = { showManualPairingMenu = true })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QRPair(
    modifier: Modifier = Modifier,
    onClickPairManually: () -> Unit = {},
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val qrHelper = PairUsingQR()
    val sessionId = "ashell_you"
    val pairingCode = generatePairingCode()
    val qrBitmap = qrHelper.generateQrBitmap(sessionId, pairingCode)

    LaunchedEffect(pairingCode) {
        viewModel.startPairingFlow(
            sessionId = sessionId,
            pairingCode = pairingCode
        )
    }

    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 5.dp),
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

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            QRImage(
                qrBitmap = qrBitmap,
                modifier = Modifier.padding(25.dp)
            )
        }

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.or),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        TextButton(
            modifier = Modifier
                .padding(25.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shapes = ButtonDefaults.shapes(),
            onClick = {
                weakHaptic()
                onClickPairManually()
            },
        ) {
            Text(
                text = stringResource(R.string.pair_manually),
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        }
    }
}

@Composable
fun PairManually(
    modifier: Modifier = Modifier,
    onClickPairUsingQR: () -> Unit = {},
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val wifiAdbState by viewModel.state

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 25.dp, end = 25.dp)
    ) {
        LabelText(
            stringResource(R.string.pair),
            modifier = Modifier
                .padding(top = 25.dp, bottom = 10.dp)
                .align(Alignment.Start)
        )

        IpAddressInputField()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PairingPortInputField(modifier = Modifier.weight(1f))
            PairingCodeInputField(modifier = Modifier.weight(1f))
        }

        val pairButtonText = wifiAdbState?.let {
            when (wifiAdbState) {
                is WifiAdbState.PairingStarted -> stringResource(R.string.pairing)
                is WifiAdbState.PairingSuccess -> stringResource(R.string.paired)
                else -> stringResource(R.string.pair)
            }
        }

        IconWithTextButton(
            icon = painterResource(R.drawable.ic_pair),
            text = pairButtonText ?: stringResource(R.string.pair),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp),
            onClick = { viewModel.startPairing() })

        if (wifiAdbState is WifiAdbState.PairingSuccess || wifiAdbState is WifiAdbState.ConnectStarted) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            )

            LabelText(
                stringResource(R.string.connect), modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(Alignment.Start)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
            ) {
                ConnectPortInputField(modifier = Modifier.weight(1f))
                IconWithTextButton(
                    icon = painterResource(R.drawable.ic_wireless),
                    text = stringResource(R.string.connect),
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    onClick = {
                        weakHaptic()
                        viewModel.startConnecting()
                    }
                )
            }
        }

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 25.dp),
            text = stringResource(R.string.or),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        TextButton(
            modifier = Modifier
                .padding(25.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shapes = ButtonDefaults.shapes(),
            onClick = {
                weakHaptic()
                onClickPairUsingQR()
            },
        ) {
            Text(
                text = stringResource(R.string.pair_using_qr),
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        }
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
    )
}

@Composable
fun ConnectionSuccessfulUi(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .padding(top = 25.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(80.dp),
                painter = painterResource(R.drawable.ic_check_circle),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }

        AutoResizeableText(
            text = stringResource(R.string.success),
            style = MaterialTheme.typography.titleLargeEmphasized,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 50.dp, bottom = 15.dp)
        )

        Text(
            text = stringResource(R.string.successful_connection_message),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 25.dp)
        )
    }
}

fun generatePairingCode(): Int {
    val random = SecureRandom()
    return (100000 + random.nextInt(900000))
}