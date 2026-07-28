package `in`.hridayan.ashell.core.common.domain.provider

import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException

/**
 * Strategy contract for a single LLM provider's HTTP transport.
 *
 * Each implementation is responsible for serializing the request,
 * deserializing the raw text response, and mapping HTTP errors to
 * [CloudNetworkException] subtypes.
 */
interface LlmProviderClient {
    val provider: LlmProvider

    /**
     * Sends [systemPrompt] and [userPrompt] to the provider and returns the raw text completion.
     *
     * @throws CloudNetworkException on any failure.
     */
    suspend fun complete(
        model: String,
        systemPrompt: String,
        userPrompt: String,
        apiKey: String
    ): String

    suspend fun completeWithTools(
        model: String,
        systemPrompt: String,
        userPrompt: String,
        apiKey: String,
        tools: List<`in`.hridayan.ashell.core.common.domain.model.ai.AiTool>
    ): String

    suspend fun completeWithHistory(
        model: String,
        systemPrompt: String,
        history: List<`in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage>,
        apiKey: String,
        tools: List<`in`.hridayan.ashell.core.common.domain.model.ai.AiTool>
    ): `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage

    suspend fun completeWithHistoryStream(
        model: String,
        systemPrompt: String,
        history: List<`in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage>,
        apiKey: String,
        tools: List<`in`.hridayan.ashell.core.common.domain.model.ai.AiTool>,
        onChunk: suspend (String) -> Unit
    ): `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
}
