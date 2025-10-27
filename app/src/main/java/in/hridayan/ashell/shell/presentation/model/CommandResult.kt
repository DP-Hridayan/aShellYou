package `in`.hridayan.ashell.shell.presentation.model

import `in`.hridayan.ashell.shell.domain.model.OutputLine
import kotlinx.coroutines.flow.MutableStateFlow

data class CommandResult(
    val command: String,
    val outputFlow: MutableStateFlow<List<OutputLine>>
)