package `in`.hridayan.ashell.shell.file_browser.presentation.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictResolution
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileConflict
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperation
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationStatus
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationType
import `in`.hridayan.ashell.shell.file_browser.domain.model.PendingPasteOperation
import `in`.hridayan.ashell.shell.file_browser.domain.model.PendingPasteItem
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.file_browser.domain.repository.FileBrowserRepository
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class FileBrowserState(
    val currentPath: String = "/storage/emulated/0",
    val files: List<RemoteFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFile: RemoteFile? = null,
    val operations: List<FileOperation> = emptyList(),
    val isVirtualEmptyFolder: Boolean = false,
    val lastSuccessfulPath: String = "/storage/emulated/0",
    // Conflict handling state
    val pendingConflict: FileConflict? = null,
    val pendingPasteOperation: PendingPasteOperation? = null,
    val applyToAllResolution: ConflictResolution? = null,
    // Selection mode
    val selectedFiles: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)

sealed class FileBrowserEvent {
    data class ShowToast(val message: String) : FileBrowserEvent()
    data class FileDownloaded(val localPath: String) : FileBrowserEvent()
    data class FileUploaded(val remotePath: String) : FileBrowserEvent()
    data object FileDeleted : FileBrowserEvent()
    data object DirectoryCreated : FileBrowserEvent()
}


@HiltViewModel
class FileBrowserViewModel @Inject constructor(
    private val repository: FileBrowserRepository,
    private val wifiAdbRepository: WifiAdbRepository
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

    init {
        lastConnectedDevice = wifiAdbRepository.getCurrentDevice()
        Log.d(
            "FileBrowserVM",
            "Init: captured device = ${lastConnectedDevice?.deviceName} at ${lastConnectedDevice?.ip}:${lastConnectedDevice?.port}"
        )

        // Start at internal storage (more reliable than /sdcard symlink)
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
                },
                onFailure = { error ->
                    // Keep current state, just show error
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load files"
                    )
                    _events.emit(FileBrowserEvent.ShowToast("Error: ${error.message}"))
                }
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
     * Check if ADB is currently connected
     */
    fun isAdbConnected(): Boolean = repository.isAdbConnected()

    /**
     * Attempt a silent reconnect using the stored device and refresh file list.
     * This provides seamless reconnection when the connection drops.
     * Uses the device captured at init or set via setConnectedDevice.
     */
    fun silentReconnectAndRefresh() {
        Log.d("FileBrowserVM", "silentReconnectAndRefresh called")

        val device = lastConnectedDevice
        if (device == null) {
            Log.w("FileBrowserVM", "No connected device stored, falling back to refresh")
            refresh()
            return
        }

        Log.d(
            "FileBrowserVM",
            "Using stored device: ${device.deviceName} at ${device.ip}:${device.port}"
        )
        _state.value = _state.value.copy(isLoading = true, error = null)
        performReconnect(device)
    }

    private fun performReconnect(device: WifiAdbDevice) {
        Log.d(
            "FileBrowserVM",
            "Attempting reconnect to: ${device.deviceName} at ${device.ip}:${device.port}"
        )

        wifiAdbRepository.reconnect(device, object : WifiAdbRepositoryImpl.ReconnectListener {
            override fun onReconnectSuccess() {
                Log.d("FileBrowserVM", "Reconnect SUCCESS")
                // Update stored device with fresh reference after successful reconnect
                lastConnectedDevice = wifiAdbRepository.getCurrentDevice() ?: device
                viewModelScope.launch {
                    _events.emit(FileBrowserEvent.ShowToast("Reconnected successfully"))
                    refresh()
                }
            }

            override fun onReconnectFailed(requiresPairing: Boolean) {
                Log.e("FileBrowserVM", "Reconnect FAILED, requiresPairing=$requiresPairing")
                viewModelScope.launch {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = if (requiresPairing) "Connection lost. Please re-pair device." else "Reconnection failed"
                    )
                }
            }
        })
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
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val localPath = File(downloadsDir, fileName).absolutePath
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
            repository.pullFile(remotePath, localPath).collect { result ->
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
                        _events.emit(FileBrowserEvent.FileDownloaded(localPath))
                        _events.emit(FileBrowserEvent.ShowToast("Downloaded to $localPath"))
                        // Delay removal to let UI show completion
                        kotlinx.coroutines.delay(2000)
                        removeOperation(operationId)
                    }

                    is FileOperationResult.Error -> {
                        updateOperation(operationId) {
                            it.copy(
                                status = OperationStatus.FAILED,
                                message = "Failed: ${result.message}"
                            )
                        }
                        _events.emit(FileBrowserEvent.ShowToast("Download failed: ${result.message}"))
                        kotlinx.coroutines.delay(3000)
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
                        _events.emit(FileBrowserEvent.ShowToast("File uploaded successfully"))
                        refresh()
                        kotlinx.coroutines.delay(2000)
                        removeOperation(operationId)
                    }

                    is FileOperationResult.Error -> {
                        updateOperation(operationId) {
                            it.copy(
                                status = OperationStatus.FAILED,
                                message = "Failed: ${result.message}"
                            )
                        }
                        _events.emit(FileBrowserEvent.ShowToast("Upload failed: ${result.message}"))
                        kotlinx.coroutines.delay(3000)
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
                    _events.emit(FileBrowserEvent.ShowToast("File deleted"))
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(FileBrowserEvent.ShowToast("Delete failed: ${error.message}"))
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
                    _events.emit(FileBrowserEvent.ShowToast("Folder created"))
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(FileBrowserEvent.ShowToast("Failed to create folder: ${error.message}"))
                }
            )
        }
    }

    fun renameFile(oldPath: String, newPath: String) {
        viewModelScope.launch {
            repository.rename(oldPath, newPath).fold(
                onSuccess = {
                    _events.emit(FileBrowserEvent.ShowToast("Renamed successfully"))
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(FileBrowserEvent.ShowToast("Rename failed: ${error.message}"))
                }
            )
        }
    }

    fun cancelOperation(operationId: String) {
        operationJobs[operationId]?.cancel()
        removeOperation(operationId)
        viewModelScope.launch {
            _events.emit(FileBrowserEvent.ShowToast("Operation cancelled"))
        }
    }

    fun cancelAllOperations() {
        operationJobs.values.forEach { it.cancel() }
        operationJobs.clear()
        _state.value = _state.value.copy(operations = emptyList())
        viewModelScope.launch {
            _events.emit(FileBrowserEvent.ShowToast("All operations cancelled"))
        }
    }

    fun copyFile(sourcePath: String, destPath: String, forceOverwrite: Boolean = false) {
        val fileName = File(sourcePath).name
        val operationId = UUID.randomUUID().toString()

        viewModelScope.launch {
            Log.d(TAG, "copyFile: source=$sourcePath, dest=$destPath, forceOverwrite=$forceOverwrite")
            // Check if destination exists (unless forcing overwrite)
            if (!forceOverwrite) {
                val exists = repository.exists(destPath).getOrNull() ?: false
                Log.d(TAG, "copyFile: exists check for '$destPath' = $exists")
                if (exists) {
                    // Check if it's a directory to show appropriate dialog
                    val isDir = repository.isDirectory(destPath).getOrNull() ?: false
                    _state.value = _state.value.copy(
                        pendingConflict = FileConflict(
                            sourcePath = sourcePath,
                            destPath = destPath,
                            operationType = OperationType.COPY,
                            isDirectory = isDir,
                            fileName = fileName
                        )
                    )
                    return@launch
                }
            } else {
                // Force overwrite - delete existing first
                val exists = repository.exists(destPath).getOrNull() ?: false
                if (exists) {
                    repository.delete(destPath) // Delete existing before copy
                }
            }

            val operation = FileOperation(
                id = operationId,
                type = OperationType.COPY,
                fileName = fileName,
                message = "Copying..."
            )
            addOperation(operation)

            repository.copy(sourcePath, destPath).fold(
                onSuccess = {
                    removeOperation(operationId)
                    _events.emit(FileBrowserEvent.ShowToast("Copied successfully"))
                    refresh()
                },
                onFailure = { error ->
                    removeOperation(operationId)
                    _events.emit(FileBrowserEvent.ShowToast("Copy failed: ${error.message}"))
                }
            )
        }
    }

    fun moveFile(sourcePath: String, destPath: String, forceOverwrite: Boolean = false) {
        val fileName = File(sourcePath).name
        val operationId = UUID.randomUUID().toString()

        viewModelScope.launch {
            Log.d(TAG, "moveFile: source=$sourcePath, dest=$destPath, forceOverwrite=$forceOverwrite")
            // Check if destination exists (unless forcing overwrite)
            if (!forceOverwrite) {
                val exists = repository.exists(destPath).getOrNull() ?: false
                Log.d(TAG, "moveFile: exists check for '$destPath' = $exists")
                if (exists) {
                    // Check if it's a directory to show appropriate dialog
                    val isDir = repository.isDirectory(destPath).getOrNull() ?: false
                    _state.value = _state.value.copy(
                        pendingConflict = FileConflict(
                            sourcePath = sourcePath,
                            destPath = destPath,
                            operationType = OperationType.MOVE,
                            isDirectory = isDir,
                            fileName = fileName
                        )
                    )
                    return@launch
                }
            } else {
                // Force overwrite - delete existing first
                val exists = repository.exists(destPath).getOrNull() ?: false
                if (exists) {
                    repository.delete(destPath) // Delete existing before move
                }
            }

            val operation = FileOperation(
                id = operationId,
                type = OperationType.MOVE,
                fileName = fileName,
                message = "Moving..."
            )
            addOperation(operation)

            repository.move(sourcePath, destPath).fold(
                onSuccess = {
                    removeOperation(operationId)
                    _events.emit(FileBrowserEvent.ShowToast("Moved successfully"))
                    refresh()
                },
                onFailure = { error ->
                    removeOperation(operationId)
                    _events.emit(FileBrowserEvent.ShowToast("Move failed: ${error.message}"))
                }
            )
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
                        _events.emit(FileBrowserEvent.ShowToast("Failed to delete: ${error.message}"))
                    }
                )
            }
            _events.emit(FileBrowserEvent.ShowToast("Deleted ${paths.size} items"))
            refresh()
        }
    }

    fun getSelectedFilePaths(): List<String> = _state.value.selectedFiles.toList()

    /**
     * Start a batch paste operation with conflict detection for all items upfront.
     * This is the main entry point for copy/move from clipboard.
     */
    fun copyFileBatch(sourcePaths: List<String>, destDir: String) {
        startPasteOperation(sourcePaths, destDir, OperationType.COPY)
    }

    fun moveFileBatch(sourcePaths: List<String>, destDir: String) {
        startPasteOperation(sourcePaths, destDir, OperationType.MOVE)
    }

    private fun startPasteOperation(sourcePaths: List<String>, destDir: String, operationType: OperationType) {
        Log.d(TAG, "startPasteOperation: sources=$sourcePaths, destDir=$destDir, op=$operationType")
        viewModelScope.launch {
            // Build list of pending items - use quick check with fallback
            val items = sourcePaths.map { sourcePath ->
                val fileName = sourcePath.substringAfterLast("/")
                val destPath = "$destDir/$fileName"
                // Try to determine if source is directory, but don't block on failure
                // If the path has no extension and doesn't contain a dot, likely a directory
                val isDir = try {
                    kotlinx.coroutines.withTimeoutOrNull(500L) {
                        repository.isDirectory(sourcePath).getOrNull()
                    } ?: !fileName.contains(".")
                } catch (e: Exception) {
                    !fileName.contains(".") // Fallback: no extension = likely directory
                }
                Log.d(TAG, "startPasteOperation: item source=$sourcePath, dest=$destPath, isDir=$isDir")
                PendingPasteItem(
                    sourcePath = sourcePath,
                    destPath = destPath,
                    isDirectory = isDir
                )
            }

            if (items.isEmpty()) {
                Log.d(TAG, "startPasteOperation: no items to paste")
                _events.emit(FileBrowserEvent.ShowToast("Nothing to paste"))
                return@launch
            }

            // Create pending operation and start processing
            val pendingOp = PendingPasteOperation(
                operationType = operationType,
                destDir = destDir,
                items = items,
                currentIndex = 0
            )

            Log.d(TAG, "startPasteOperation: created pendingOp with ${items.size} items")
            _state.value = _state.value.copy(
                pendingPasteOperation = pendingOp,
                applyToAllResolution = null // Reset apply-to-all
            )

            // Process first item
            processNextPasteItem()
        }
    }

    /**
     * Process the next item in the pending paste operation.
     * Called after each conflict resolution or when no conflict exists.
     */
    private fun processNextPasteItem() {
        viewModelScope.launch {
            val pendingOp = _state.value.pendingPasteOperation
            if (pendingOp == null) {
                Log.d(TAG, "processNextPasteItem: pendingPasteOperation is NULL - exiting")
                return@launch
            }
            
            Log.d(TAG, "processNextPasteItem: currentIndex=${pendingOp.currentIndex}, totalItems=${pendingOp.items.size}, isComplete=${pendingOp.isComplete}")

            if (pendingOp.isComplete) {
                Log.d(TAG, "processNextPasteItem: operation complete - finalizing")
                finalizePasteOperation()
                return@launch
            }

            val currentItem = pendingOp.currentItem
            if (currentItem == null) {
                Log.d(TAG, "processNextPasteItem: currentItem is NULL - exiting")
                return@launch
            }
            
            Log.d(TAG, "processNextPasteItem: checking exists for destPath=${currentItem.destPath}")
            
            // Use cached file list to check for conflicts - NO ADB CALLS!
            // The _state.value.files contains the current directory listing
            val destFileName = currentItem.destPath.substringAfterLast("/")
            val existingFile = _state.value.files.find { it.name == destFileName }
            val destExists = existingFile != null
            val destIsDir = existingFile?.isDirectory ?: false
            
            Log.d(TAG, "processNextPasteItem: cached lookup - exists=$destExists, isDir=$destIsDir")

            if (destExists) {
                Log.d(TAG, "processNextPasteItem: CONFLICT DETECTED for ${currentItem.destPath}")
                // Check if we have an apply-to-all resolution cached
                val cachedResolution = _state.value.applyToAllResolution
                if (cachedResolution != null) {
                    Log.d(TAG, "processNextPasteItem: using cached resolution: $cachedResolution")
                    executeResolution(cachedResolution, currentItem, pendingOp)
                } else {
                    Log.d(TAG, "processNextPasteItem: showing conflict dialog")
                    // Show conflict dialog using cached data
                    val conflict = FileConflict(
                        sourcePath = currentItem.sourcePath,
                        destPath = currentItem.destPath,
                        operationType = pendingOp.operationType,
                        isDirectory = destIsDir,
                        sourceIsDirectory = currentItem.isDirectory,
                        fileName = currentItem.sourcePath.substringAfterLast("/"),
                        remainingCount = pendingOp.remainingCount
                    )
                    Log.d(TAG, "processNextPasteItem: setting pendingConflict=$conflict")
                    _state.value = _state.value.copy(pendingConflict = conflict)
                    Log.d(TAG, "processNextPasteItem: state.pendingConflict is now ${_state.value.pendingConflict}")
                }
            } else {
                Log.d(TAG, "processNextPasteItem: no conflict - executing operation")
                executeOperation(currentItem, pendingOp)
            }
        }
    }

    /**
     * Execute the actual copy/move operation for an item.
     */
    private suspend fun executeOperation(item: PendingPasteItem, pendingOp: PendingPasteOperation) {
        val result = when (pendingOp.operationType) {
            OperationType.COPY -> repository.copy(item.sourcePath, item.destPath)
            OperationType.MOVE -> repository.move(item.sourcePath, item.destPath)
            else -> Result.failure(Exception("Invalid operation type"))
        }

        val newOp = result.fold(
            onSuccess = {
                pendingOp.copy(
                    currentIndex = pendingOp.currentIndex + 1,
                    processedCount = pendingOp.processedCount + 1
                )
            },
            onFailure = {
                Log.e(TAG, "Failed to ${pendingOp.operationType.name.lowercase()} ${item.sourcePath}", it)
                pendingOp.copy(
                    currentIndex = pendingOp.currentIndex + 1,
                    failedCount = pendingOp.failedCount + 1
                )
            }
        )

        _state.value = _state.value.copy(pendingPasteOperation = newOp)
        processNextPasteItem()
    }

    /**
     * Execute a conflict resolution for the current item.
     */
    private suspend fun executeResolution(resolution: ConflictResolution, item: PendingPasteItem, pendingOp: PendingPasteOperation) {
        when (resolution) {
            ConflictResolution.SKIP -> {
                // Skip - just move to next item
                val newOp = pendingOp.copy(
                    currentIndex = pendingOp.currentIndex + 1,
                    skippedCount = pendingOp.skippedCount + 1
                )
                _state.value = _state.value.copy(pendingPasteOperation = newOp)
                processNextPasteItem()
            }

            ConflictResolution.REPLACE -> {
                // Delete existing, then copy/move
                val deleteResult = repository.delete(item.destPath)
                if (deleteResult.isSuccess) {
                    executeOperation(item, pendingOp)
                } else {
                    Log.e(TAG, "Failed to delete ${item.destPath} for replace")
                    val newOp = pendingOp.copy(
                        currentIndex = pendingOp.currentIndex + 1,
                        failedCount = pendingOp.failedCount + 1
                    )
                    _state.value = _state.value.copy(pendingPasteOperation = newOp)
                    processNextPasteItem()
                }
            }

            ConflictResolution.KEEP_BOTH -> {
                // Generate unique name and copy/move
                val uniquePath = generateUniqueName(item.destPath)
                val newItem = item.copy(destPath = uniquePath)
                executeOperation(newItem, pendingOp)
            }

            ConflictResolution.MERGE -> {
                // Merge directories
                if (item.isDirectory) {
                    mergeDirectoryContents(item, pendingOp)
                } else {
                    // For files, merge = keep both
                    executeResolution(ConflictResolution.KEEP_BOTH, item, pendingOp)
                }
            }
        }
    }

    /**
     * Generate a unique name for Keep Both: filename (1).ext, filename (2).ext, etc.
     */
    private suspend fun generateUniqueName(destPath: String): String {
        val file = File(destPath)
        val baseName = file.nameWithoutExtension
        val extension = file.extension.let { if (it.isNotEmpty()) ".$it" else "" }
        val parentPath = file.parent ?: _state.value.currentPath

        var counter = 1
        var newPath: String
        do {
            newPath = "$parentPath/$baseName ($counter)$extension"
            counter++
        } while (repository.exists(newPath).getOrNull() == true && counter < 100)

        return newPath
    }

    /**
     * Merge source directory contents into destination directory.
     * Contents of source are copied/moved into destination without deleting destination.
     */
    private suspend fun mergeDirectoryContents(item: PendingPasteItem, pendingOp: PendingPasteOperation) {
        val sourceFiles = repository.listFiles(item.sourcePath).getOrNull() ?: emptyList()
        val filesToMerge = sourceFiles.filterNot { it.isParentDirectory || it.name == ".." }

        if (filesToMerge.isEmpty()) {
            // Empty source folder - skip
            val newOp = pendingOp.copy(
                currentIndex = pendingOp.currentIndex + 1,
                skippedCount = pendingOp.skippedCount + 1
            )
            _state.value = _state.value.copy(pendingPasteOperation = newOp)
            _events.emit(FileBrowserEvent.ShowToast("Empty folder skipped"))
            processNextPasteItem()
            return
        }

        // Create items for nested paste operation
        val nestedItems = filesToMerge.map { file ->
            PendingPasteItem(
                sourcePath = file.path,
                destPath = "${item.destPath}/${file.name}",
                isDirectory = file.isDirectory
            )
        }

        // Create nested operation - append to current items
        val remainingItems = pendingOp.items.drop(pendingOp.currentIndex + 1)
        val newItems = nestedItems + remainingItems
        
        val newOp = PendingPasteOperation(
            operationType = pendingOp.operationType,
            destDir = item.destPath,
            items = newItems,
            currentIndex = 0,
            processedCount = pendingOp.processedCount,
            skippedCount = pendingOp.skippedCount,
            failedCount = pendingOp.failedCount
        )

        _state.value = _state.value.copy(pendingPasteOperation = newOp)
        
        // If move operation, we'll delete source folder after merge completes
        // For now, just process nested items
        processNextPasteItem()
    }

    /**
     * Finalize the paste operation - show summary and cleanup state.
     */
    private suspend fun finalizePasteOperation() {
        val pendingOp = _state.value.pendingPasteOperation ?: return

        val message = when {
            pendingOp.failedCount == 0 && pendingOp.skippedCount == 0 ->
                "Completed: ${pendingOp.processedCount} items"
            pendingOp.failedCount == 0 ->
                "Completed: ${pendingOp.processedCount} items, ${pendingOp.skippedCount} skipped"
            else ->
                "Completed: ${pendingOp.processedCount} items, ${pendingOp.skippedCount} skipped, ${pendingOp.failedCount} failed"
        }

        _state.value = _state.value.copy(
            pendingPasteOperation = null,
            pendingConflict = null,
            applyToAllResolution = null
        )

        _events.emit(FileBrowserEvent.ShowToast(message))
        refresh()
    }

    /**
     * Handle user's conflict resolution from the dialog.
     */
    fun resolveConflict(resolution: ConflictResolution, applyToAll: Boolean = false) {
        val conflict = _state.value.pendingConflict ?: return
        val pendingOp = _state.value.pendingPasteOperation ?: return
        val currentItem = pendingOp.currentItem ?: return

        // Cache resolution if apply-to-all
        if (applyToAll) {
            _state.value = _state.value.copy(applyToAllResolution = resolution)
        }

        // Clear current conflict dialog
        _state.value = _state.value.copy(pendingConflict = null)

        // Execute resolution
        viewModelScope.launch {
            executeResolution(resolution, currentItem, pendingOp)
        }
    }

    /**
     * Dismiss conflict dialog and cancel the entire paste operation.
     */
    fun dismissConflict() {
        _state.value = _state.value.copy(
            pendingConflict = null,
            pendingPasteOperation = null,
            applyToAllResolution = null
        )
        viewModelScope.launch {
            _events.emit(FileBrowserEvent.ShowToast("Operation cancelled"))
        }
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
                _events.emit(FileBrowserEvent.ShowToast("No files selected (folders cannot be downloaded)"))
            }
            return
        }

        exitSelectionMode()

        files.forEach { file ->
            downloadFile(file.path, file.name)
        }
    }
}
