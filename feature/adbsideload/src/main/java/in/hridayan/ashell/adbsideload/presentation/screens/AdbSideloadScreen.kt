@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.adbsideload.presentation.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.adbsideload.presentation.components.card.SideloadDeviceCard
import `in`.hridayan.ashell.adbsideload.presentation.components.card.SideloadFileCard
import `in`.hridayan.ashell.adbsideload.presentation.components.card.SideloadInstructionsCard
import `in`.hridayan.ashell.adbsideload.presentation.components.card.SideloadProgressCard
import `in`.hridayan.ashell.adbsideload.presentation.components.dialog.SideloadDeviceWaitingDialog
import `in`.hridayan.ashell.adbsideload.presentation.components.slidetoconfirm.SideloadSlider
import `in`.hridayan.ashell.adbsideload.presentation.viewmodel.SideloadViewModel
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AdbSideloadScreen(
    viewModel: SideloadViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val operation by viewModel.operation.collectAsState()

    var showWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var selectedUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var selectedFileName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFileSize by rememberSaveable { mutableLongStateOf(-1L) }

    val stateValue = state
    val isDeviceDetected = stateValue is SideloadState.Connected ||
            stateValue is SideloadState.Connecting ||
            stateValue is SideloadState.DeviceFound
    val isAdbReady = stateValue is SideloadState.Connected
    val isConnecting = stateValue is SideloadState.Connecting
    val deviceName = (stateValue as? SideloadState.Connected)?.deviceName
        ?: (stateValue as? SideloadState.DeviceFound)?.deviceName
    val isOperationActive = operation.status == SideloadStatus.SENDING ||
            operation.status == SideloadStatus.READING_FILE
    val isOperationFinished = operation.status == SideloadStatus.COMPLETE ||
            operation.status == SideloadStatus.ERROR ||
            operation.status == SideloadStatus.CANCELLED

    LaunchedEffect(Unit) {
        val currentState = viewModel.state.value
        if (currentState !is SideloadState.Connected && currentState !is SideloadState.Connecting) {
            showWaitingDialog = true
            viewModel.startScan()
        }
    }

    LaunchedEffect(stateValue) {
        when (stateValue) {
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
    ) { uri ->
        uri?.let { pickedUri ->
            selectedUri = pickedUri
            scope.launch(Dispatchers.IO) {
                val name = resolveFileName(context, pickedUri)
                val size = resolveFileSize(context, pickedUri)
                withContext(Dispatchers.Main) {
                    selectedFileName = name
                    selectedFileSize = size
                }
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { AutoResizeableText(text = stringResource(R.string.adb_sideload)) },
                navigationIcon = { BackButton(onClick = { navController.navigateBack() }) }
            )
        }
    ) { paddingValues ->
        ScreenContent(
            modifier = Modifier.padding(paddingValues),
            isDeviceDetected = isDeviceDetected,
            isAdbReady = isAdbReady,
            isConnecting = isConnecting,
            deviceName = deviceName,
            selectedFileName = selectedFileName,
            selectedFileSize = selectedFileSize,
            isOperationActive = isOperationActive,
            isOperationFinished = isOperationFinished,
            operation = operation,
            selectedUri = selectedUri,
            onPickFile = {
                filePickerLauncher.launch(
                    arrayOf(
                        "application/zip",
                        "application/octet-stream",
                        "*/*"
                    )
                )
            },
            onClearFile = {
                selectedUri = null
                selectedFileName = null
                selectedFileSize = -1L
            },
            onSideload = { uri -> viewModel.sideload(uri) },
            onCancelSideload = { viewModel.cancelSideload() },
            onDone = { viewModel.resetOperation() },
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
private fun ScreenContent(
    modifier: Modifier,
    isDeviceDetected: Boolean,
    isAdbReady: Boolean,
    isConnecting: Boolean,
    deviceName: String?,
    selectedFileName: String?,
    selectedFileSize: Long,
    isOperationActive: Boolean,
    isOperationFinished: Boolean,
    operation: SideloadOperation,
    selectedUri: Uri?,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    onSideload: (Uri) -> Unit,
    onCancelSideload: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionLabel(text = stringResource(R.string.device_info))

        SideloadDeviceCard(
            isDetected = isDeviceDetected,
            isConnecting = isConnecting,
            deviceName = deviceName
        )

        if (!isOperationActive && !isOperationFinished) {
            FileSection(
                selectedFileName = selectedFileName,
                selectedFileSize = selectedFileSize,
                onPickFile = onPickFile,
                onClearFile = onClearFile
            )
            SideloadSlider(
                enabled = isAdbReady && selectedUri != null,
                onConfirm = { selectedUri?.let { onSideload(it) } }
            )
        }

        if (isOperationActive || isOperationFinished) {
            SideloadProgressCard(
                operation = operation,
                onCancel = onCancelSideload,
                onDone = onDone
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
    selectedFileName: String?,
    selectedFileSize: Long,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
) {
    SectionLabel(text = stringResource(R.string.file))

    SideloadFileCard(
        fileName = selectedFileName,
        fileSize = selectedFileSize,
        onPickFile = onPickFile,
        onClearFile = onClearFile,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun resolveFileName(context: Context, uri: Uri): String {
    if (uri.scheme == "content") {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (col >= 0) return cursor.getString(col)
            }
        }
    }
    return uri.path?.substringAfterLast('/') ?: "package.zip"
}

private fun resolveFileSize(context: Context, uri: Uri): Long {
    return context.contentResolver.query(
        uri, arrayOf(OpenableColumns.SIZE), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getLong(0) else -1L
    } ?: -1L
}
