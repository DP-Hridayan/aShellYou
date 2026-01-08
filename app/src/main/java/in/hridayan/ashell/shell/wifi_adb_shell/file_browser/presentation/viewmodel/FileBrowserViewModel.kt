package `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.presentation.viewmodel

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.repository.FileBrowserRepository
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
    val pendingConflict: ConflictInfo? = null
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
    private val repository: FileBrowserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FileBrowserState())
    val state: StateFlow<FileBrowserState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FileBrowserEvent>()
    val events: SharedFlow<FileBrowserEvent> = _events.asSharedFlow()

    private val pathHistory = mutableListOf<String>()
    private var navigationJob: Job? = null
    private val operationJobs = mutableMapOf<String, Job>()

    init {
        // Start at internal storage (more reliable than /sdcard symlink)
        loadFiles("/storage/emulated/0")
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

    fun navigateUp(): Boolean {
        val parentPath = File(_state.value.currentPath).parent
        return if (parentPath != null && _state.value.currentPath != "/") {
            loadFiles(parentPath)
            true
        } else {
            false
        }
    }

    fun navigateBack(): Boolean {
        return if (pathHistory.isNotEmpty()) {
            val previousPath = pathHistory.removeAt(pathHistory.lastIndex)
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true, error = null)
                repository.listFiles(previousPath).fold(
                    onSuccess = { files ->
                        _state.value = _state.value.copy(
                            currentPath = previousPath,
                            files = files,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
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

    fun clearSelectedFile() {
        _state.value = _state.value.copy(selectedFile = null)
    }

    fun downloadFile(remotePath: String, fileName: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
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
    
    fun copyFile(sourcePath: String, destPath: String) {
        val fileName = File(sourcePath).name
        val operationId = UUID.randomUUID().toString()
        val operation = FileOperation(
            id = operationId,
            type = OperationType.COPY,
            fileName = fileName,
            message = "Copying..."
        )
        addOperation(operation)

        val job = viewModelScope.launch {
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
        operationJobs[operationId] = job
    }
    
    fun moveFile(sourcePath: String, destPath: String) {
        val fileName = File(sourcePath).name
        val operationId = UUID.randomUUID().toString()
        val operation = FileOperation(
            id = operationId,
            type = OperationType.MOVE,
            fileName = fileName,
            message = "Moving..."
        )
        addOperation(operation)

        val job = viewModelScope.launch {
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
        operationJobs[operationId] = job
    }
    
    fun dismissConflict() {
        _state.value = _state.value.copy(pendingConflict = null)
    }
}
