package `in`.hridayan.ashell.commandexamples.presentation.model

/**
 * @param Search Holds the states for SearchBar
 */
sealed class UiState {
    data class Search(val query: String = "")
}