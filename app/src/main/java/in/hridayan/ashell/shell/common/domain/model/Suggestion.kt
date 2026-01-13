package `in`.hridayan.ashell.shell.common.domain.model

data class Suggestion(
    val id: String,
    val text: String,
    val type: SuggestionType,
    val label: SuggestionLabel? = null  // "System" or "User" for packages
)
