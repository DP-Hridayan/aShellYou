package `in`.hridayan.ashell.commandexamples.presentation.viewmodel

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import `in`.hridayan.ashell.commandexamples.presentation.model.CmdExamplesScreenState
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CommandExamplesViewModel @Inject constructor(
    private val commandRepository: CommandRepository,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _states = MutableStateFlow(CmdExamplesScreenState())
    val states: StateFlow<CmdExamplesScreenState> = _states

    /**
     * @param loadProgress This loading progress shows the progress when loading the list of [preloadedCommands]
     */
    private val _loadProgress = MutableStateFlow(0f)
    val loadProgress: StateFlow<Float> = _loadProgress

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    init {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }

    val allCommands: Flow<List<CommandEntity>> =
        commandRepository.getCommandsAlphabetically().stateIn(
            viewModelScope,
            SharingStarted.Companion.Lazily, emptyList()
        )

    @OptIn(FlowPreview::class)
    val filteredCommands: StateFlow<List<CommandEntity>> =
        _states
            .map { it.search.textFieldValue.text }
            .combine(allCommands) { query, commands ->
                if (query.isBlank()) {
                    commands
                } else {
                    withContext(Dispatchers.Default) {
                        commands.filter {
                            it.command.contains(query, ignoreCase = true) ||
                                    it.description.contains(query, ignoreCase = true) ||
                                    it.labels.any { label ->
                                        label.contains(query, ignoreCase = true)
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


    fun onSearchQueryChange(newValue: TextFieldValue) = with(_states.value) {
        _states.value = this.copy(
            search = search.copy(textFieldValue = newValue)
        )
    }

    fun onCommandFieldTextChange(newValue: TextFieldValue) = with(_states.value) {
        _states.value = this.copy(
            commandField = commandField.copy(
                fieldValue = newValue,
                isError = false
            )
        )
    }

    fun onDescriptionFieldTextChange(newValue: TextFieldValue) = with(_states.value) {
        _states.value = this.copy(
            descriptionField = descriptionField.copy(
                fieldValue = newValue,
                isError = false
            )
        )
    }

    fun onLabelFieldTextChange(newValue: TextFieldValue) = with(_states.value) {
        _states.value = this.copy(
            labelField = labelField.copy(
                fieldValue = newValue,
                isError = false
            )
        )
    }

    fun onLabelAdd(label: String) = with(_states.value) {
        val trimmedLabel = label.trim()

        if (trimmedLabel.isEmpty()) {
            _states.value = this.copy(
                labelField = labelField.copy(
                    isError = true,
                    errorMessage = appContext.getString(R.string.field_cannot_be_blank)
                )
            )
            return@with
        }

        var labels = _states.value.labelField.labels

        if (labels.size >= 3) {
            _states.value = this.copy(
                labelField = labelField.copy(
                    isError = true,
                    errorMessage = appContext.getString(R.string.error_labels_limit_reached)
                )
            )
            return@with
        }

        if (trimmedLabel !in labels) {
            labels = labels + trimmedLabel
        }

        _states.value = this.copy(
            labelField = labelField.copy(
                fieldValue = TextFieldValue(""),
                labels = labels
            )
        )
    }

    fun onLabelRemove(label: String) = with(_states.value) {
        var labels = this.labelField.labels

        labels = labels.filterNot { it == label }

        _states.value = this.copy(
            labelField = labelField.copy(
                labels = labels
            )
        )
    }

    fun getCommandCount(): Int {
        var count = 0
        viewModelScope.launch(Dispatchers.IO) {
            count = commandRepository.getCommandCount()
        }
        return count
    }

    suspend fun getCommandById(id: Int): String? {
        return commandRepository.getCommandById(id)?.command
    }

    fun addCommand(onSuccess: () -> Unit) = with(_states.value) {
        val command = this.commandField.fieldValue.text.trim()
        val description = this.descriptionField.fieldValue.text.trim()

        val isCommandFieldBlank = command.isBlank()
        val isDescriptionFieldBlank = description.isBlank()

        if (isCommandFieldBlank || isDescriptionFieldBlank) {
            _states.value = this.copy(
                commandField = commandField.copy(
                    isError = isCommandFieldBlank,
                    errorMessage = appContext.getString(R.string.field_cannot_be_blank)
                ),
                descriptionField = descriptionField.copy(
                    isError = isDescriptionFieldBlank,
                    errorMessage = appContext.getString(R.string.field_cannot_be_blank)
                )
            )

            return@with
        }

        viewModelScope.launch {
            commandRepository.insertCommand(
                CommandEntity(
                    command = command,
                    description = description,
                    labels = this@with.labelField.labels
                )
            )
            clearInputFields()
            onSuccess()
        }
    }

    fun deleteCommand(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            commandRepository.deleteCommand(id)
            onSuccess()
        }
    }

    suspend fun setFieldsForEdit(id: Int) = with(_states.value) {
        val commandById = commandRepository.getCommandById(id)

        _states.value = this.copy(
            commandField = commandField.copy(
                fieldValue = TextFieldValue(commandById?.command ?: "")
            ),
            descriptionField = descriptionField.copy(
                fieldValue = TextFieldValue(commandById?.description ?: "")
            ),
            labelField = labelField.copy(
                fieldValue = TextFieldValue(""),
                labels = commandById?.labels ?: emptyList()
            )
        )
    }

    fun clearInputFields() = with(_states.value) {
        _states.value = this.copy(
            commandField = commandField.copy(fieldValue = TextFieldValue("")),
            descriptionField = descriptionField.copy(fieldValue = TextFieldValue("")),
            labelField = labelField.copy(fieldValue = TextFieldValue(""), labels = emptyList())
        )
    }

    fun editCommand(id: Int, onSuccess: () -> Unit) = with(_states.value) {
        val command = this.commandField.fieldValue.text.trim()
        val description = this.descriptionField.fieldValue.text.trim()

        val isCommandFieldBlank = command.isBlank()
        val isDescriptionFieldBlank = description.isBlank()

        if (isCommandFieldBlank || isDescriptionFieldBlank) {
            _states.value = this.copy(
                commandField = commandField.copy(
                    isError = isCommandFieldBlank,
                    errorMessage = appContext.getString(R.string.field_cannot_be_blank)
                ),
                descriptionField = descriptionField.copy(
                    isError = isDescriptionFieldBlank,
                    errorMessage = appContext.getString(R.string.field_cannot_be_blank)
                )
            )
            return@with
        }

        viewModelScope.launch {
            commandRepository.updateCommand(
                CommandEntity(
                    id = id,
                    command = command,
                    description = description,
                    labels = this@with.labelField.labels
                )
            )
            clearInputFields()
            onSuccess()
        }
    }

    fun toggleFavourite(id: Int, isFavourite: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            commandRepository.updateFavoriteStatus(id, isFavourite)
            onSuccess()
        }
    }

    fun loadDefaultCommands() {
        viewModelScope.launch {
            _isLoading.value = true
            commandRepository.loadDefaultCommandsWithProgress().collect { progress ->
                _loadProgress.value = progress
            }
            _isLoading.value = false
        }
    }
}