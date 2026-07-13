@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootState
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.FastbootDeviceWaitingDialog
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
    var showRebootConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var pendingRebootMode by rememberSaveable { mutableStateOf<RebootMode?>(null) }
    var hasShownInitialDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(fastbootState) {
        if (!isConnected && !hasShownInitialDialog) {
            showDeviceWaitingDialog = true
            hasShownInitialDialog = true
        }

        if (isConnected) {
            showDeviceWaitingDialog = false
            viewModel.loadDeviceInfo()
            viewModel.loadAllVariables()
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fastboot)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
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
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = Dimens.paddingLarge)
                .padding(bottom = Dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                viewModel.startScan()
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
}
