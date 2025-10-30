package `in`.hridayan.ashell.shell.presentation.viewmodel

import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.repository.ShellRepository
import `in`.hridayan.ashell.shell.domain.usecase.ExtractLastCommandOutputUseCase
import `in`.hridayan.ashell.shell.domain.usecase.GetSaveOutputFileNameUseCase
import `in`.hridayan.ashell.shell.presentation.model.CommandResult
import `in`.hridayan.ashell.shell.presentation.model.ShellScreenState
import `in`.hridayan.ashell.shell.presentation.model.ShellState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor(
    private val shellRepository: ShellRepository,
    private val commandExamplesRepository: CommandRepository,
    private val extractLastCommandOutputUseCase: ExtractLastCommandOutputUseCase,
    private val getSaveOutputFileNameUseCase: GetSaveOutputFileNameUseCase,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _states = MutableStateFlow(ShellScreenState())
    val states: StateFlow<ShellScreenState> = _states

    val shizukuPermissionState: StateFlow<Boolean> = shellRepository.shizukuPermissionState()

    val allCommands: Flow<List<CommandEntity>> =
        commandExamplesRepository.getSortedCommands(SortType.AZ).stateIn(
            viewModelScope,
            SharingStarted.Companion.Lazily, emptyList()
        )

    @OptIn(FlowPreview::class)
    val commandSuggestions: StateFlow<List<CommandEntity>> =
        _states
            .map { it.commandField.fieldValue.text }
            .combine(allCommands) { query, commands ->
                if (query.isBlank()) {
                    emptyList()
                } else {
                    withContext(Dispatchers.Default) {
                        commands.filter {
                            it.command.contains(query, ignoreCase = true)
                        }
                    }
                }
            }
            .distinctUntilChanged()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    @OptIn(FlowPreview::class)
    val filteredOutput: StateFlow<List<CommandResult>> =
        _states.map { it.search.textFieldValue.text }
            .combine(_states.map { it.output }) { query, results ->
                if (query.isBlank()) {
                    results
                } else {
                    withContext(Dispatchers.Default) {
                        results.mapNotNull { commandResult ->
                            val filteredLines = commandResult.outputFlow.value.filter { line ->
                                line.text.contains(query, ignoreCase = true)
                            }

                            if (filteredLines.isNotEmpty()) {
                                commandResult.copy(
                                    outputFlow = MutableStateFlow(filteredLines)
                                )
                            } else {
                                null
                            }
                        }
                    }
                }
            }
            .distinctUntilChanged()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun updateButtonGroupHeight(newHeight: Dp) = with(_states.value) {
        _states.value = this.copy(buttonGroupHeight = newHeight)
    }

    fun toggleSearchBar() = _states.update {
        it.copy(
            search = it.search.copy(
                textFieldValue = TextFieldValue(""),
                isVisible = !it.search.isVisible
            )
        )
    }


    fun onSearchQueryChange(value: TextFieldValue) =
        _states.update { it.copy(search = it.search.copy(textFieldValue = value)) }


    fun onCommandTextFieldChange(
        newValue: TextFieldValue,
        isError: Boolean = false,
        errorMessage: String = ""
    ) =
        _states.update {
            it.copy(
                commandField = it.commandField.copy(
                    fieldValue = newValue.copy(
                        selection = TextRange(newValue.text.length)
                    ),
                    isError = isError,
                    errorMessage = errorMessage
                ),
                search = it.search.copy(
                    textFieldValue = TextFieldValue(""),
                    isVisible = false
                ),
                shellState = when {
                    newValue.text.isBlank() -> ShellState.Free
                    else -> ShellState.InputQuery(newValue.text)
                }
            )
        }

    fun clearOutput() = _states.update { it.copy(output = emptyList()) }

    fun getLastCommandOutput(rawOutput: String): String {
        return extractLastCommandOutputUseCase(rawOutput)
    }

    fun getSaveOutputFileName(saveWholeOutput: Boolean): String {
        val lastCommand = _states.value.cmdHistory.lastOrNull()
        return getSaveOutputFileNameUseCase(saveWholeOutput, lastCommand)
    }

    fun runBasicCommand() = runCommand { shellRepository.executeBasicCommand(it) }

    fun runRootCommand() = runCommand { shellRepository.executeRootCommand(it) }

    fun runShizukuCommand() = runCommand { shellRepository.executeShizukuCommand(it) }

    private fun runCommand(executor: suspend (String) -> Flow<OutputLine>) = with(_states.value) {
        if (this.shellState is ShellState.Busy) return@with

        val commandText = this.commandField.fieldValue.text.trim()
        if (commandText.isBlank()) {
            _states.update {
                it.copy(
                    commandField = it.commandField.copy(
                        isError = true,
                        errorMessage = appContext.getString(R.string.field_cannot_be_blank)
                    )
                )
            }
            return@with
        }

        val outputFlow = MutableStateFlow<List<OutputLine>>(emptyList())
        val newResult = CommandResult(commandText, outputFlow)

        _states.update {
            it.copy(
                commandField = it.commandField.copy(fieldValue = TextFieldValue("")),
                output = it.output + newResult,
                cmdHistory = if (it.cmdHistory.lastOrNull() == commandText) it.cmdHistory
                else it.cmdHistory + commandText,
                shellState = ShellState.Busy
            )
        }

        viewModelScope.launch {
            executor(commandText).collect { line ->
                outputFlow.update { it + line }
            }
            _states.update { it.copy(shellState = ShellState.Free) }
        }
    }

    fun executeSimpleCommand(cmdArray: Array<String>) {
        viewModelScope.launch {
            Runtime.getRuntime().exec(cmdArray)
        }
    }

    fun stopCommand() {
        shellRepository.stopCommand()
        _states.update { it.copy(shellState = ShellState.Free) }
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

    fun hasRootAccess(): Boolean = shellRepository.hasRootAccess()
}