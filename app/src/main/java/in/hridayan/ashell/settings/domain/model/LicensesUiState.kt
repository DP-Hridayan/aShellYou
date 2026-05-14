package `in`.hridayan.ashell.settings.domain.model

data class LicensesUiState(
    val isLoading: Boolean = true,
    val libraries: List<LibraryItem> = emptyList(),
    /** Subset of [libraries] shown after applying [searchQuery]. */
    val filteredLibraries: List<LibraryItem> = emptyList(),
    val searchQuery: String = "",
    /** The library whose full-text dialog/sheet is currently open, or null if none. */
    val selectedLibrary: LibraryItem? = null,
    val error: String? = null,
)