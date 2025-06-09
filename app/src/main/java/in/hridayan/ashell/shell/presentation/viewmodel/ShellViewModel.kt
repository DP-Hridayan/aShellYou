package `in`.hridayan.ashell.shell.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.domain.model.CommandResult
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.model.ShellState
import `in`.hridayan.ashell.shell.domain.repository.ShellRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor(
    private val shellRepository: ShellRepository,
) : ViewModel() {
    private val _command = MutableStateFlow("")
    val command: StateFlow<String> = _command

    private val _commandError = MutableStateFlow(false)
    val commandError: StateFlow<Boolean> = _commandError

    private val _commandResults = MutableStateFlow<List<CommandResult>>(emptyList())
    val commandResults: StateFlow<List<CommandResult>> = _commandResults

    private val _shellState = MutableStateFlow<ShellState>(ShellState.Free)
    val shellState: StateFlow<ShellState> = _shellState

    val shizukuPermissionState: StateFlow<Boolean> = shellRepository.shizukuPermissionState()

    fun onCommandChange(newValue: String) {
        _command.value = newValue
        _commandError.value = false

        _shellState.value = when {
            newValue.isBlank() -> ShellState.Free
            else -> ShellState.InputQuery(newValue)
        }
    }

    fun clearOutput() {
        _commandResults.value = emptyList()
    }

    fun runBasicCommand() = runCommand { shellRepository.executeBasicCommand(it) }

    fun runRootCommand() = runCommand { shellRepository.executeRootCommand(it) }

    fun runShizukuCommand() = runCommand { shellRepository.executeShizukuCommand(it) }

    private fun runCommand(executor: suspend (String) -> Flow<OutputLine>) {
        if (_shellState.value is ShellState.Busy) return

        val commandText = _command.value.trim()
        if (commandText.isBlank()) {
            _commandError.value = true
            return
        }

        val outputFlow = MutableStateFlow<List<OutputLine>>(emptyList())
        val newResult = CommandResult(commandText, outputFlow)

        _commandResults.update { it + newResult }
        _command.value = ""
        _shellState.value = ShellState.Busy

        viewModelScope.launch {
            executor(commandText).collect { line ->
                outputFlow.update { it + line }
            }
            _shellState.value = ShellState.Free
        }
    }

    fun executeSimpleCommand(command: String) {
        viewModelScope.launch {
            Runtime.getRuntime().exec(command)
        }
    }

    fun stopCommand() {
        shellRepository.stopCommand()
        _shellState.value = ShellState.Free
    }

    fun requestShizukuPermission() {
        shellRepository.refreshShizukuPermission()
    }

    fun refreshShizukuPermission() {
        shellRepository.refreshShizukuPermission()
    }

    fun isShizukuInstalled(): Boolean {
        return shellRepository.isShizukuInstalled()
    }

    fun hasShizukuPermission(): Boolean {
        return shellRepository.hasShizukuPermission()
    }
}