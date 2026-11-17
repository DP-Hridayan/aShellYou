@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.presentation.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalStrongHaptic
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.domain.model.FastbootState
import `in`.hridayan.ashell.core.common.domain.model.PaletteStyle
import `in`.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.common.settings.SettingsState
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPill
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPillDefaults
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPillItem
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.appBrandingNoPadding
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupIconButtonDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.color.createDynamicScheme
import `in`.hridayan.ashell.core.presentation.theme.color.toComposeColorScheme
import `in`.hridayan.ashell.core.presentation.viewmodel.DialogViewModel
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.common.presentation.components.drawer.LocalAdbModeCollapsibleCard
import `in`.hridayan.ashell.shell.common.presentation.components.drawer.ShellSideDrawer
import `in`.hridayan.ashell.shell.common.presentation.model.DeviceActionId
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.validation.PartitionRisk
import `in`.hridayan.ashell.shell.fastboot.domain.validation.PartitionValidationResult
import `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet.FastbootConsoleOutput
import `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet.FlashPartitionContent
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootDeviceWaitingDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootRebootOptionsContent
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.ActiveSlotsCard
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.ConnectedDeviceCard
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.FastbootQuickToolsCard
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.UnlockStatusCard
import `in`.hridayan.ashell.shell.local_adb_shell.presentation.components.dialog.TcpIpUnavailableDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.CodePairingStage
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.DiscoveredPairingService
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.card.DiscoveredDeviceCard
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.ConnectionSuccessDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.PairModeChooseDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.TcpIpEnabledDialog
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.image.QRImage
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.item.SavedDeviceItem
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.Instructions
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.NotificationPairingHintCard
import io.nayuki.qrcodegen.QrCode
import kotlin.math.max

class MockSettingsState(
    private val overrides: Map<SettingsKeys<*>, Any?> = emptyMap()
) : SettingsState {
    @Suppress("UNCHECKED_CAST")
    @Composable
    override fun <T> get(key: SettingsKeys<T>): T =
        (overrides[key] as? T) ?: key.defaultValue

    override fun <T> set(key: SettingsKeys<T>, value: T) {}
}

@Composable
fun DocPreviewTheme(
    settingsOverrides: Map<SettingsKeys<*>, Any?> = emptyMap(),
    content: @Composable () -> Unit
) {
    val colorScheme = createDynamicScheme(
        primarySeedArgb = 0xFF7D9B36.toInt(),
        paletteStyle = PaletteStyle.TONAL_SPOT,
        isDark = false
    ).toComposeColorScheme()

    CompositionLocalProvider(
        LocalSettings provides MockSettingsState(settingsOverrides),
        LocalWeakHaptic provides {},
        LocalStrongHaptic provides {},
        LocalDarkMode provides false,
        LocalDialogManager provides remember { DialogViewModel() }
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme
        ) {
            Surface(color = colorScheme.background) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun LocalAdbModesCardPreview() {
    DocPreviewTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LocalAdbModeCollapsibleCard(
                selectedMode = LocalAdbWorkingMode.SHIZUKU,
                onModeSelected = {},
                initialExpanded = true
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
fun LocalAdbDrawerScreenPreview() {
    DocPreviewTheme(
        settingsOverrides = mapOf(
            SettingsKeys.LocalAdbWorkingMode to LocalAdbWorkingMode.SHIZUKU
        )
    ) {
        ModalNavigationDrawer(
            drawerState = rememberDrawerState(DrawerValue.Open),
            gesturesEnabled = true,
            drawerContent = {
                ShellSideDrawer(
                    connectedDevice = "Pixel 8 Pro",
                    modeText = "Local ADB",
                    showLocalAdbModes = true,
                    actions = listOf(DeviceActionId.FILES, DeviceActionId.ASK_AI),
                    onAction = {},
                    onNavigateSettings = {},
                    initialExpandedLocalAdbModes = true
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Local ADB") },
                        navigationIcon = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Terminal Output Screen",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TcpIpUnavailableDialogPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            TcpIpUnavailableDialog(
                command = "adb tcpip 5555",
                onCopyCommand = {},
                onDismiss = {},
                onUseWirelessDebugging = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
fun TcpIpEnabledDialogPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            TcpIpEnabledDialog(
                port = 5555,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 460)
@Composable
fun WirelessPairModeChooseDialogPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            PairModeChooseDialog(
                onDismiss = {},
                onClickPairSelf = {},
                onClickPairAnother = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 580)
@Composable
fun WirelessOwnDeviceInstructionsPreview() {
    DocPreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NotificationPairingHintCard(modifier = Modifier.fillMaxWidth())
            Instructions(
                modifier = Modifier.fillMaxWidth(),
                onClickDevOptionsButton = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun WirelessOwnDeviceConnectedPreview() {
    DocPreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SavedDeviceItem(
                modifier = Modifier.fillMaxWidth(),
                device = WifiAdbDevice(
                    ip = "127.0.0.1",
                    port = 37841,
                    deviceName = "Pixel 8 Pro",
                    isOwnDevice = true,
                    isPaired = true
                ),
                isConnected = true,
                isReconnecting = false,
                onReconnect = {},
                onForget = {},
                onDisconnect = {}
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                shapes = ButtonDefaults.shapes(),
                onClick = {}
            ) {
                Text(text = "Go to terminal")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 480)
@Composable
fun WirelessOtherDeviceQrPreview() {
    DocPreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomCard(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "Scan the QR code from another device to pair instantly.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            val actualQrBitmap = remember {
                val content = "WIFI:T:ADB;S:ashellyou_pair;P:492018;;"
                val qr = QrCode.encodeText(content, QrCode.Ecc.MEDIUM)
                val qrSize = qr.size
                val targetSize = 400
                val scale = max(1, targetSize / qrSize)
                val finalSize = qrSize * scale
                val bitmap = createBitmap(finalSize, finalSize)
                for (y in 0 until qrSize) {
                    for (x in 0 until qrSize) {
                        val color = if (qr.getModule(
                                x,
                                y
                            )
                        ) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                        for (dy in 0 until scale) {
                            for (dx in 0 until scale) {
                                bitmap.setPixel(x * scale + dx, y * scale + dy, color)
                            }
                        }
                    }
                }
                bitmap
            }

            QRImage(
                qrBitmap = actualQrBitmap,
                isWifiConnected = true,
                isExpired = false,
                wifiAdbState = WifiAdbState.Idle
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 320)
@Composable
fun WirelessOtherDeviceCodeActivePreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DiscoveredDeviceCard(
                modifier = Modifier.fillMaxWidth(),
                service = DiscoveredPairingService(
                    serviceName = "Google Pixel 8 Pro",
                    ip = "192.168.1.105",
                    port = 37841,
                    deviceName = "Google Pixel 8 Pro"
                ),
                stage = CodePairingStage.Idle,
                initialCode = "492018"
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 390)
@Composable
fun WirelessSavedDevicesTabPreview() {
    DocPreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SavedDeviceItem(
                    modifier = Modifier.fillMaxWidth(),
                    device = WifiAdbDevice(
                        ip = "192.168.1.105",
                        port = 37841,
                        deviceName = "Pixel Pro",
                        isPaired = true,
                        isOwnDevice = true
                    ),
                    isConnected = true,
                    isReconnecting = false,
                    onReconnect = {},
                    onForget = {},
                    onDisconnect = {}
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shapes = ButtonDefaults.shapes(),
                    onClick = {}
                ) {
                    Text(text = "Go to terminal")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SavedDeviceItem(
                    modifier = Modifier.fillMaxWidth(),
                    device = WifiAdbDevice(
                        ip = "192.168.1.142",
                        port = 42159,
                        deviceName = "Samsung Galaxy S24",
                        isPaired = true,
                        isOwnDevice = false
                    ),
                    isConnected = false,
                    isReconnecting = false,
                    onReconnect = {},
                    onForget = {},
                    onDisconnect = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 340)
@Composable
fun WirelessConnectionSuccessDialogPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            ConnectionSuccessDialog(
                device = WifiAdbDevice(
                    ip = "192.168.1.105",
                    port = 37841,
                    deviceName = "Google Pixel 8 Pro",
                    isPaired = true,
                    isOwnDevice = false
                ),
                onGoToTerminal = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 420)
@Composable
fun OtgDeviceWaitingPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(min = 280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mobile_loupe),
                        contentDescription = null,
                    )
                    AutoResizeableText(
                        text = stringResource(R.string.waiting_for_device),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.connect_device_via_otg),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    LoadingIndicator(modifier = Modifier.size(72.dp))
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        shapes = ButtonDefaults.shapes(),
                        onClick = {}
                    ) {
                        AutoResizeableText(text = stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
fun OtgDeviceConnectedPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(min = 280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mobile_loupe),
                        contentDescription = null,
                    )
                    AutoResizeableText(
                        text = stringResource(R.string.device_connected),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconWithTextCard(
                        icon = painterResource(R.drawable.ic_otg),
                        text = "Pixel 8 Pro",
                        shape = CustomCardShape(50),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Button(
                        onClick = {},
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.widthIn(ButtonDefaults.IconSpacing))
                        AutoResizeableText(text = stringResource(R.string.start))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
fun OtgTerminalScreenPreview() {
    DocPreviewTheme {
        Scaffold(
            floatingActionButton = {
                Box(modifier = Modifier.padding(bottom = 10.dp, end = 10.dp)) {
                    ExtendedFloatingActionButton(
                        onClick = {},
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_paste),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        text = {
                            AutoResizeableText(text = stringResource(R.string.paste))
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // 1. Utility Button Group (Drawer, Bookmarks, History, Clear, Search)
                val iconButtonConfig = ButtonGroupIconButtonDefaults.defaultConfig(
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                val buttonGroupItems = listOf(
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(
                            type = ButtonType.IconButton,
                            iconButtonConfig = iconButtonConfig
                        ),
                        icon = Icons.Rounded.Menu,
                        onClick = {}
                    ),
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(
                            type = ButtonType.IconButton,
                            iconButtonConfig = iconButtonConfig
                        ),
                        iconResId = R.drawable.ic_bookmarks,
                        onClick = {}
                    ),
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(
                            type = ButtonType.IconButton,
                            iconButtonConfig = iconButtonConfig
                        ),
                        icon = Icons.Rounded.History,
                        onClick = {}
                    ),
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(
                            type = ButtonType.IconButton,
                            iconButtonConfig = iconButtonConfig
                        ),
                        iconResId = R.drawable.ic_clear,
                        onClick = {}
                    ),
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(
                            type = ButtonType.IconButton,
                            iconButtonConfig = iconButtonConfig
                        ),
                        icon = Icons.Rounded.Search,
                        onClick = {}
                    ),
                )

                OverflowButtonGroup(
                    items = buttonGroupItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, bottom = 25.dp, start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                )

                // 2. Branding Bar (Height 48.dp, horizontal padding 20.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
                            imageVector = DynamicColorImageVectors.appBrandingNoPadding(),
                            contentDescription = null,
                        )
                    }
                }

                // 3. Command Input Row (padding 20.dp, spacedBy 20.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        value = "pm list packages -3",
                        onValueChange = {},
                        label = { Text(stringResource(R.string.command_title)) },
                        trailingIcon = {
                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                onClick = {}
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_add_bookmark),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    FloatingActionButton(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = {},
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send"
                        )
                    }
                }

                // 4. Output Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.output),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardDoubleArrowUp,
                                    contentDescription = "Scroll to top",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardDoubleArrowDown,
                                    contentDescription = "Scroll to bottom",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_copy),
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Terminal Output Lines
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp, top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$ getprop ro.product.model",
                                style = MaterialTheme.typography.titleSmallEmphasized.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Pixel 8 Pro",
                                style = MaterialTheme.typography.bodySmallEmphasized.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$ pm list packages -3",
                                style = MaterialTheme.typography.titleSmallEmphasized.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "package:in.hridayan.ashell\npackage:com.termux\npackage:org.mozilla.firefox",
                                style = MaterialTheme.typography.bodySmallEmphasized.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
fun OtgDrawerScreenPreview() {
    DocPreviewTheme {
        ModalNavigationDrawer(
            drawerState = rememberDrawerState(DrawerValue.Open),
            gesturesEnabled = true,
            drawerContent = {
                ShellSideDrawer(
                    connectedDevice = "Pixel 8 Pro",
                    modeText = "OTG",
                    showLocalAdbModes = false,
                    actions = listOf(
                        DeviceActionId.FILES,
                        DeviceActionId.SCREEN,
                        DeviceActionId.PORTS,
                        DeviceActionId.ASK_AI
                    ),
                    onAction = {},
                    onNavigateSettings = {},
                    initialExpandedLocalAdbModes = false
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("OTG") },
                        navigationIcon = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Terminal Output Screen",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 360)
@Composable
fun SystemHostUsbPermissionPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(min = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_otg),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Open aShellYou to handle this USB device?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Allow aShellYou to access Pixel 8 Pro via USB Host.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = true, onCheckedChange = {})
                        Text(
                            text = "Always open aShellYou when this USB device is connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {}) {
                            Text("Cancel")
                        }
                        Button(onClick = {}) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
fun SystemTargetRsaAuthorizationPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(min = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Allow USB debugging?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "The computer's RSA key fingerprint is:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "28:B1:4C:90:3A:D8:07:95:\n2E:77:81:44:6C:5F:A2:1D",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = true, onCheckedChange = {})
                        Text(
                            text = "Always allow from this computer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {}) {
                            Text("Cancel")
                        }
                        Button(onClick = {}) {
                            Text("Allow")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FastbootDashboardConnectedContent(
    initialExpanded: Boolean = true
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fastboot)) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "Pixel 8 Pro (husky)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Usb,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            FloatingNavPill(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                items = listOf(
                    FloatingNavPillItem(text = stringResource(R.string.dashboard)),
                    FloatingNavPillItem(text = stringResource(R.string.console)),
                    FloatingNavPillItem(text = stringResource(R.string.logs))
                ),
                selectedIndex = 0,
                onSelectionChange = {},
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = FloatingNavPillDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    floatingContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                innerPillPadding = PaddingValues(0.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AutoResizeableText(
                modifier = Modifier.padding(
                    bottom = 10.dp,
                    start = 5.dp,
                    end = 5.dp,
                    top = 8.dp
                ),
                text = stringResource(R.string.device_info),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            ConnectedDeviceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                isConnected = true,
                deviceName = "husky",
                serialNumber = "24011FDEE00000",
                variant = "husky-row-user",
                bootloaderVersion = "husky-14.0-10921345",
                basebandVersion = "g5300g-231215-240105-B-11295245",
                securityPatch = "2024-05-05",
                batteryLevel = null,
                initialExpanded = initialExpanded
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActiveSlotsCard(
                    modifier = Modifier.weight(1f),
                    activeSlotIsA = true,
                    activeSlotIsB = false
                )
                UnlockStatusCard(
                    modifier = Modifier.weight(1f),
                    isUnlocked = true
                )
            }

            AutoResizeableText(
                modifier = Modifier.padding(bottom = 10.dp, start = 5.dp, end = 5.dp),
                text = stringResource(R.string.quick_tools),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FastbootQuickToolsCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.flash),
                    painter = painterResource(R.drawable.ic_bolt),
                    enabled = true,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    onClick = {}
                )
                FastbootQuickToolsCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.get_variables),
                    painter = painterResource(R.drawable.ic_list_alt),
                    enabled = true,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    onClick = {}
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FastbootQuickToolsCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.wipe_data),
                    painter = painterResource(R.drawable.ic_delete_sweep),
                    enabled = true,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    onClick = {}
                )
                FastbootQuickToolsCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.reboot_options),
                    painter = painterResource(R.drawable.ic_settings_backup_restore),
                    enabled = true,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 420)
@Composable
fun FastbootDeviceWaitingPreview() {
    DocPreviewTheme {
        FastbootDeviceWaitingDialog(
            onDismiss = {},
            onConfirm = {},
            fastbootState = FastbootState.Idle,
            isAdbDeviceConnected = true,
            onBootIntoFastboot = {},
            startScan = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 360)
@Composable
fun FastbootDeviceConnectedPreview() {
    DocPreviewTheme {
        FastbootDeviceWaitingDialog(
            onDismiss = {},
            onConfirm = {},
            fastbootState = FastbootState.DeviceFound("husky"),
            isAdbDeviceConnected = false,
            onBootIntoFastboot = {},
            startScan = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 890)
@Composable
fun FastbootDashboardConnectedPreview() {
    DocPreviewTheme {
        FastbootDashboardConnectedContent(initialExpanded = true)
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
fun FastbootRebootOptionsPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            FastbootDashboardConnectedContent(initialExpanded = false)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
            )
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    FastbootRebootOptionsContent()
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
fun FastbootConsoleTabPreview() {
    DocPreviewTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.fastboot)) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    "Pixel 8 Pro (husky)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Usb,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                FloatingNavPill(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    items = listOf(
                        FloatingNavPillItem(text = stringResource(R.string.dashboard)),
                        FloatingNavPillItem(text = stringResource(R.string.console)),
                        FloatingNavPillItem(text = stringResource(R.string.logs))
                    ),
                    selectedIndex = 1,
                    onSelectionChange = {},
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = FloatingNavPillDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        floatingContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    innerPillPadding = PaddingValues(0.dp)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                FastbootConsoleOutput(
                    modifier = Modifier.weight(1f),
                    commandOutput = "${'$'} fastboot devices\n24011FDEE00000\tfastboot\n\n${'$'} fastboot getvar product\nproduct: husky\nFinished. Total time: 0.002s\n\n${'$'} fastboot getvar unlocked\nunlocked: yes\nFinished. Total time: 0.001s",
                    onClearOutput = {}
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = "fastboot getvar all",
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilledIconButton(
                        onClick = {},
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
fun FastbootFlashPartitionPreview() {
    DocPreviewTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            FastbootDashboardConnectedContent(initialExpanded = false)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
            )
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                FlashPartitionContent(
                    selectedPartition = "boot",
                    isCustomPartition = false,
                    customPartition = "",
                    partitionValidation = PartitionValidationResult.Valid,
                    partitionRisk = PartitionRisk.STANDARD,
                    selectedFileName = "boot.img",
                    isFileAdded = true,
                    isOperationVisible = false,
                    flashOperation = FlashOperation(),
                    canSlideToFlash = true,
                    isOperationRunning = false,
                    onSelectCustomMode = {},
                    onSwitchToCommon = {},
                    onCustomPartitionChange = {},
                    onPartitionSelected = {},
                    onChooseFile = {},
                    onFileRemoved = {},
                    onSlideToConfirm = {},
                    onCancel = {},
                    onDismissOperation = {}
                )
            }
        }
    }
}
