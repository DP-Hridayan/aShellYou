package `in`.hridayan.ashell.shell.file_browser.presentation.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
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

enum class OperationType { UPLOAD, DOWNLOAD, COPY, MOVE }

data class FileOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val fileName: String,
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val message: String = ""
)

data class FileBrowserState(
    val currentPath: String = "/storage/emulated/0",
    val files: List<RemoteFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFile: RemoteFile? = null,
    val operations: List<FileOperation> = emptyList(),
    val isVirtualEmptyFolder: Boolean = false,
    val lastSuccessfulPath: String = "/storage/emulated/0",
    val pendingConflict: ConflictInfo? = null,
    val selectedFiles: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)

data class ConflictInfo(
    val sourcePath: String,
    val destPath: String,
    val isDirectory: Boolean,
    val operationType: OperationType
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

    private val _state = MutableStateFlow(FileBrowserState())
    val state: StateFlow<FileBrowserState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FileBrowserEvent>()
    val events: SharedFlow<FileBrowserEvent> = _events.asSharedFlow()

    private val pathHistory = mutableListOf<String>()
    private var navigationJob: Job? = null
    private val operationJobs = mutableMapOf<String, Job>()
    private var connectedDevice: WifiAdbDevice? = null

    init {
        connectedDevice = wifiAdbRepository.getCurrentDevice()
        Log.d(
            "FileBrowserVM",
            "Init: captured device = ${connectedDevice?.deviceName} at ${connectedDevice?.ip}:${connectedDevice?.port}"
        )

        // Start at internal storage (more reliable than /sdcard symlink)
        loadFiles("/storage/emulated/0")
    }

    /**
     * Set the connected device explicitly (called from UI with device from navigation if needed)
     */
    fun setConnectedDevice(device: WifiAdbDevice?) {
        connectedDevice = device
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

        val device = connectedDevice
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
                connectedDevice = wifiAdbRepository.getCurrentDevice() ?: device
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
            message = "Downloading..."
        )
        addOperation(operation)

        val job = viewModelScope.launch {
            repository.pullFile(remotePath, localPath).collect { result ->
                when (result) {
                    is FileOperationResult.Progress -> {
                        updateOperation(operationId) {
                            it.copy(bytesTransferred = result.current, totalBytes = result.total)
                        }
                    }

                    is FileOperationResult.Success -> {
                        removeOperation(operationId)
                        _events.emit(FileBrowserEvent.FileDownloaded(localPath))
                        _events.emit(FileBrowserEvent.ShowToast("Downloaded to $localPath"))
                    }

                    is FileOperationResult.Error -> {
                        removeOperation(operationId)
                        _events.emit(FileBrowserEvent.ShowToast("Download failed: ${result.message}"))
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
            message = "Uploading..."
        )
        addOperation(operation)

        val job = viewModelScope.launch {
            repository.pushFile(localPath, remotePath).collect { result ->
                when (result) {
                    is FileOperationResult.Progress -> {
                        updateOperation(operationId) {
                            it.copy(bytesTransferred = result.current, totalBytes = result.total)
                        }
                    }

                    is FileOperationResult.Success -> {
                        removeOperation(operationId)
                        _events.emit(FileBrowserEvent.FileUploaded(remotePath))
                        _events.emit(FileBrowserEvent.ShowToast("File uploaded successfully"))
                        refresh()
                    }

                    is FileOperationResult.Error -> {
                        removeOperation(operationId)
                        _events.emit(FileBrowserEvent.ShowToast("Upload failed: ${result.message}"))
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
            // Check if destination exists (unless forcing overwrite)
            if (!forceOverwrite) {
                val exists = repository.exists(destPath).getOrNull() ?: false
                if (exists) {
                    _state.value = _state.value.copy(
                        pendingConflict = ConflictInfo(
                            sourcePath = sourcePath,
                            destPath = destPath,
                            isDirectory = false,
                            operationType = OperationType.COPY
                        )
                    )
                    return@launch
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
            // Check if destination exists (unless forcing overwrite)
            if (!forceOverwrite) {
                val exists = repository.exists(destPath).getOrNull() ?: false
                if (exists) {
                    _state.value = _state.value.copy(
                        pendingConflict = ConflictInfo(
                            sourcePath = sourcePath,
                            destPath = destPath,
                            isDirectory = false,
                            operationType = OperationType.MOVE
                        )
                    )
                    return@launch
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

    fun resolveConflictReplace() {
        val conflict = _state.value.pendingConflict ?: return
        dismissConflict()
        when (conflict.operationType) {
            OperationType.COPY -> copyFile(
                conflict.sourcePath,
                conflict.destPath,
                forceOverwrite = true
            )

            OperationType.MOVE -> moveFile(
                conflict.sourcePath,
                conflict.destPath,
                forceOverwrite = true
            )

            else -> {}
        }
    }

    fun resolveConflictKeepBoth() {
        val conflict = _state.value.pendingConflict ?: return
        dismissConflict()
        // Generate new name with (1), (2), etc.
        val destFile = File(conflict.destPath)
        val baseName = destFile.nameWithoutExtension
        val extension = destFile.extension.let { if (it.isNotEmpty()) ".$it" else "" }
        val parentPath = destFile.parent ?: _state.value.currentPath

        viewModelScope.launch {
            var counter = 1
            var newPath: String
            do {
                newPath = "$parentPath/${baseName} ($counter)$extension"
                counter++
            } while (repository.exists(newPath).getOrNull() == true && counter < 100)

            when (conflict.operationType) {
                OperationType.COPY -> copyFile(conflict.sourcePath, newPath, forceOverwrite = true)
                OperationType.MOVE -> moveFile(conflict.sourcePath, newPath, forceOverwrite = true)
                else -> {}
            }
        }
    }

    fun resolveConflictSkip() {
        dismissConflict()
        viewModelScope.launch {
            _events.emit(FileBrowserEvent.ShowToast("Skipped"))
        }
    }

    fun dismissConflict() {
        _state.value = _state.value.copy(pendingConflict = null)
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

    fun copyFileBatch(sourcePaths: List<String>, destDir: String) {
        sourcePaths.forEach { sourcePath ->
            val fileName = sourcePath.substringAfterLast("/")
            val destPath = "$destDir/$fileName"
            copyFile(sourcePath, destPath)
        }
    }

    fun moveFileBatch(sourcePaths: List<String>, destDir: String) {
        sourcePaths.forEach { sourcePath ->
            val fileName = sourcePath.substringAfterLast("/")
            val destPath = "$destDir/$fileName"
            moveFile(sourcePath, destPath)
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
