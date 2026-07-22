@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.viewmodel


import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.domain.model.FastbootState
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootCommandResult
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootConnection
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootDeviceInfo
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode
import `in`.hridayan.ashell.shell.fastboot.domain.repository.FastbootRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class FastbootViewModel @Inject constructor(
    private val repository: FastbootRepository
) : ViewModel() {

    val state: StateFlow<FastbootState> = FastbootConnection.state

    private val _deviceInfo = MutableStateFlow<FastbootDeviceInfo?>(null)
    val deviceInfo: StateFlow<FastbootDeviceInfo?> = _deviceInfo.asStateFlow()

    private val _variables = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val variables: StateFlow<List<Pair<String, String>>> = _variables.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<FastbootCommandResult>>(emptyList())
    val commandHistory: StateFlow<List<FastbootCommandResult>> = _commandHistory.asStateFlow()

    private val _isLoadingDeviceInfo = MutableStateFlow(false)
    val isLoadingDeviceInfo: StateFlow<Boolean> = _isLoadingDeviceInfo.asStateFlow()

    private val _isLoadingVariables = MutableStateFlow(false)
    val isLoadingVariables: StateFlow<Boolean> = _isLoadingVariables.asStateFlow()

    private val _flashOperation = MutableStateFlow(FlashOperation())
    val flashOperation: StateFlow<FlashOperation> = _flashOperation.asStateFlow()

    private var flashJob: kotlinx.coroutines.Job? = null

    fun startScan() = viewModelScope.launch {
        repository.searchDevices()
    }

    fun disconnect() = viewModelScope.launch {
        repository.disconnect()
        _deviceInfo.value = null
        _variables.value = emptyList()
    }

    fun unRegister() = viewModelScope.launch {
        repository.unRegister()
    }

    fun loadDeviceInfo() = viewModelScope.launch {
        _isLoadingDeviceInfo.value = true
        repository.getDeviceInfo().collect { info ->
            _deviceInfo.value = info
            _isLoadingDeviceInfo.value = false
        }
    }

    fun loadAllVariables() = viewModelScope.launch {
        _isLoadingVariables.value = true
        repository.getAllVariables().collect { vars ->
            _variables.value = vars
            _isLoadingVariables.value = false
        }
    }

    fun sendCommand(command: String) = viewModelScope.launch {
        repository.sendCommand(command).collect { result ->
            _commandHistory.value += result
        }
    }

    fun reboot(mode: RebootMode) {
        repository.reboot(mode)
    }

    fun clearHistory() {
        _commandHistory.value = emptyList()
    }

    fun flashPartition(partition: String, imageUri: Uri) {
        flashJob?.cancel()
        flashJob = viewModelScope.launch {
            repository.flashPartition(partition, imageUri) { operation ->
                _flashOperation.value = operation
            }.collect { result ->
                _commandHistory.value += result
            }
        }
    }

    fun erasePartition(partition: String) {
        flashJob?.cancel()
        flashJob = viewModelScope.launch {
            repository.erasePartition(partition) { operation ->
                _flashOperation.value = operation
            }.collect { result ->
                _commandHistory.value += result
            }
        }
    }

    fun bootImage(imageUri: Uri) {
        flashJob?.cancel()
        flashJob = viewModelScope.launch {
            repository.bootImage(imageUri) { operation ->
                _flashOperation.value = operation
            }.collect { result ->
                _commandHistory.value += result
            }
        }
    }

    fun cancelFlashOperation() {
        flashJob?.cancel()
        flashJob = null
        _flashOperation.value = FlashOperation(
            status = FlashStatus.ERROR,
            message = "Operation cancelled"
        )
    }

    fun resetFlashOperation() {
        flashJob?.cancel()
        flashJob = null
        _flashOperation.value = FlashOperation()
    }

    override fun onCleared() {
        unRegister()
        super.onCleared()
    }
}
