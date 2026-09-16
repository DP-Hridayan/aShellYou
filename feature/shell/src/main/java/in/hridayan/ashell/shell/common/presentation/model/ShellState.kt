package `in`.hridayan.ashell.shell.common.presentation.model

sealed class ShellState {
    object Free : ShellState()
    object Busy : ShellState()
    data class InputQuery(val input: String) : ShellState()

    companion object {
        /**
         * The state the screen should hold when no command is running, derived from the input field.
         *
         * [Busy] is never produced here, so callers must keep a running command's state themselves
         * rather than recomputing it from the field text.
         */
        fun forInput(text: String): ShellState =
            if (text.isBlank()) Free else InputQuery(text)
    }
}
