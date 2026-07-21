package `in`.hridayan.ashell.shell.common.domain.model

/**
 * Represents a suggestion offered to the user while typing in the shell.
 *
 * @property id A unique identifier for the suggestion.
 * @property text The actual text that will be inserted or completed in the input.
 * @property type The category of this suggestion. See [SuggestionType]
 * @property label An optional label providing additional context. See [SuggestionLabel]
 */
data class Suggestion(
    val id: String,
    val text: String,
    val type: SuggestionType,
    val label: SuggestionLabel? = null
)
