package `in`.hridayan.ashell.commandexamples.presentation.model

import androidx.compose.ui.text.input.TextFieldValue

sealed class CmdScreenInputFieldState {
    data class CommandInputFieldState(
        val fieldValue: TextFieldValue = TextFieldValue(""),
        val isError: Boolean = false,
        val errorMessage: String = ""
    ) : CmdScreenInputFieldState()

    data class DescriptionInputFieldState(
        val fieldValue: TextFieldValue = TextFieldValue(""),
        val isError: Boolean = false,
        val errorMessage: String = ""
    ) : CmdScreenInputFieldState()

    data class LabelInputFieldState(
        val fieldValue: TextFieldValue = TextFieldValue(""),
        val labels: List<String> = emptyList(),
        val isError: Boolean = false,
        val errorMessage: String = ""
    ) : CmdScreenInputFieldState()
}
