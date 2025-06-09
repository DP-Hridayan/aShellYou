package `in`.hridayan.ashell.shell.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.domain.model.CommandResult
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.model.ShellState
import `in`.hridayan.ashell.shell.domain.usecase.ShellCommandExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor(
    private val executor: ShellCommandExecutor
) : ViewModel() {
    private val _command = MutableStateFlow("")
    val command: StateFlow<String> = _command

    private val _commandError = MutableStateFlow(false)
    val commandError: StateFlow<Boolean> = _commandError

    private val _commandResults = MutableStateFlow<List<CommandResult>>(emptyList())
    val commandResults: StateFlow<List<CommandResult>> = _commandResults

    private val _shellState = MutableStateFlow<ShellState>(ShellState.Free)
    val shellState: StateFlow<ShellState> = _shellState

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

    fun runCommand() {
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
            executor.executeCommand(commandText)
                .collect { line ->
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
        executor.stop()
        _shellState.value = ShellState.Free
    }
}