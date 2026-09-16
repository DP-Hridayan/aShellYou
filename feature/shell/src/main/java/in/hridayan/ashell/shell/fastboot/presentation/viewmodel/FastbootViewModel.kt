package `in`.hridayan.ashell.shell.fastboot.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.FastbootState
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootCommandResult
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootConnection
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootDeviceInfo
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode
import `in`.hridayan.ashell.shell.fastboot.domain.repository.FastbootRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _eraseOperation = MutableStateFlow(FlashOperation())
    val eraseOperation: StateFlow<FlashOperation> = _eraseOperation.asStateFlow()

    private val _runningCommandId = MutableStateFlow<String?>(null)
    val runningCommandId: StateFlow<String?> = _runningCommandId.asStateFlow()

    private val _commandOutput = MutableStateFlow("")
    val commandOutput: StateFlow<String> = _commandOutput.asStateFlow()

    private val _isConsoleCommandRunning = MutableStateFlow(false)
    val isConsoleCommandRunning: StateFlow<Boolean> = _isConsoleCommandRunning.asStateFlow()

    private var flashJob: Job? = null
    private var eraseJob: Job? = null
    private var commandJob: Job? = null

    fun startScan() {
        repository.searchDevices()
    }

    fun disconnect() {
        repository.disconnect()
        _deviceInfo.value = null
        _variables.value = emptyList()
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

    fun sendCommand(command: String) {
        commandJob?.cancel()
        _commandOutput.value += "\n> $command\n"
        _isConsoleCommandRunning.value = true
        commandJob = viewModelScope.launch {
            try {
                repository.sendCommand(command).collect(::recordResult)
            } finally {
                _isConsoleCommandRunning.value = false
            }
        }
    }

    fun stopConsoleCommand() {
        commandJob?.cancel()
        commandJob = null
        _isConsoleCommandRunning.value = false
    }

    /** Run a predefined command card identified by [commandId]. */
    fun runPredefinedCommand(commandId: String, command: String) {
        if (_runningCommandId.value == commandId) {
            commandJob?.cancel()
            commandJob = null
            _runningCommandId.value = null
            return
        }
        commandJob?.cancel()
        _runningCommandId.value = commandId
        _commandOutput.value += "\n> $command\n"
        commandJob = viewModelScope.launch {
            try {
                repository.sendCommand(command).collect(::recordResult)
            } finally {
                _runningCommandId.value = null
            }
        }
    }

    private fun recordResult(result: FastbootCommandResult) {
        _commandHistory.value += result
        _commandOutput.value += result.data + "\n"
    }

    fun clearOutput() {
        _commandOutput.value = ""
    }

    fun reboot(mode: RebootMode) {
        repository.reboot(mode)
    }

    fun clearHistory() {
        _commandHistory.value = emptyList()
    }

    fun flashPartition(partition: String, imageUri: Uri) {
        flashJob?.cancel()
        _flashOperation.value = FlashOperation(partition = partition, status = FlashStatus.READING_FILE)
        flashJob = viewModelScope.launch {
            repository.flashPartition(partition, imageUri) { operation ->
                _flashOperation.value = operation
            }.collect { result -> _commandHistory.value += result }
        }
    }

    fun bootImage(imageUri: Uri) {
        flashJob?.cancel()
        _flashOperation.value = FlashOperation(status = FlashStatus.READING_FILE)
        flashJob = viewModelScope.launch {
            repository.bootImage(imageUri) { operation ->
                _flashOperation.value = operation
            }.collect { result -> _commandHistory.value += result }
        }
    }

    fun erasePartition(partition: String) {
        eraseJob?.cancel()
        _eraseOperation.value = FlashOperation(partition = partition, status = FlashStatus.ERASING)
        eraseJob = viewModelScope.launch {
            repository.erasePartition(partition) { operation ->
                _eraseOperation.value = operation
            }.collect { result -> _commandHistory.value += result }
        }
    }

    fun cancelFlashOperation() {
        flashJob?.cancel()
        flashJob = null
        repository.cancelOperation()
        _flashOperation.value = _flashOperation.value.copy(status = FlashStatus.CANCELLED)
    }

    fun resetFlashOperation() {
        flashJob?.cancel()
        flashJob = null
        _flashOperation.value = FlashOperation()
    }

    fun cancelEraseOperation() {
        eraseJob?.cancel()
        eraseJob = null
        repository.cancelOperation()
        _eraseOperation.value = _eraseOperation.value.copy(status = FlashStatus.CANCELLED)
    }

    fun resetEraseOperation() {
        eraseJob?.cancel()
        eraseJob = null
        _eraseOperation.value = FlashOperation()
    }
}
