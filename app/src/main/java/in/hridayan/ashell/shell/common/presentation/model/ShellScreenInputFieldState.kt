package `in`.hridayan.ashell.shell.common.presentation.model

import androidx.compose.ui.text.input.TextFieldValue

sealed class ShellScreenInputFieldState() {
    data class CommandInputFieldState(
        val fieldValue: TextFieldValue = TextFieldValue(""),
        val isError: Boolean = false,
        val errorMessage: String = ""
    ) : ShellScreenInputFieldState()
}