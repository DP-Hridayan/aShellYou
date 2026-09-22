package `in`.hridayan.ashell.ai.presentation.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private const val MAX_THOUGHT_CHARS = 500
private const val MAX_THOUGHT_LINES = 10
private const val ARGS_TRUNCATED_NOTICE = "\n\n[...Args truncated for UI]"
private const val OUTPUT_TRUNCATED_NOTICE = "\n\n[...Output truncated for UI]"

/**
 * Shortens tool arguments and results for the collapsed "thoughts" view.
 *
 * A tool can return tens of thousands of characters, so the transcript shows a readable excerpt
 * rather than the whole payload.
 */
internal object ThoughtFormatter {

    private val prettyJson = Json { prettyPrint = true }
    private val json = Json { ignoreUnknownKeys = true }

    fun formatArgs(argsJson: String): String {
        val pretty = runCatching {
            prettyJson.encodeToString(JsonElement.serializer(), json.parseToJsonElement(argsJson))
        }.getOrDefault(argsJson)

        return pretty.truncateTo(MAX_THOUGHT_CHARS, ARGS_TRUNCATED_NOTICE)
    }

    fun formatResult(result: String): String {
        val lines = result.lines()
        val byLine = if (lines.size > MAX_THOUGHT_LINES) {
            val hidden = lines.size - MAX_THOUGHT_LINES
            lines.take(MAX_THOUGHT_LINES).joinToString("\n") +
                "\n\n[...Output truncated ($hidden more lines)]"
        } else {
            result
        }

        return byLine.truncateTo(MAX_THOUGHT_CHARS, OUTPUT_TRUNCATED_NOTICE)
    }

    private fun String.truncateTo(limit: Int, notice: String): String =
        if (length <= limit) this else take(limit) + notice
}
