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

  /*  fun runCommand() {
        if (_shellState.value is ShellState.Busy) return

        val commandText = _command.value.trim()
        val outputFlow = MutableStateFlow<List<OutputLine>>(emptyList())
        val newResult = CommandResult(commandText, outputFlow)

        _commandResults.update { it + newResult }
        _command.value = ""
        _shellState.value = ShellState.Busy

        CoroutineScope(Dispatchers.IO).launch {
            executor.executeCommand(commandText, outputFlow)

            withContext(Dispatchers.Main) {
                _shellState.value = ShellState.Free
            }
        }
    }
*/

    private val _outputLines = MutableStateFlow<List<OutputLine>>(emptyList())
    val outputLines: StateFlow<List<OutputLine>> = _outputLines

    fun runCommand() {
        viewModelScope.launch {
            executeCommand(_command.value.trim()).collect { line ->
                _outputLines.update { it + line }
            }
        }
    }

    fun executeCommand(command: String): Flow<OutputLine> = flow {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

        while (true) {
            val line = reader.readLine() ?: break
            emit(OutputLine(line, isError = false))
        }

        while (true) {
            val errorLine = errorReader.readLine() ?: break
            emit(OutputLine(errorLine, isError = true))
        }

        process.waitFor()
    }.flowOn(Dispatchers.IO)


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