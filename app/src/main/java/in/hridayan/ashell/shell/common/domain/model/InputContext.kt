package `in`.hridayan.ashell.shell.common.domain.model

data class InputContext(
    val fullText: String,
    val currentToken: String,
    val suggestionType: SuggestionType,
    val filterPrefix: String  // The full prefix to filter by (e.g., "com.google.")
)
