package `in`.hridayan.ashell.shell.domain.model

import kotlinx.coroutines.flow.MutableStateFlow

data class CommandResult(
    val command: String,
    val outputFlow: MutableStateFlow<List<OutputLine>>
)