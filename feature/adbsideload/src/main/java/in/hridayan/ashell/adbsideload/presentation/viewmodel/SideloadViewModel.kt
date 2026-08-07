package `in`.hridayan.ashell.adbsideload.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadConnection
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SideloadViewModel @Inject constructor(
    private val repository: SideloadRepository
) : ViewModel() {

    val state: StateFlow<SideloadState> = SideloadConnection.state

    private val _operation = MutableStateFlow(SideloadOperation())
    val operation: StateFlow<SideloadOperation> = _operation.asStateFlow()

    private var sideloadJob: Job? = null

    fun startScan() = viewModelScope.launch {
        repository.searchDevices()
    }

    fun disconnect() = viewModelScope.launch {
        repository.disconnect()
    }

    fun sideload(uri: Uri) {
        sideloadJob?.cancel()
        sideloadJob = viewModelScope.launch {
            repository.sideload(uri) { progress ->
                _operation.value = progress
            }.collect { finalState ->
                _operation.value = finalState
            }
        }
    }

    fun cancelSideload() {
        repository.cancelSideload()
        sideloadJob?.cancel()
        sideloadJob = null
        _operation.value = _operation.value.copy(status = SideloadStatus.CANCELLED)
    }

    fun resetOperation() {
        sideloadJob?.cancel()
        sideloadJob = null
        _operation.value = SideloadOperation()
    }

    override fun onCleared() {
        repository.unRegister()
        super.onCleared()
    }
}
