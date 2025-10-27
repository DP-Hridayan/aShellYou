@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.presentation.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.core.utils.askUserToEnableWifi
import `in`.hridayan.ashell.core.utils.createAppNotificationSettingsIntent
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.core.utils.isNotificationPermissionGranted
import `in`.hridayan.ashell.core.utils.openDeveloperOptions
import `in`.hridayan.ashell.core.utils.registerNetworkCallback
import `in`.hridayan.ashell.core.utils.unregisterNetworkCallback
import `in`.hridayan.ashell.settings.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.settings.presentation.components.shape.getRoundedShape
import `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.presentation.component.dialog.GrantNotificationAccessDialog

@Composable
fun PairingOwnDeviceScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val weakHaptic = LocalWeakHaptic.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var isWifiConnected by remember { mutableStateOf(context.isConnectedToWifi()) }
    var hasNotificationAccess by remember { mutableStateOf(isNotificationPermissionGranted(context)) }
    var showNotificationEnableDialog by rememberSaveable { mutableStateOf(false) }

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
                    showNotificationEnableDialog = false
                }
            }
        )
    }

    val notificationSettingsIntent = createAppNotificationSettingsIntent(context)

    val onClickNotificationButton: () -> Unit = {
        weakHaptic()
        context.startActivity(notificationSettingsIntent)
    }

    val onClickWifiEnableButton: () -> Unit = {
        weakHaptic()
        context.askUserToEnableWifi()
    }

    val onClickDeveloperOptionsButton: () -> Unit = {
        weakHaptic()
        if (hasNotificationAccess) openDeveloperOptions(context)
        else showNotificationEnableDialog = true
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
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            if (!hasNotificationAccess) item {
                NotificationAccessRequestCard(
                    onClickButton = onClickNotificationButton
                )
            } else
                item {
                    NotificationPairingHintCard()
                }

            item { NotificationStyleIconWithTextCard() }

            if (!isWifiConnected) item { WifiEnableCard(onClickButton = onClickWifiEnableButton) }

            item {
                Instructions(
                    onClickButton = onClickDeveloperOptionsButton
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

    if (showNotificationEnableDialog) {
        GrantNotificationAccessDialog(
            onDismiss = { showNotificationEnableDialog = false },
            onConfirm = { onClickNotificationButton() })
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
fun Instructions(modifier: Modifier = Modifier, onClickButton: () -> Unit) {
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
            roundedShape = getRoundedShape(index = 0, size = 3),
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
                        onClick = { onClickButton() }
                    )
                }
            }
        }

        RoundedCornerCard(
            roundedShape = getRoundedShape(index = 1, size = 3),
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
            roundedShape = getRoundedShape(index = 2, size = 3),
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