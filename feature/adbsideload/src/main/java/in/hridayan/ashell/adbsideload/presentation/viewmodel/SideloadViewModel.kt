package `in`.hridayan.ashell.adbsideload.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadPackageInfo
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SideloadViewModel @Inject constructor(
    private val repository: SideloadRepository
) : ViewModel() {

    val state: StateFlow<SideloadState> = repository.connectionState

    private val _operation = MutableStateFlow(SideloadOperation())
    val operation: StateFlow<SideloadOperation> = _operation.asStateFlow()

    private val _selectedFile = MutableStateFlow<SideloadPackageInfo?>(null)
    val selectedFile: StateFlow<SideloadPackageInfo?> = _selectedFile.asStateFlow()

    private var sideloadJob: Job? = null

    fun startScan() {
        repository.searchDevices()
    }

    fun retryPermission() {
        repository.retryPermission()
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun selectFile(uri: Uri) {
        viewModelScope.launch { _selectedFile.value = repository.inspectPackage(uri) }
    }

    fun clearFile() {
        _selectedFile.value = null
    }

    fun sideload() {
        val file = _selectedFile.value ?: return
        sideloadJob?.cancel()
        sideloadJob = viewModelScope.launch {
            repository.sideload(file.uri).collect { progress -> _operation.value = progress }
        }
    }

    fun cancelSideload() {
        sideloadJob?.cancel()
        sideloadJob = null
        repository.cancelSideload()
        _operation.update { it.copy(status = SideloadStatus.CANCELLED) }
    }

    fun resetOperation() {
        sideloadJob?.cancel()
        sideloadJob = null
        _operation.value = SideloadOperation()
    }
}
