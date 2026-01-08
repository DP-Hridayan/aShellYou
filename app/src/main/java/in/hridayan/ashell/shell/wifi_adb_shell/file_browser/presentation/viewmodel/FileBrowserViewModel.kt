package `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.presentation.viewmodel

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.repository.FileBrowserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class FileBrowserState(
    val currentPath: String = "/storage/emulated/0",
    val files: List<RemoteFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFile: RemoteFile? = null,
    val isOperationInProgress: Boolean = false,
    val operationProgress: Float = 0f,
    val operationMessage: String? = null
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

    // Path history for back navigation
    private val pathHistory = mutableListOf<String>()
    
    // Navigation job for debouncing rapid clicks
    private var navigationJob: kotlinx.coroutines.Job? = null

    init {
        // Start at internal storage (more reliable than /sdcard symlink)
        loadFiles("/storage/emulated/0")
    }

    fun loadFiles(path: String) {
        // Cancel any pending navigation
        navigationJob?.cancel()
        
        navigationJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.listFiles(path).fold(
                onSuccess = { files ->
                    // Add current path to history if navigating forward
                    if (path != _state.value.currentPath && !path.startsWith(_state.value.currentPath + "/..")) {
                        pathHistory.add(_state.value.currentPath)
                    }
                    _state.value = _state.value.copy(
                        currentPath = path,
                        files = files,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load files"
                    )
                }
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

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isOperationInProgress = true,
                operationProgress = 0f,
                operationMessage = "Downloading..."
            )

            repository.pullFile(remotePath, localPath).collect { result ->
                when (result) {
                    is FileOperationResult.Progress -> {
                        val progress = if (result.total > 0) {
                            result.current.toFloat() / result.total.toFloat()
                        } else 0f
                        _state.value = _state.value.copy(operationProgress = progress)
                    }
                    is FileOperationResult.Success -> {
                        _state.value = _state.value.copy(
                            isOperationInProgress = false,
                            operationMessage = null
                        )
                        _events.emit(FileBrowserEvent.FileDownloaded(localPath))
                        _events.emit(FileBrowserEvent.ShowToast("Downloaded to $localPath"))
                    }
                    is FileOperationResult.Error -> {
                        _state.value = _state.value.copy(
                            isOperationInProgress = false,
                            operationMessage = null
                        )
                        _events.emit(FileBrowserEvent.ShowToast("Download failed: ${result.message}"))
                    }
                }
            }
        }
    }

    fun uploadFile(localPath: String, fileName: String) {
        val remotePath = "${_state.value.currentPath}/$fileName"

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isOperationInProgress = true,
                operationProgress = 0f,
                operationMessage = "Uploading..."
            )

            repository.pushFile(localPath, remotePath).collect { result ->
                when (result) {
                    is FileOperationResult.Progress -> {
                        val progress = if (result.total > 0) {
                            result.current.toFloat() / result.total.toFloat()
                        } else 0f
                        _state.value = _state.value.copy(operationProgress = progress)
                    }
                    is FileOperationResult.Success -> {
                        _state.value = _state.value.copy(
                            isOperationInProgress = false,
                            operationMessage = null
                        )
                        _events.emit(FileBrowserEvent.FileUploaded(remotePath))
                        _events.emit(FileBrowserEvent.ShowToast("File uploaded successfully"))
                        loadFiles(_state.value.currentPath) // Refresh
                    }
                    is FileOperationResult.Error -> {
                        _state.value = _state.value.copy(
                            isOperationInProgress = false,
                            operationMessage = null
                        )
                        _events.emit(FileBrowserEvent.ShowToast("Upload failed: ${result.message}"))
                    }
                }
            }
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isOperationInProgress = true,
                operationMessage = "Deleting..."
            )

            repository.deleteFile(path).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isOperationInProgress = false,
                        operationMessage = null,
                        selectedFile = null
                    )
                    _events.emit(FileBrowserEvent.FileDeleted)
                    _events.emit(FileBrowserEvent.ShowToast("File deleted"))
                    loadFiles(_state.value.currentPath) // Refresh
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isOperationInProgress = false,
                        operationMessage = null
                    )
                    _events.emit(FileBrowserEvent.ShowToast("Delete failed: ${error.message}"))
                }
            )
        }
    }

    fun createDirectory(name: String) {
        val path = "${_state.value.currentPath}/$name"

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isOperationInProgress = true,
                operationMessage = "Creating folder..."
            )

            repository.createDirectory(path).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isOperationInProgress = false,
                        operationMessage = null
                    )
                    _events.emit(FileBrowserEvent.DirectoryCreated)
                    _events.emit(FileBrowserEvent.ShowToast("Folder created"))
                    loadFiles(_state.value.currentPath) // Refresh
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isOperationInProgress = false,
                        operationMessage = null
                    )
                    _events.emit(FileBrowserEvent.ShowToast("Failed to create folder: ${error.message}"))
                }
            )
        }
    }

    fun refresh() {
        loadFiles(_state.value.currentPath)
    }
}
