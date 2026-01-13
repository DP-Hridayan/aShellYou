package `in`.hridayan.ashell.shell.common.domain.usecase

import `in`.hridayan.ashell.shell.common.domain.model.InputContext
import `in`.hridayan.ashell.shell.common.domain.model.SuggestionType
import javax.inject.Inject

class DetectSuggestionTypeUseCase @Inject constructor() {

    operator fun invoke(text: String): InputContext {
        if (text.isBlank()) {
            return InputContext(
                fullText = text,
                currentToken = "",
                suggestionType = SuggestionType.COMMAND,
                filterPrefix = ""
            )
        }

        // Get the last token (word after last space)
        val tokens = text.split(" ")
        val currentToken = tokens.lastOrNull() ?: ""

        // If token is empty or ends with space, show commands
        if (currentToken.isBlank() || text.endsWith(" ")) {
            return InputContext(
                fullText = text,
                currentToken = "",
                suggestionType = SuggestionType.COMMAND,
                filterPrefix = text.trim()
            )
        }

        // Check if token contains a dot
        if (!currentToken.contains(".")) {
            // No dot yet, show command suggestions
            return InputContext(
                fullText = text,
                currentToken = currentToken,
                suggestionType = SuggestionType.COMMAND,
                filterPrefix = currentToken
            )
        }

        // Token has at least one dot - determine type based on first segment
        val firstDotIndex = currentToken.indexOf(".")
        val firstSegment = currentToken.take(firstDotIndex)

        val suggestionType = when {
            firstSegment.equals("android", ignoreCase = true) -> SuggestionType.PERMISSION
            else -> SuggestionType.PACKAGE
        }

        return InputContext(
            fullText = text,
            currentToken = currentToken,
            suggestionType = suggestionType,
            filterPrefix = currentToken
        )
    }
}
