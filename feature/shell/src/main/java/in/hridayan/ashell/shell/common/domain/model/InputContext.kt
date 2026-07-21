package `in`.hridayan.ashell.shell.common.domain.model

/**
 * Represents the context of the user's input in the shell.
 * This is used to determine what kind of suggestions should be provided.
 *
 * @property fullText The entire text currently entered in the input field.
 * @property currentToken Anything from a letter to words in the input field.
 * @property suggestionType The category of suggestions applicable to the current context. See [SuggestionType]
 * @property filterPrefix The prefix used to filter the list of available suggestions.
 */
data class InputContext(
    val fullText: String,
    val currentToken: String,
    val suggestionType: SuggestionType,
    val filterPrefix: String
)
