@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.adbsideload.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadPackageInfo
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.presentation.components.card.SideloadDeviceCard
import `in`.hridayan.ashell.adbsideload.presentation.components.card.SideloadFileCard
import `in`.hridayan.ashell.adbsideload.presentation.components.card.SideloadInstructionsCard
import `in`.hridayan.ashell.adbsideload.presentation.components.card.SideloadProgressCard
import `in`.hridayan.ashell.adbsideload.presentation.components.dialog.SideloadDeviceWaitingDialog
import `in`.hridayan.ashell.adbsideload.presentation.components.slidetoconfirm.SideloadSlider
import `in`.hridayan.ashell.adbsideload.presentation.components.text.sideloadErrorText
import `in`.hridayan.ashell.adbsideload.presentation.model.SideloadDeviceUiState
import `in`.hridayan.ashell.adbsideload.presentation.model.SideloadScreenActions
import `in`.hridayan.ashell.adbsideload.presentation.viewmodel.SideloadViewModel
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.effect.KeepScreenOn
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ToastUtils

private val PACKAGE_MIME_TYPES = arrayOf("application/zip", "application/octet-stream", "*/*")

@Composable
fun AdbSideloadScreen(
    viewModel: SideloadViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val state by viewModel.state.collectAsState()
    val operation by viewModel.operation.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()

    var showWaitingDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val transferInProgress = operation.status.isActive
    val leavingBlockedMessage = stringResource(R.string.sideload_in_progress_stay)
    val warnCannotLeave = { ToastUtils.makeToast(context, leavingBlockedMessage) }

    BackHandler(enabled = transferInProgress) { warnCannotLeave() }

    LaunchedEffect(Unit) {
        val currentState = viewModel.state.value
        if (currentState !is SideloadState.Connected && currentState !is SideloadState.Connecting) {
            showWaitingDialog = true
            viewModel.startScan()
        }
    }

    LaunchedEffect(state) {
        when (state) {
            is SideloadState.Connected -> showWaitingDialog = false
            is SideloadState.Disconnected -> {
                showWaitingDialog = true
                viewModel.startScan()
            }

            else -> {}
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::selectFile) }

    KeepScreenOn(enabled = operation.status.isActive)

    val actions = SideloadScreenActions(
        onPickFile = { filePickerLauncher.launch(PACKAGE_MIME_TYPES) },
        onClearFile = viewModel::clearFile,
        onSideload = viewModel::sideload,
        onCancelSideload = viewModel::cancelSideload,
        onDone = viewModel::resetOperation,
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { AutoResizeableText(text = stringResource(R.string.adb_sideload)) },
                navigationIcon = {
                    BackButton(
                        onClick = {
                            if (transferInProgress) warnCannotLeave() else navController.navigateBack()
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        ScreenContent(
            modifier = Modifier.padding(paddingValues),
            device = state.toDeviceUiState(),
            selectedFile = selectedFile,
            operation = operation,
            actions = actions,
        )
    }

    if (showWaitingDialog) {
        SideloadDeviceWaitingDialog(
            onDismiss = { showWaitingDialog = false },
            onDeviceConnected = { showWaitingDialog = false }
        )
    }
}

@Composable
private fun SideloadState.toDeviceUiState(): SideloadDeviceUiState = when (this) {
    is SideloadState.Connected -> SideloadDeviceUiState(isDetected = true, isReady = true, deviceName = deviceName)
    is SideloadState.Connecting -> SideloadDeviceUiState(isDetected = true, isConnecting = true)
    is SideloadState.DeviceFound ->
        SideloadDeviceUiState(isDetected = true, awaitingPermission = true, deviceName = deviceName)
    is SideloadState.WrongMode -> SideloadDeviceUiState(
        isDetected = true,
        wrongMode = true,
        deviceName = deviceName
    )

    is SideloadState.Error -> SideloadDeviceUiState(errorText = sideloadErrorText(error, detail))
    else -> SideloadDeviceUiState()
}

@Composable
private fun ScreenContent(
    modifier: Modifier,
    device: SideloadDeviceUiState,
    selectedFile: SideloadPackageInfo?,
    operation: SideloadOperation,
    actions: SideloadScreenActions,
) {
    val isOperationVisible = operation.status.isActive || operation.status.isFinished

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionLabel(text = stringResource(R.string.device_info))

        SideloadDeviceCard(device = device)

        if (!isOperationVisible) {
            FileSection(selectedFile = selectedFile, actions = actions)
            SideloadSlider(
                enabled = device.isReady && selectedFile != null,
                confirmed = operation.status.isActive,
                onConfirm = actions.onSideload
            )
        } else {
            SideloadProgressCard(
                operation = operation,
                onCancel = actions.onCancelSideload,
                onDone = actions.onDone
            )
        }

        SectionLabel(text = stringResource(R.string.instructions))

        SideloadInstructionsCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    AutoResizeableText(
        modifier = Modifier.padding(start = 4.dp),
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun FileSection(
    selectedFile: SideloadPackageInfo?,
    actions: SideloadScreenActions,
) {
    SectionLabel(text = stringResource(R.string.file))

    SideloadFileCard(
        fileName = selectedFile?.name,
        fileSize = selectedFile?.size ?: -1L,
        onPickFile = actions.onPickFile,
        onClearFile = actions.onClearFile,
        modifier = Modifier.fillMaxWidth()
    )
}
