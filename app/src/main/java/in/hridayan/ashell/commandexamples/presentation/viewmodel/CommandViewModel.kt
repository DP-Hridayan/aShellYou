package `in`.hridayan.ashell.commandexamples.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommandViewModel @Inject constructor(
    private val commandRepository: CommandRepository
) : ViewModel() {
    private val _command = MutableStateFlow("")
    val command: StateFlow<String> = _command

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _labels = MutableStateFlow<List<String>>(emptyList())
    val labels: StateFlow<List<String>> = _labels

    private val _commandError = MutableStateFlow(false)
    val commandError: StateFlow<Boolean> = _commandError

    private val _descriptionError = MutableStateFlow(false)
    val descriptionError: StateFlow<Boolean> = _descriptionError

    private val _labelError = MutableStateFlow(false)
    val labelError: StateFlow<Boolean> = _labelError

    init {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }

    val allCommands: Flow<List<CommandEntity>> =
        commandRepository.getCommandsAlphabetically().stateIn(
            viewModelScope,
            SharingStarted.Companion.Lazily, emptyList()
        )

    fun onCommandChange(newValue: String) {
        _command.value = newValue
        _commandError.value = false
    }

    fun onDescriptionChange(newValue: String) {
        _description.value = newValue
        _descriptionError.value = false
    }

    fun onLabelAdd(label: String) {
        val trimmedLabel = label.trim()
        if (trimmedLabel.isEmpty()) {
            _labelError.value = true
            return
        }

        if (trimmedLabel !in _labels.value) {
            _labels.value = _labels.value + trimmedLabel
        }
    }

    fun onLabelRemove(label: String) {
        _labels.value = _labels.value.filterNot { it == label }
    }

    fun clearLabelError() {
        _labelError.value = false
    }

    fun getCommandCount(): Int {
        var count = 0
        viewModelScope.launch(Dispatchers.IO) {
            count = commandRepository.getCommandCount()
        }
        return count
    }

    fun addCommand(onSuccess: () -> Unit) {
        val isCommandValid = _command.value.trim().isNotBlank()
        val isDescriptionValid = _description.value.trim().isNotBlank()

        _commandError.value = !isCommandValid
        _descriptionError.value = !isDescriptionValid

        if (isCommandValid && isDescriptionValid) {
            viewModelScope.launch {
                commandRepository.insertCommand(
                    CommandEntity(
                        command = _command.value.trim(),
                        description = _description.value.trim(),
                        labels = _labels.value
                    )
                )
                _command.value = ""
                _description.value = ""
                _labels.value = emptyList()
                onSuccess()
            }
        }
    }

    fun deleteCommand(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            commandRepository.deleteCommand(id)
            onSuccess()
        }
    }

    suspend fun setFieldsForEdit(id: Int) {
        val commandById = commandRepository.getCommandById(id)
        _command.value = commandById?.command ?: ""
        _description.value = commandById?.description ?: ""
        _labels.value = commandById?.labels ?: emptyList()
    }

    fun clearInputFields() {
        _command.value = ""
        _description.value = ""
        _labels.value = emptyList()
    }

    fun editCommand(id: Int, onSuccess: () -> Unit) {
        val isCommandValid = _command.value.trim().isNotBlank()
        val isDescriptionValid = _description.value.trim().isNotBlank()

        _commandError.value = !isCommandValid
        _descriptionError.value = !isDescriptionValid

        if (isCommandValid && isDescriptionValid) {
            viewModelScope.launch {
                commandRepository.updateCommand(
                    CommandEntity(
                        id = id,
                        command = _command.value.trim(),
                        description = _description.value.trim(),
                        labels = _labels.value
                    )
                )
                _command.value = ""
                _description.value = ""
                _labels.value = emptyList()
                onSuccess()
            }
        }
    }

    fun toggleFavourite(id: Int, isFavourite: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            commandRepository.updateFavoriteStatus(id, isFavourite)
            onSuccess()
        }
    }
}