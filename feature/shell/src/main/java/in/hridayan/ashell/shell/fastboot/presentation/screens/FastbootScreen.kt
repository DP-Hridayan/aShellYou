@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFlexBoxApi::class
)

package `in`.hridayan.ashell.shell.fastboot.presentation.screens

import `in`.hridayan.ashell.core.resources.R


import `in`.hridayan.ashell.core.navigation.navigateBack

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPill
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPillDefaults
import `in`.hridayan.ashell.core.presentation.components.navigation.FloatingNavPillItem
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.core.domain.model.FastbootState
import `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet.FlashPartitionBottomSheet
import `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet.GetVariablesBottomSheet
import `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet.WipeDataBottomSheet
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootDeviceWaitingDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootRebootOptionsDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.ActiveSlotsCard
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.ConnectedDeviceCard
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.FastbootQuickToolsCard
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.UnlockStatusCard
import `in`.hridayan.ashell.shell.fastboot.presentation.viewmodel.FastbootViewModel

@Composable
fun FastbootScreen(
    viewModel: FastbootViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    val fastbootState by viewModel.state.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val variables by viewModel.variables.collectAsState()
    val commandHistory by viewModel.commandHistory.collectAsState()
    val isLoadingDeviceInfo by viewModel.isLoadingDeviceInfo.collectAsState()
    val isLoadingVariables by viewModel.isLoadingVariables.collectAsState()
    val flashOperation by viewModel.flashOperation.collectAsState()

    val isConnected = fastbootState is FastbootState.Connected
    var showDeviceWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var showFlashPartitionBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showGetVariablesBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showWipeDataBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showRebootOptionsDialog by rememberSaveable { mutableStateOf(false) }

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
                navigationIcon = { 
                    BackButton(onClick = { navController.navigateBack() }) 
                },
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
                deviceName = deviceInfo?.product,
                serialNumber = deviceInfo?.serialNo,
                variant = deviceInfo?.variant,
                bootloaderVersion = deviceInfo?.bootloaderVersion,
                basebandVersion = deviceInfo?.basebandVersion,
                securityPatch = deviceInfo?.securityPatchLevel,
                batteryLevel = deviceInfo?.batteryLevel
            )

            FlexBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                config = {
                    direction(FlexDirection.Row)
                    wrap(FlexWrap.Wrap)
                    gap(10.dp)
                    alignItems(FlexAlignItems.Stretch)
                }
            ) {
                ActiveSlotsCard(
                    modifier = Modifier.flex { grow(1f) },
                    activeSlotIsA = deviceInfo?.currentSlot?.contains(
                        other = "A",
                        ignoreCase = true
                    ) == true,
                    activeSlotIsB = deviceInfo?.currentSlot?.contains(
                        other = "B",
                        ignoreCase = true
                    ) == true
                )

                UnlockStatusCard(
                    modifier = Modifier.flex { grow(1f) },
                    isUnlocked = deviceInfo?.isUnlocked
                )
            }

            AutoResizeableText(
                modifier = Modifier.padding(bottom = 10.dp, start = 5.dp, end = 5.dp),
                text = stringResource(R.string.quick_tools),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            FlexBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                config = {
                    direction(FlexDirection.Row)
                    wrap(FlexWrap.Wrap)
                    gap(10.dp)
                    alignItems(FlexAlignItems.Stretch)
                }
            ) {
                FastbootQuickToolsCard(
                    modifier = Modifier.flex { grow(1f) },
                    title = stringResource(R.string.flash),
                    painter = painterResource(R.drawable.ic_bolt),
                    enabled = isConnected,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    onClick = { showFlashPartitionBottomSheet = true }
                )

                FastbootQuickToolsCard(
                    modifier = Modifier.flex { grow(1f) },
                    title = stringResource(R.string.get_variables),
                    painter = painterResource(R.drawable.ic_list_alt),
                    enabled = isConnected,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    onClick = { showGetVariablesBottomSheet = true }
                )

                FastbootQuickToolsCard(
                    modifier = Modifier.flex { grow(1f) },
                    title = stringResource(R.string.wipe_data),
                    painter = painterResource(R.drawable.ic_delete_sweep),
                    enabled = isConnected,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    onClick = { showWipeDataBottomSheet = true }
                )

                FastbootQuickToolsCard(
                    modifier = Modifier.flex { grow(1f) },
                    title = stringResource(R.string.reboot_options),
                    painter = painterResource(R.drawable.ic_settings_backup_restore),
                    enabled = isConnected,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    onClick = { showRebootOptionsDialog = true }
                )
            }

            /* QuickActionsSection(
                 isConnected = isConnected,
                 onReboot = { mode ->
                     pendingRebootMode = mode
                     showRebootConfirmDialog = true
                 }
             )*/
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

    if (showRebootOptionsDialog) {
        FastbootRebootOptionsDialog(
            onDismiss = { showRebootOptionsDialog = false },
            onReboot = { mode -> viewModel.reboot(mode) }
        )
    }

    if (showFlashPartitionBottomSheet) {
        FlashPartitionBottomSheet(
            onDismiss = { showFlashPartitionBottomSheet = false },
            isConnected = isConnected,
            flashOperation = flashOperation,
            onFlash = { partition, uri -> viewModel.flashPartition(partition, uri) },
            onErase = { partition -> viewModel.erasePartition(partition) },
            onBootImage = { uri -> viewModel.bootImage(uri) },
            onResetOperation = { viewModel.resetFlashOperation() },
            onCancel = { viewModel.cancelFlashOperation() }
        )
    }

    if (showGetVariablesBottomSheet) {
        GetVariablesBottomSheet(
            onDismiss = { showGetVariablesBottomSheet = false },
            variables = variables,
            isLoading = isLoadingVariables,
            onRefresh = { viewModel.loadAllVariables() }
        )
    }

    if (showWipeDataBottomSheet) {
        WipeDataBottomSheet(
            onDismiss = { showWipeDataBottomSheet = false },
            onErase = { partition -> viewModel.erasePartition(partition) }
        )
    }
}

object FastbootTabs {
    const val DASHBOARD = 0
    const val COMMANDS = 1
    const val LOGS = 2
}
