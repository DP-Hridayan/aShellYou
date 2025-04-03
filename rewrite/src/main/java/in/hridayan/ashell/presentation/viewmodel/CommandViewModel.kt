package `in`.hridayan.ashell.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity
import `in`.hridayan.ashell.domain.repository.command_examples.CommandRepository
import `in`.hridayan.ashell.domain.repository.command_examples.LabelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommandViewModel @Inject constructor(
    private val commandRepository: CommandRepository,
    private val labelRepository: LabelRepository
) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            commandRepository.insertCommand(
                command = CommandEntity(
                    command = "ls",
                    description = "List files in the current directory",
                    example = "ls",
                    labels = listOf("normal")
                )

            )

            commandRepository.insertCommand(
                command = CommandEntity(
                    command = "ter",
                    description = "List files in the current directory",
                    example = "ls",
                    labels = listOf("normal","file")
                )

            )

            commandRepository.insertCommand(
                command = CommandEntity(
                    command = "were",
                    description = "List files in the current directory",
                    example = "ls",
                    labels = listOf("normal","file","directory")
                )

            )
        }
    }

    fun insertCommand(command: CommandEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                commandRepository.insertCommand(command)
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    val allCommands: Flow<List<CommandEntity>> = commandRepository.getAllCommands().stateIn(
        viewModelScope,
        SharingStarted.Lazily, emptyList()
    )

    fun sortCommandsAlphabetically() {
        viewModelScope.launch (Dispatchers.IO){
            commandRepository.reorderCommandsAlphabetically()
        }
    }

    fun sortCommandsReversedAlphabetically() {
        viewModelScope.launch(Dispatchers.IO) {
            commandRepository.reorderCommandsReverseAlphabetically()
        }
    }
}