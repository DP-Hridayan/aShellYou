@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPill
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPillDefaults
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPillItem
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootState
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootDeviceWaitingDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootFlashProgressDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootRebootConfirmDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.CommandConsoleSection
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.DeviceInfoSection
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.PartitionOperationsSection
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.QuickActionsSection
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.VariableExplorerSection
import `in`.hridayan.ashell.shell.fastboot.presentation.viewmodel.FastbootViewModel

@Composable
fun FastbootScreen(
    viewModel: FastbootViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val fastbootState by viewModel.state.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val variables by viewModel.variables.collectAsState()
    val commandHistory by viewModel.commandHistory.collectAsState()
    val isLoadingDeviceInfo by viewModel.isLoadingDeviceInfo.collectAsState()
    val isLoadingVariables by viewModel.isLoadingVariables.collectAsState()
    val flashOperation by viewModel.flashOperation.collectAsState()

    val isConnected = fastbootState is FastbootState.Connected
    var showDeviceWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var showRebootConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var pendingRebootMode by rememberSaveable { mutableStateOf<RebootMode?>(null) }

    /**
     * Similar to OTG screen's dirty hack for properly syncing states after reconnection.
     * Tracks whether a disconnection happened so we can auto-show the waiting dialog
     * and re-scan when reconnecting (e.g. after device reboots between modes).
     */
    var disconnected by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(fastbootState) {
        when (fastbootState) {
            is FastbootState.Connected -> {
                showDeviceWaitingDialog = false
                disconnected = false
                viewModel.loadDeviceInfo()
                viewModel.loadAllVariables()
            }

            is FastbootState.Idle,
            is FastbootState.Disconnected -> {
                showDeviceWaitingDialog = true
                disconnected = true
                viewModel.startScan()
            }

            // DeviceFound, Searching, Connecting, Error — show dialog so user can see status
            else -> {
                showDeviceWaitingDialog = true
            }
        }
    }

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    val tabItems = listOf(
        FloatingNavPillItem(text = stringResource(R.string.dashboard)),
        FloatingNavPillItem(text = stringResource(R.string.commands)),
        FloatingNavPillItem(text = stringResource(R.string.logs))
    )

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(FastbootTabs.DASHBOARD) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    val expandedFontSize = 33.sp
                    val collapsedFontSize = 20.sp

                    val fontSize = lerp(expandedFontSize, collapsedFontSize, collapsedFraction)
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = stringResource(R.string.fastboot),
                        maxLines = 1,
                        fontSize = fontSize,
                        letterSpacing = 0.05.em
                    )
                },
                navigationIcon = { BackButton() },
                actions = {
                    AssistChip(
                        onClick = {
                            if (!isConnected) {
                                showDeviceWaitingDialog = true
                                viewModel.startScan()
                            }
                        },
                        label = {
                            Text(
                                text = if (isConnected) {
                                    (fastbootState as FastbootState.Connected).deviceName
                                } else {
                                    stringResource(R.string.disconnected)
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Usb else Icons.Default.UsbOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = if (isConnected) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            AssistChipDefaults.assistChipColors()
                        },
                        modifier = Modifier.padding(end = Dimens.paddingSmall)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            FloatingNavPill(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(vertical = 20.dp),
                items = tabItems,
                selectedIndex = selectedTabIndex,
                onSelectionChange = {
                    weakHaptic()
                    selectedTabIndex = it
                },
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = FloatingNavPillDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    floatingContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                innerPillPadding = PaddingValues(0.dp)
            )

            AutoResizeableText(
                modifier = Modifier.padding(bottom = 10.dp, start = 5.dp, end = 5.dp),
                text = stringResource(R.string.device_info),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            ConnectedDeviceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                isConnected = isConnected,
                deviceName = deviceInfo?.product
            )

            ActiveSlotsCard(
                modifier = Modifier.padding(bottom = 10.dp),
                activeSlotIsA = deviceInfo?.currentSlot?.contains("A", ignoreCase = true) == true,
                activeSlotIsB = deviceInfo?.currentSlot?.contains("B", ignoreCase = true) == true
            )

            DeviceInfoSection(
                deviceInfo = deviceInfo,
                isLoading = isLoadingDeviceInfo,
                onRefresh = { viewModel.loadDeviceInfo() },
                isConnected = isConnected
            )

            QuickActionsSection(
                isConnected = isConnected,
                onReboot = { mode ->
                    pendingRebootMode = mode
                    showRebootConfirmDialog = true
                }
            )

            PartitionOperationsSection(
                isConnected = isConnected,
                flashOperation = flashOperation,
                onFlash = { partition, uri -> viewModel.flashPartition(partition, uri) },
                onErase = { partition -> viewModel.erasePartition(partition) },
                onBootImage = { uri -> viewModel.bootImage(uri) },
                onResetOperation = { viewModel.resetFlashOperation() }
            )

            VariableExplorerSection(
                variables = variables,
                isLoading = isLoadingVariables,
                onRefresh = { viewModel.loadAllVariables() },
                isConnected = isConnected
            )

            CommandConsoleSection(
                commandHistory = commandHistory,
                onSendCommand = { viewModel.sendCommand(it) },
                onClearHistory = { viewModel.clearHistory() },
                isConnected = isConnected
            )
        }
    }

    if (showDeviceWaitingDialog) {
        FastbootDeviceWaitingDialog(
            onDismiss = { showDeviceWaitingDialog = false },
            onConfirm = {
                showDeviceWaitingDialog = false
                if (disconnected) {
                    viewModel.startScan()
                }
            }
        )
    }

    if (showRebootConfirmDialog && pendingRebootMode != null) {
        FastbootRebootConfirmDialog(
            rebootMode = pendingRebootMode!!,
            onDismiss = {
                showRebootConfirmDialog = false
                pendingRebootMode = null
            },
            onConfirm = {
                showRebootConfirmDialog = false
                viewModel.reboot(pendingRebootMode!!)
                pendingRebootMode = null
            }
        )
    }

    // Flash progress dialog — shown during any flash/erase/boot operation
    if (flashOperation.status != FlashStatus.IDLE) {
        FastbootFlashProgressDialog(
            operation = flashOperation,
            onCancel = { viewModel.cancelFlashOperation() },
            onDismiss = { viewModel.resetFlashOperation() }
        )
    }
}

@Composable
private fun ConnectedDeviceCard(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    deviceName: String?
) {
    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.run {
                if (isConnected) primaryContainer else errorContainer
            },
            contentColor = MaterialTheme.colorScheme.run {
                if (isConnected) onPrimaryContainer else onErrorContainer
            },
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.run {
                        if (isConnected) primary else error
                    }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.padding(15.dp),
                    painter = if (isConnected) painterResource(R.drawable.ic_check_circle)
                    else painterResource(R.drawable.ic_cancel),
                    tint = MaterialTheme.colorScheme.run {
                        if (isConnected) onPrimary else onError
                    },
                    contentDescription = null
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = stringResource(R.string.connected_device),
                    style = MaterialTheme.typography.titleMediumEmphasized
                )

                Text(
                    text = if (isConnected && deviceName != null) deviceName else stringResource(R.string.no_device_connected),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ActiveSlotsCard(
    modifier: Modifier = Modifier,
    activeSlotIsA: Boolean = false,
    activeSlotIsB: Boolean = false
) {
    val activeSlotContainerColor = MaterialTheme.colorScheme.primary
    val activeSlotContentColor = MaterialTheme.colorScheme.onPrimary
    val inActiveSlotContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val inActiveSlotContentColor = MaterialTheme.colorScheme.onSurfaceVariant

    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoResizeableText(
                modifier = Modifier.alpha(0.9f),
                text = stringResource(R.string.active_slot),
                style = MaterialTheme.typography.titleMediumEmphasized
            )

            if (activeSlotIsA || activeSlotIsB) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (activeSlotIsA) activeSlotContainerColor else inActiveSlotContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                            text = "A",
                            style = MaterialTheme.typography.displaySmallEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSlotIsA) activeSlotContentColor else inActiveSlotContentColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (activeSlotIsB) activeSlotContainerColor else inActiveSlotContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                            text = "B",
                            style = MaterialTheme.typography.displaySmallEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSlotIsB) activeSlotContentColor else inActiveSlotContentColor
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.slot_data_unknown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

object FastbootTabs {
    const val DASHBOARD = 0
    const val COMMANDS = 1
    const val LOGS = 2
}