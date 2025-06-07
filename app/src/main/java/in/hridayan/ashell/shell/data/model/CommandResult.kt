package `in`.hridayan.ashell.shell.data.model

import kotlinx.coroutines.flow.MutableStateFlow

data class CommandResult(
    val command: String,
    val outputFlow: MutableStateFlow<String> = MutableStateFlow("")
)