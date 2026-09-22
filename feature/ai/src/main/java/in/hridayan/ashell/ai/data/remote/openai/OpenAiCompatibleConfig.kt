package `in`.hridayan.ashell.ai.data.remote.openai

import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider

/**
 * Everything that differs between two providers speaking the OpenAI chat-completions protocol.
 *
 * Groq and OpenRouter share request shapes, tool-call semantics and SSE framing, so they share a
 * client. Only the endpoint, the attribution headers and a handful of error dialects differ.
 */
internal data class OpenAiCompatibleConfig(
    val provider: LlmProvider,
    val chatCompletionsUrl: String,
    val extraHeaders: Map<String, String> = emptyMap(),
) {
    companion object {
        private const val GROQ_CHAT_COMPLETIONS_URL =
            "https://api.groq.com/openai/v1/chat/completions"
        private const val OPENROUTER_CHAT_COMPLETIONS_URL =
            "https://openrouter.ai/api/v1/chat/completions"

        private const val HEADER_REFERER = "HTTP-Referer"
        private const val HEADER_TITLE = "X-Title"
        private const val APP_TITLE = "aShell You"

        val Groq = OpenAiCompatibleConfig(
            provider = LlmProvider.Groq,
            chatCompletionsUrl = GROQ_CHAT_COMPLETIONS_URL,
        )

        val OpenRouter = OpenAiCompatibleConfig(
            provider = LlmProvider.OpenRouter,
            chatCompletionsUrl = OPENROUTER_CHAT_COMPLETIONS_URL,
            extraHeaders = mapOf(
                HEADER_REFERER to UrlConst.URL_GITHUB_REPO,
                HEADER_TITLE to APP_TITLE,
            ),
        )
    }
}
