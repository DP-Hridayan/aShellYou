package `in`.hridayan.ashell.shell.domain.model

sealed class ShellState {
    object Free : ShellState()
    object Busy : ShellState()
    data class InputQuery(val input: String) : ShellState()
}