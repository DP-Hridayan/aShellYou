package `in`.hridayan.ashell.shell.common.presentation.viewmodel

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
import `in`.hridayan.ashell.shell.common.data.permission.PermissionProvider
import `in`.hridayan.ashell.shell.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo
import `in`.hridayan.ashell.shell.common.domain.model.Suggestion
import `in`.hridayan.ashell.shell.common.domain.model.SuggestionLabel
import `in`.hridayan.ashell.shell.common.domain.model.SuggestionType
import `in`.hridayan.ashell.shell.common.domain.repository.PackageRepository
import `in`.hridayan.ashell.shell.common.domain.repository.ShellRepository
import `in`.hridayan.ashell.shell.common.domain.usecase.DetectSuggestionTypeUseCase
import `in`.hridayan.ashell.shell.common.domain.usecase.ExtractLastCommandOutputUseCase
import `in`.hridayan.ashell.shell.common.domain.usecase.GetSaveOutputFileNameUseCase
import `in`.hridayan.ashell.shell.common.presentation.model.CommandResult
import `in`.hridayan.ashell.shell.common.presentation.model.ShellScreenState
import `in`.hridayan.ashell.shell.common.presentation.model.ShellState
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
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
    private val packageRepository: PackageRepository,
    private val detectSuggestionTypeUseCase: DetectSuggestionTypeUseCase,
    private val extractLastCommandOutputUseCase: ExtractLastCommandOutputUseCase,
    private val getSaveOutputFileNameUseCase: GetSaveOutputFileNameUseCase,
    private val otgRepository: OtgRepository,
    private val wifiAdbRepository: WifiAdbRepository,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _states = MutableStateFlow(ShellScreenState())
    val states: StateFlow<ShellScreenState> = _states

    val shizukuPermissionState: StateFlow<Boolean> = shellRepository.shizukuPermissionState()

    private val allCommands: Flow<List<CommandEntity>> =
        commandExamplesRepository.getSortedCommands(SortType.AZ).stateIn(
            viewModelScope,
            SharingStarted.Lazily, emptyList()
        )

    private val _packages = MutableStateFlow<List<PackageInfo>>(emptyList())

    init {
        viewModelScope.launch {
            _packages.value = packageRepository.getInstalledPackages()
        }
    }

    @OptIn(FlowPreview::class)
    val suggestions: StateFlow<List<Suggestion>> =
        _states
            .map { it.commandField.fieldValue.text }
            .combine(allCommands) { text, commands ->
                val context = detectSuggestionTypeUseCase(text)

                withContext(Dispatchers.Default) {
                    when (context.suggestionType) {
                        SuggestionType.COMMAND -> {
                            if (context.filterPrefix.isBlank()) {
                                emptyList()
                            } else {
                                commands.filter {
                                    it.command.contains(context.filterPrefix, ignoreCase = true)
                                }.map { cmd ->
                                    Suggestion(
                                        id = cmd.id.toString(),
                                        text = cmd.command,
                                        type = SuggestionType.COMMAND
                                    )
                                }
                            }
                        }

                        SuggestionType.PACKAGE -> {
                            _packages.value.filter {
                                it.packageName.startsWith(context.filterPrefix, ignoreCase = true)
                            }.take(50).map { pkg ->
                                Suggestion(
                                    id = pkg.packageName,
                                    text = pkg.packageName,
                                    type = SuggestionType.PACKAGE,
                                    label = if (pkg.isSystemApp) SuggestionLabel.SYSTEM else SuggestionLabel.USER
                                )
                            }
                        }

                        SuggestionType.PERMISSION -> {
                            PermissionProvider.adbPermissions.filter {
                                it.startsWith(context.filterPrefix, ignoreCase = true)
                            }.take(50).map { perm ->
                                Suggestion(
                                    id = perm,
                                    text = perm,
                                    type = SuggestionType.PERMISSION
                                )
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
                    fieldValue = newValue.copy(),
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

    fun updateTextFieldSelection() = _states.update {
        it.copy(
            commandField = it.commandField.copy(
                fieldValue = it.commandField.fieldValue.copy(
                    selection = TextRange(it.commandField.fieldValue.text.length)
                )
            )
        )
    }

    fun applySuggestion(suggestion: Suggestion) {
        when (suggestion.type) {
            SuggestionType.COMMAND -> {
                // For commands: replace entire input with sanitized command (remove placeholders)
                val sanitizedCommand = suggestion.text
                    .replace(Regex("<[^>]+>"), "")  // Remove <package>, <permission>, etc.
                    .replace(Regex("\\s+"), " ")    // Collapse multiple spaces
                    .trim()
                onCommandTextFieldChange(TextFieldValue(sanitizedCommand))
            }

            SuggestionType.PACKAGE, SuggestionType.PERMISSION -> {
                // For packages/permissions: replace only the last token
                val currentText = _states.value.commandField.fieldValue.text
                val lastSpaceIndex = currentText.lastIndexOf(' ')
                val newText = if (lastSpaceIndex >= 0) {
                    currentText.substring(0, lastSpaceIndex + 1) + suggestion.text
                } else {
                    suggestion.text
                }
                onCommandTextFieldChange(TextFieldValue(newText))
            }
        }
        updateTextFieldSelection()
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

    fun runOtgCommand() = runCommand { otgRepository.runOtgCommand(it) }

    fun runWifiAdbCommand() = runCommand { wifiAdbRepository.execute(it) }

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
                cmdHistory = filterDuplicateHistoryEntries(commandText, it.cmdHistory),
                shellState = ShellState.Busy
            )
        }

        viewModelScope.launch {
            val outputBuffer = mutableListOf<OutputLine>()
            var lastFlushTime = System.currentTimeMillis()
            val flushIntervalMs = 250L

            executor(commandText).collect { line ->
                outputBuffer.add(line)

                val now = System.currentTimeMillis()
                if (now - lastFlushTime >= flushIntervalMs || outputBuffer.size >= 100) {
                    val linesToAdd = outputBuffer.toList()
                    outputBuffer.clear()
                    outputFlow.update { it + linesToAdd }
                    lastFlushTime = now
                }
            }

            if (outputBuffer.isNotEmpty()) {
                outputFlow.update { it + outputBuffer }
            }

            _states.update { it.copy(shellState = ShellState.Free) }
        }
    }

    private fun filterDuplicateHistoryEntries(
        commandText: String,
        cmdHistory: List<String>
    ): List<String> {
        val filteredHistory = cmdHistory.filter { it != commandText.trim() }
        return filteredHistory + commandText
    }

    fun executeSimpleCommand(cmdArray: Array<String>) {
        viewModelScope.launch {
            Runtime.getRuntime().exec(cmdArray)
        }
    }

    fun stopCommand() {
        shellRepository.stopCommand()
        otgRepository.stopCommand()
        wifiAdbRepository.abortShell()
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