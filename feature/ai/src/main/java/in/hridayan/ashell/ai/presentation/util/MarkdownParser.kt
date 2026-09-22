package `in`.hridayan.ashell.ai.presentation.util

import `in`.hridayan.ashell.ai.presentation.model.MessageComponent

private val CODE_BLOCK_REGEX = Regex("```(\\w*)\\n(.*?)```", RegexOption.DOT_MATCHES_ALL)

/**
 * Splits a model reply into prose and fenced code blocks.
 *
 * The split is what lets the chat render a "Use" action beside a suggested command instead of
 * leaving the user to select and copy it.
 */
internal object MarkdownParser {

    fun parse(content: String): List<MessageComponent> {
        val components = mutableListOf<MessageComponent>()
        var lastIndex = 0

        CODE_BLOCK_REGEX.findAll(content).forEach { match ->
            val textBefore = content.substring(lastIndex, match.range.first)
            if (textBefore.isNotBlank()) {
                components.add(MessageComponent.Text(textBefore.trim()))
            }
            components.add(
                MessageComponent.CodeBlock(match.groupValues[1], match.groupValues[2].trim())
            )
            lastIndex = match.range.last + 1
        }

        val textAfter = content.substring(lastIndex)
        if (textAfter.isNotBlank()) {
            components.add(MessageComponent.Text(textAfter.trim()))
        }

        return components
    }
}
