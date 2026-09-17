package `in`.hridayan.ashell.adbsideload.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadPackageInfo
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadPackageInspector
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SideloadViewModel @Inject constructor(
    private val repository: SideloadRepository,
    private val packageInspector: SideloadPackageInspector,
) : ViewModel() {

    val state: StateFlow<SideloadState> = repository.connectionState

    val operation: StateFlow<SideloadOperation> = repository.operation

    private val _selectedFile = MutableStateFlow<SideloadPackageInfo?>(null)
    val selectedFile: StateFlow<SideloadPackageInfo?> = _selectedFile.asStateFlow()

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
        viewModelScope.launch { _selectedFile.value = packageInspector.inspect(uri) }
    }

    fun clearFile() {
        _selectedFile.value = null
    }

    fun sideload() {
        val file = _selectedFile.value ?: return
        repository.sideload(file.uri)
    }

    fun cancelSideload() {
        repository.cancelSideload()
    }

    fun resetOperation() {
        repository.resetOperation()
    }
}
