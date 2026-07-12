@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.viewmodel

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootCommandResult
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootConnection
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootDeviceInfo
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootState
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
            _commandHistory.value = _commandHistory.value + result
        }
    }

    fun reboot(mode: RebootMode) {
        repository.reboot(mode)
    }

    fun clearHistory() {
        _commandHistory.value = emptyList()
    }

    override fun onCleared() {
        unRegister()
        super.onCleared()
    }
}
