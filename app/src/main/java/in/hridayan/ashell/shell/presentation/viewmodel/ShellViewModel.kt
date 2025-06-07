package `in`.hridayan.ashell.shell.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.data.model.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor() : ViewModel() {

    private val _commandResults = MutableStateFlow<List<CommandResult>>(emptyList())
    val commandResults: StateFlow<List<CommandResult>> = _commandResults

    fun runCommand(command: String) {
        val outputFlow = MutableStateFlow("")
        val newResult = CommandResult(command, outputFlow)

        _commandResults.update { oldList -> oldList + newResult }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val process = Runtime.getRuntime().exec(command)
                    val reader = process.inputStream.bufferedReader()
                    val errReader = process.errorStream.bufferedReader()

                    val stdJob = launch {
                        reader.forEachLine { line ->
                            outputFlow.update { it + "\n$line" }
                        }
                    }

                    val errJob = launch {
                        errReader.forEachLine { line ->
                            outputFlow.update { it + "\n$line" }
                        }
                    }

                    stdJob.join()
                    errJob.join()
                    process.waitFor()
                } catch (e: Exception) {
                    outputFlow.update { it + "\nError: ${e.message}" }
                }
            }
        }
    }
}
