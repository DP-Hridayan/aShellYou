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

    private val _example = MutableStateFlow("")
    val example: StateFlow<String> = _example

    private val _label= MutableStateFlow("")
    val label: StateFlow<String> = _label

    private val _commandError = MutableStateFlow(false)
    val commandError: StateFlow<Boolean> = _commandError

    private val _descriptionError = MutableStateFlow(false)
    val descriptionError: StateFlow<Boolean> = _descriptionError

    private val _exampleError = MutableStateFlow(false)
    val exampleError: StateFlow<Boolean> = _exampleError

    init {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }

    val allCommands: Flow<List<CommandEntity>> = commandRepository.getCommandsAlphabetically().stateIn(
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

    fun onExampleChange(newValue: String) {
        _example.value = newValue
        _exampleError.value = false
    }

    fun onLabelChange(newValue: String) {
        _label.value = newValue
    }

    fun addCommand(onSuccess: () -> Unit) {
        val isCommandValid = _command.value.isNotBlank()
        val isDescriptionValid = _description.value.isNotBlank()
        val isExampleValid = _example.value.isNotBlank()

        _commandError.value = !isCommandValid
        _descriptionError.value = !isDescriptionValid
        _exampleError.value = !isExampleValid

        if (isCommandValid && isDescriptionValid && isExampleValid) {
            viewModelScope.launch {
                commandRepository.insertCommand(
                    CommandEntity(
                        command = _command.value,
                        description = _description.value,
                        example = _example.value,
                        labels = listOf(_label.value)
                    )
                )
                _command.value = ""
                _description.value = ""
                _example.value = ""
                _label.value = ""
                onSuccess()
            }
        }
    }
}