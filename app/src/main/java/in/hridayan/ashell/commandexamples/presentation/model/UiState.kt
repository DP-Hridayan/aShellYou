package `in`.hridayan.ashell.commandexamples.presentation.model

import androidx.compose.ui.text.input.TextFieldValue

/**
 * @param Search Holds the states for SearchBar
 */
sealed class UiState {
    data class Search(val textFieldValue: TextFieldValue = TextFieldValue(""))
}