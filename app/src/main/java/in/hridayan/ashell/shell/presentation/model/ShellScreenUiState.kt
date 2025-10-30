package `in`.hridayan.ashell.shell.presentation.model

import androidx.compose.ui.text.input.TextFieldValue

sealed class ShellScreenUiState() {
    data class Search(
        val textFieldValue: TextFieldValue = TextFieldValue(""),
        val isVisible: Boolean = false
    )
}