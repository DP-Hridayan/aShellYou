package `in`.hridayan.ashell.shell.presentation.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.domain.model.CommandResult
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.model.ShellState
import `in`.hridayan.ashell.shell.domain.repository.ShellRepository
import `in`.hridayan.ashell.shell.domain.usecase.ExtractLastCommandOutputUseCase
import `in`.hridayan.ashell.shell.domain.usecase.GetSaveOutputFileNameUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor(
    private val shellRepository: ShellRepository,
    private val extractLastCommandOutputUseCase: ExtractLastCommandOutputUseCase,
    private val getSaveOutputFileNameUseCase: GetSaveOutputFileNameUseCase
) : ViewModel() {
    private val _command = MutableStateFlow(TextFieldValue(text = "", selection = TextRange(0)))
    val command: StateFlow<TextFieldValue> = _command

    private val _commandError = MutableStateFlow(false)
    val commandError: StateFlow<Boolean> = _commandError

    private val _commandResults = MutableStateFlow<List<CommandResult>>(emptyList())
    val commandResults: StateFlow<List<CommandResult>> = _commandResults

    private val _shellState = MutableStateFlow<ShellState>(ShellState.Free)
    val shellState: StateFlow<ShellState> = _shellState

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history

    val shizukuPermissionState: StateFlow<Boolean> = shellRepository.shizukuPermissionState()

    private val _isSearchBarVisible = MutableStateFlow(false)
    val isSearchBarVisible: StateFlow<Boolean> = _isSearchBarVisible

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun toggleSearchBar() {
        _isSearchBarVisible.value = !_isSearchBarVisible.value
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onCommandChange(newValue: TextFieldValue) {
        val updatedValue = newValue.copy(
            selection = TextRange(newValue.text.length)
        )

        _command.value = updatedValue
        _commandError.value = false

        _shellState.value = when {
            newValue.text.isBlank() -> ShellState.Free
            else -> ShellState.InputQuery(newValue.text)
        }
    }

    fun clearOutput() {
        if (_commandResults.value.isNotEmpty()) _commandResults.value = emptyList()
    }

    fun getLastCommandOutput(rawOutput: String): String {
        return extractLastCommandOutputUseCase(rawOutput)
    }

    fun getSaveOutputFileName(saveWholeOutput: Boolean): String {
        val lastCommand = _history.value.lastOrNull()
        return getSaveOutputFileNameUseCase(saveWholeOutput, lastCommand)
    }

    fun runBasicCommand() = runCommand { shellRepository.executeBasicCommand(it) }

    fun runRootCommand() = runCommand { shellRepository.executeRootCommand(it) }

    fun runShizukuCommand() = runCommand { shellRepository.executeShizukuCommand(it) }

    private fun runCommand(executor: suspend (String) -> Flow<OutputLine>) {
        if (_shellState.value is ShellState.Busy) return

        val commandText = _command.value.text.trim()
        if (commandText.isBlank()) {
            _commandError.value = true
            return
        }

        val outputFlow = MutableStateFlow<List<OutputLine>>(emptyList())
        val newResult = CommandResult(commandText, outputFlow)

        _history.update { history ->
            if (history.lastOrNull() == commandText) history
            else history + commandText
        }
        _commandResults.update { it + newResult }
        _command.value = TextFieldValue("")
        _shellState.value = ShellState.Busy

        viewModelScope.launch {
            executor(commandText).collect { line ->
                outputFlow.update { it + line }
            }
            _shellState.value = ShellState.Free
        }
    }

    fun executeSimpleCommand(cmdArray: Array<String>) {
        viewModelScope.launch {
            Runtime.getRuntime().exec(cmdArray)
        }
    }

    fun stopCommand() {
        shellRepository.stopCommand()
        _shellState.value = ShellState.Free
    }

    fun requestShizukuPermission() {
        shellRepository.requestShizukuPermission()
    }

    fun refreshShizukuPermission() {
        shellRepository.refreshShizukuPermission()
    }

    fun hasShizukuPermission(): Boolean {
        return shellRepository.hasShizukuPermission()
    }
}