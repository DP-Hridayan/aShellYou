package `in`.hridayan.ashell.shell.presentation.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ShellScreenState(
    val commandField: ShellScreenInputFieldState.CommandInputFieldState = ShellScreenInputFieldState.CommandInputFieldState(),
    val output: List<CommandResult> = emptyList(),
    val shellState: ShellState = ShellState.Free,
    val cmdHistory: List<String> = emptyList(),
    val buttonGroupHeight: Dp = 0.dp,
    val search: ShellScreenUiState.Search = ShellScreenUiState.Search()
)
