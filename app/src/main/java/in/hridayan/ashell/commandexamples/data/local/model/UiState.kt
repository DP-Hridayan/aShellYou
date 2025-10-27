package `in`.hridayan.ashell.commandexamples.data.local.model

sealed class UiState {
    data class Search(val query: String = "")
}