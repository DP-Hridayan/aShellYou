package `in`.hridayan.ashell.shell.file_browser.presentation.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.AdbFileBrowserConnectionMode
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.file_browser.data.executor.OtgCommandExecutor
import `in`.hridayan.ashell.shell.file_browser.data.repository.FileBrowserRepositoryImpl
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictDecision
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictResolution
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileConflict
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperation
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationStatus
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationType
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteFailure
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteFailureReason
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteRequest
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteSummary
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.file_browser.domain.repository.FileBrowserRepository
import `in`.hridayan.ashell.shell.file_browser.domain.usecase.PasteFilesUseCase
import `in`.hridayan.ashell.shell.file_browser.domain.util.RemotePaths
import `in`.hridayan.ashell.shell.file_browser.presentation.model.FileBrowserEvent
import `in`.hridayan.ashell.shell.file_browser.presentation.model.FileBrowserState
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FileBrowserViewModel @Inject constructor(
    private val repository: FileBrowserRepository,
    private val wifiAdbRepository: WifiAdbRepository,
    private val otgRepository: OtgRepository,
    private val otgExecutor: OtgCommandExecutor,
    private val pasteFiles: PasteFilesUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "FileBrowserViewModel"
    }

    private val _state = MutableStateFlow(FileBrowserState())
    val state: StateFlow<FileBrowserState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FileBrowserEvent>()
    val events: SharedFlow<FileBrowserEvent> = _events.asSharedFlow()

    private val pathHistory = mutableListOf<String>()
    private var navigationJob: Job? = null
    private val operationJobs = mutableMapOf<String, Job>()
    private var lastConnectedDevice: WifiAdbDevice? = null
    private var pasteJob: Job? = null
    private var pendingConflictDecision: CompletableDeferred<ConflictDecision?>? = null
    private var pasteCancelRequested = false

    // Track connection mode for conditional logic
    private var connectionMode = AdbFileBrowserConnectionMode.WIFI_ADB

    init {
        lastConnectedDevice = wifiAdbRepository.getCurrentDevice()
        Log.d(
            "FileBrowserVM",
            "Init: captured device = ${lastConnectedDevice?.deviceName} at ${lastConnectedDevice?.ip}:${lastConnectedDevice?.port}"
        )

        // Don't auto-load files in init - wait for setConnectionMode to be called
    }

    /**
     * Set the connection mode based on navigation parameter.
     * Called from FileBrowserScreen on init.
     */
    fun setConnectionMode(mode: AdbFileBrowserConnectionMode) {
        connectionMode = mode

        // Cast to impl to access setExecutor (safe since we know the DI provides impl)
        val repoImpl = repository as? FileBrowserRepositoryImpl

        when (connectionMode) {
            AdbFileBrowserConnectionMode.OTG_ADB -> {
                Log.d(TAG, "Setting connection mode: OTG")
                repoImpl?.setExecutor(otgExecutor)
            }

            AdbFileBrowserConnectionMode.WIFI_ADB -> {
                Log.d(TAG, "Setting connection mode: WIFI")
                repoImpl?.resetToWifiExecutor()
                lastConnectedDevice = wifiAdbRepository.getCurrentDevice()
            }
        }

        loadFiles("/storage/emulated/0")
    }

    /**
     * Set the connected device explicitly (called from UI with device from navigation if needed)
     */
    fun setConnectedDevice(device: WifiAdbDevice?) {
        lastConnectedDevice = device
        Log.d(
            "FileBrowserVM",
            "setConnectedDevice: ${device?.deviceName} at ${device?.ip}:${device?.port}"
        )
    }

    fun loadFiles(path: String, addToHistory: Boolean = true) {
        // Cancel any pending navigation
        navigationJob?.cancel()

        navigationJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // For OTG mode, retry up to 2 more times before showing error
            val isOtg = connectionMode == AdbFileBrowserConnectionMode.OTG_ADB
            val maxAttempts = if (isOtg) 3 else 1
            var lastError: Throwable? = null

            repeat(maxAttempts) { attempt ->
                repository.listFiles(path).fold(
                    onSuccess = { files ->
                        // Only add to history if navigating forward (not refresh)
                        if (addToHistory && path != _state.value.currentPath) {
                            pathHistory.add(_state.value.currentPath)
                        }
                        _state.value = _state.value.copy(
                            currentPath = path,
                            lastSuccessfulPath = path,
                            files = files,
                            isLoading = false,
                            error = null,
                            isVirtualEmptyFolder = false
                        )
                        return@launch // Success - exit
                    },
                    onFailure = { error ->
                        lastError = error
                        if (isOtg && attempt < maxAttempts - 1) {
                            Log.d(TAG, "OTG loadFiles attempt ${attempt + 1} failed, retrying...")
                            delay(500)
                        }
                    }
                )
            }

            _state.value = _state.value.copy(
                isLoading = false,
                error = lastError?.message ?: "Failed to load files"
            )
            _events.emit(
                FileBrowserEvent.ShowToast(
                    R.string.fb_error,
                    listOf(lastError?.message ?: "Unknown")
                )
            )
        }
    }

    /**
     * Refresh current directory without adding to history
     */
    fun refresh() {
        loadFiles(_state.value.currentPath, addToHistory = false)
    }

    /**
     * Attempt a silent reconnect using the appropriate method based on connection mode.
     * WiFi: Uses stored device and WiFi reconnect
     * OTG: Uses searchDevices with retry
     */
    fun silentReconnectAndRefresh() {
        Log.d(TAG, "silentReconnectAndRefresh called, mode=$connectionMode")
        _state.value = _state.value.copy(isLoading = true, error = null)

        when (connectionMode) {
            AdbFileBrowserConnectionMode.WIFI_ADB -> performWifiReconnect()
            AdbFileBrowserConnectionMode.OTG_ADB -> performOtgReconnect()
        }
    }

    private fun performWifiReconnect() {
        val device = lastConnectedDevice
        if (device == null) {
            Log.w(TAG, "No connected device stored, falling back to refresh")
            refresh()
            return
        }

        Log.d(TAG, "WiFi reconnect to: ${device.deviceName} at ${device.ip}:${device.port}")

        wifiAdbRepository.reconnect(
            device,
            object : WifiAdbRepositoryImpl.ReconnectListener {
                override fun onReconnectSuccess() {
                    Log.d(TAG, "WiFi Reconnect SUCCESS")
                    lastConnectedDevice = wifiAdbRepository.getCurrentDevice() ?: device
                    viewModelScope.launch {
                        _events.emit(FileBrowserEvent.ShowToast(R.string.fb_reconnected_successfully))
                        refresh()
                    }
                }

                override fun onReconnectFailed(requiresPairing: Boolean) {
                    Log.e(TAG, "WiFi Reconnect FAILED, requiresPairing=$requiresPairing")
                    viewModelScope.launch {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = if (requiresPairing) "Connection lost. Please re-pair device." else "Reconnection failed"
                        )
                    }
                }
            }
        )
    }

    private fun performOtgReconnect() {
        viewModelScope.launch {
            Log.d(TAG, "OTG reconnect: starting scan with retry")

            // Try up to 3 times for OTG reconnection
            repeat(3) { attempt ->
                Log.d(TAG, "OTG reconnect attempt ${attempt + 1}")
                otgRepository.searchDevices()

                // Wait a bit for connection to establish
                delay(1000)

                // Check if connected
                val currentState = OtgConnection.currentState
                if (currentState is OtgState.Connected) {
                    Log.d(TAG, "OTG Reconnect SUCCESS on attempt ${attempt + 1}")
                    _events.emit(FileBrowserEvent.ShowToast(R.string.fb_reconnected_successfully))
                    refresh()
                    return@launch
                }

                if (attempt < 2) {
                    delay(500) // Wait before retry
                }
            }

            // All attempts failed
            Log.e(TAG, "OTG Reconnect FAILED after 3 attempts")
            _state.value = _state.value.copy(
                isLoading = false,
                error = "OTG connection failed. Please reconnect device."
            )
        }
    }

    fun navigateUp(): Boolean {
        val parentPath = File(_state.value.currentPath).parent
        return if (parentPath != null && _state.value.currentPath != "/") {
            loadFiles(parentPath)
            true
        } else {
            false
        }
    }

    fun onFileClick(file: RemoteFile) {
        if (file.isDirectory) {
            loadFiles(file.path)
        } else {
            _state.value = _state.value.copy(selectedFile = file)
        }
    }

    fun downloadFile(remotePath: String, fileName: String) {
        val savedPath = "${Environment.DIRECTORY_DOWNLOADS}/$fileName"
        val operationId = UUID.randomUUID().toString()
        val operation = FileOperation(
            id = operationId,
            type = OperationType.DOWNLOAD,
            fileName = fileName,
            message = "Downloading...",
            status = OperationStatus.IN_PROGRESS
        )
        addOperation(operation)

        val job = viewModelScope.launch {
            repository.pullFile(remotePath, fileName).collect { result ->
                when (result) {
                    is FileOperationResult.Progress -> {
                        updateOperation(operationId) {
                            it.copy(
                                bytesTransferred = result.current,
                                totalBytes = result.total,
                                status = OperationStatus.IN_PROGRESS
                            )
                        }
                    }

                    is FileOperationResult.Success -> {
                        updateOperation(operationId) {
                            it.copy(
                                status = OperationStatus.COMPLETED,
                                message = "Download complete",
                                bytesTransferred = it.totalBytes // Ensure 100% shown
                            )
                        }
                        _events.emit(FileBrowserEvent.FileDownloaded(savedPath))
                        _events.emit(
                            FileBrowserEvent.ShowToast(
                                R.string.fb_downloaded_to,
                                listOf(savedPath)
                            )
                        )
                        // Delay removal to let UI show completion
                        delay(2000)
                        removeOperation(operationId)
                    }

                    is FileOperationResult.Error -> {
                        updateOperation(operationId) {
                            it.copy(
                                status = OperationStatus.FAILED,
                                message = "Failed: ${result.message}"
                            )
                        }
                        _events.emit(
                            FileBrowserEvent.ShowToast(
                                R.string.fb_download_failed,
                                listOf(result.message)
                            )
                        )
                        delay(3000)
                        removeOperation(operationId)
                    }
                }
            }
        }
        operationJobs[operationId] = job
    }

    fun uploadFile(localPath: String, fileName: String) {
        val remotePath = "${_state.value.currentPath}/$fileName"
        val operationId = UUID.randomUUID().toString()
        val operation = FileOperation(
            id = operationId,
            type = OperationType.UPLOAD,
            fileName = fileName,
            message = "Uploading...",
            status = OperationStatus.IN_PROGRESS
        )
        addOperation(operation)

        val job = viewModelScope.launch {
            repository.pushFile(localPath, remotePath).collect { result ->
                when (result) {
                    is FileOperationResult.Progress -> {
                        updateOperation(operationId) {
                            it.copy(
                                bytesTransferred = result.current,
                                totalBytes = result.total,
                                status = OperationStatus.IN_PROGRESS
                            )
                        }
                    }

                    is FileOperationResult.Success -> {
                        updateOperation(operationId) {
                            it.copy(
                                status = OperationStatus.COMPLETED,
                                message = "Upload complete",
                                bytesTransferred = it.totalBytes
                            )
                        }
                        _events.emit(FileBrowserEvent.FileUploaded(remotePath))
                        _events.emit(FileBrowserEvent.ShowToast(R.string.fb_uploaded_successfully))
                        refresh()
                        delay(2000)
                        removeOperation(operationId)
                    }

                    is FileOperationResult.Error -> {
                        updateOperation(operationId) {
                            it.copy(
                                status = OperationStatus.FAILED,
                                message = "Failed: ${result.message}"
                            )
                        }
                        _events.emit(
                            FileBrowserEvent.ShowToast(
                                R.string.fb_upload_failed,
                                listOf(result.message)
                            )
                        )
                        delay(3000)
                        removeOperation(operationId)
                    }
                }
            }
        }
        operationJobs[operationId] = job
    }

    private fun addOperation(operation: FileOperation) {
        _state.value = _state.value.copy(
            operations = _state.value.operations + operation
        )
    }

    private fun updateOperation(id: String, update: (FileOperation) -> FileOperation) {
        _state.value = _state.value.copy(
            operations = _state.value.operations.map { if (it.id == id) update(it) else it }
        )
    }

    private fun removeOperation(id: String) {
        operationJobs.remove(id)
        _state.value = _state.value.copy(
            operations = _state.value.operations.filter { it.id != id }
        )
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            repository.deleteFile(path).fold(
                onSuccess = {
                    _state.value = _state.value.copy(selectedFile = null)
                    _events.emit(FileBrowserEvent.FileDeleted)
                    _events.emit(FileBrowserEvent.ShowToast(R.string.fb_file_deleted))
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(
                        FileBrowserEvent.ShowToast(
                            R.string.fb_delete_failed,
                            listOf(error.message ?: "Unknown")
                        )
                    )
                }
            )
        }
    }

    fun createDirectory(name: String) {
        val path = "${_state.value.currentPath}/$name"
        viewModelScope.launch {
            repository.createDirectory(path).fold(
                onSuccess = {
                    _events.emit(FileBrowserEvent.DirectoryCreated)
                    _events.emit(FileBrowserEvent.ShowToast(R.string.fb_folder_created))
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(
                        FileBrowserEvent.ShowToast(
                            R.string.fb_create_folder_failed,
                            listOf(error.message ?: "Unknown")
                        )
                    )
                }
            )
        }
    }

    fun renameFile(oldPath: String, newPath: String) {
        viewModelScope.launch {
            repository.rename(oldPath, newPath).fold(
                onSuccess = {
                    _events.emit(FileBrowserEvent.ShowToast(R.string.fb_renamed_successfully))
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(
                        FileBrowserEvent.ShowToast(
                            R.string.fb_rename_failed,
                            listOf(error.message ?: "Unknown")
                        )
                    )
                }
            )
        }
    }

    fun cancelOperation(operationId: String) {
        operationJobs[operationId]?.cancel()
        removeOperation(operationId)
        viewModelScope.launch {
            _events.emit(FileBrowserEvent.ShowToast(R.string.fb_operation_cancelled))
        }
    }

    fun cancelAllOperations() {
        operationJobs.values.forEach { it.cancel() }
        operationJobs.clear()
        _state.value = _state.value.copy(operations = emptyList())
        viewModelScope.launch {
            _events.emit(FileBrowserEvent.ShowToast(R.string.fb_all_operations_cancelled))
        }
    }

    fun enterSelectionMode(initialFile: RemoteFile) {
        _state.value = _state.value.copy(
            isSelectionMode = true,
            selectedFiles = setOf(initialFile.path)
        )
    }

    fun exitSelectionMode() {
        _state.value = _state.value.copy(
            isSelectionMode = false,
            selectedFiles = emptySet()
        )
    }

    fun toggleFileSelection(file: RemoteFile) {
        val current = _state.value.selectedFiles
        val newSelection = if (current.contains(file.path)) {
            current - file.path
        } else {
            current + file.path
        }
        _state.value = _state.value.copy(
            selectedFiles = newSelection,
            isSelectionMode = newSelection.isNotEmpty()
        )
    }

    fun selectAllFiles() {
        val allPaths = _state.value.files
            .filterNot { it.isParentDirectory }
            .map { it.path }
            .toSet()
        _state.value = _state.value.copy(selectedFiles = allPaths)
    }

    fun deleteSelectedFiles() {
        val paths = _state.value.selectedFiles.toList()
        exitSelectionMode()
        viewModelScope.launch {
            paths.forEach { path ->
                repository.deleteFile(path).fold(
                    onSuccess = {},
                    onFailure = { error ->
                        _events.emit(
                            FileBrowserEvent.ShowToast(
                                R.string.fb_delete_failed,
                                listOf(error.message ?: "Unknown")
                            )
                        )
                    }
                )
            }
            _events.emit(FileBrowserEvent.ShowToast(R.string.fb_deleted_items, listOf(paths.size)))
            refresh()
        }
    }

    fun getSelectedFilePaths(): List<String> = _state.value.selectedFiles.toList()

    /** @return false when the paste was refused, so the caller should keep its clipboard. */
    fun copyFileBatch(sourcePaths: List<String>, destDir: String): Boolean =
        startPaste(PasteRequest(sourcePaths, destDir, OperationType.COPY))

    /** @return false when the paste was refused, so the caller should keep its clipboard. */
    fun moveFileBatch(sourcePaths: List<String>, destDir: String): Boolean =
        startPaste(PasteRequest(sourcePaths, destDir, OperationType.MOVE))

    private fun startPaste(request: PasteRequest): Boolean {
        val refusal = when {
            pasteJob?.isActive == true -> R.string.fb_paste_in_progress
            request.sourcePaths.isEmpty() -> R.string.fb_nothing_to_paste
            else -> null
        }
        if (refusal != null) {
            viewModelScope.launch { _events.emit(FileBrowserEvent.ShowToast(refusal)) }
            return false
        }

        pasteCancelRequested = false
        pasteJob = viewModelScope.launch { runPaste(request) }
        return true
    }

    private suspend fun runPaste(request: PasteRequest) {
        _state.update { it.copy(isPasting = true, pasteProgress = null) }
        try {
            val summary = pasteFiles(
                request = request,
                resolveConflict = ::awaitConflictDecision,
                isCancelled = { pasteCancelRequested },
                onProgress = { progress -> _state.update { it.copy(pasteProgress = progress) } }
            )
            emitPasteSummary(summary)
        } finally {
            _state.update { it.copy(isPasting = false, pasteProgress = null, pendingConflict = null) }
            refresh()
        }
    }

    private suspend fun awaitConflictDecision(conflict: FileConflict): ConflictDecision? {
        val decision = CompletableDeferred<ConflictDecision?>()
        pendingConflictDecision = decision
        _state.update { it.copy(pendingConflict = conflict) }

        return try {
            decision.await()
        } finally {
            pendingConflictDecision = null
            _state.update { it.copy(pendingConflict = null) }
        }
    }

    private suspend fun emitPasteSummary(summary: PasteSummary) {
        val handledCount = summary.completedCount + summary.skippedCount + summary.failedCount

        if (summary.cancelled) {
            _events.emit(FileBrowserEvent.ShowToast(R.string.fb_operation_cancelled))
        }
        if (!summary.cancelled || handledCount > 0) {
            _events.emit(pasteCountsToast(summary))
        }
        summary.failures.firstOrNull()?.let { _events.emit(pasteFailureToast(it)) }
    }

    private fun pasteCountsToast(summary: PasteSummary): FileBrowserEvent.ShowToast = when {
        summary.failedCount == 0 && summary.skippedCount == 0 ->
            FileBrowserEvent.ShowToast(
                R.string.fb_paste_completed,
                listOf(summary.completedCount)
            )

        summary.failedCount == 0 ->
            FileBrowserEvent.ShowToast(
                R.string.fb_paste_completed_with_skipped,
                listOf(summary.completedCount, summary.skippedCount)
            )

        else ->
            FileBrowserEvent.ShowToast(
                R.string.fb_paste_completed_with_failed,
                listOf(summary.completedCount, summary.skippedCount, summary.failedCount)
            )
    }

    private fun pasteFailureToast(failure: PasteFailure): FileBrowserEvent.ShowToast {
        val name = RemotePaths.fileName(failure.sourcePath)
        return when (failure.reason) {
            PasteFailureReason.SOURCE_MISSING ->
                FileBrowserEvent.ShowToast(R.string.fb_paste_source_missing, listOf(name))

            PasteFailureReason.INTO_ITSELF ->
                FileBrowserEvent.ShowToast(R.string.fb_paste_into_itself, listOf(name))

            PasteFailureReason.NO_FREE_NAME ->
                FileBrowserEvent.ShowToast(R.string.fb_paste_no_free_name, listOf(name))

            PasteFailureReason.COMMAND_FAILED ->
                FileBrowserEvent.ShowToast(
                    R.string.fb_paste_item_failed,
                    listOf(name, failure.message ?: "Unknown")
                )
        }
    }

    fun resolveConflict(resolution: ConflictResolution, applyToAll: Boolean = false) {
        pendingConflictDecision?.complete(ConflictDecision(resolution, applyToAll))
    }

    /** Stops the paste; items already handled stay where they are. */
    fun dismissConflict() {
        pendingConflictDecision?.complete(null)
    }

    /** Stops the paste once the item in progress is finished, since a half done item can't be undone. */
    fun cancelPaste() {
        pasteCancelRequested = true
        pendingConflictDecision?.complete(null)
    }

    fun deselectAllFiles() {
        _state.value = _state.value.copy(selectedFiles = emptySet())
    }

    fun areAllFilesSelected(): Boolean {
        val allFiles = _state.value.files.filterNot { it.isParentDirectory }
        return allFiles.isNotEmpty() && _state.value.selectedFiles.size == allFiles.size
    }

    fun downloadSelectedFiles() {
        val selectedPaths = _state.value.selectedFiles.toList()
        val files = _state.value.files.filter { it.path in selectedPaths && !it.isDirectory }

        if (files.isEmpty()) {
            viewModelScope.launch {
                _events.emit(FileBrowserEvent.ShowToast(R.string.no_files_download))
            }
            return
        }

        exitSelectionMode()

        files.forEach { file ->
            downloadFile(file.path, file.name)
        }
    }
}
