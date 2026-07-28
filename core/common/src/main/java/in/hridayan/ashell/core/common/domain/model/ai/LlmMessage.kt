package `in`.hridayan.ashell.core.common.domain.model.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a generic message in an LLM conversation history.
 */
@Serializable
data class LlmMessage(
    val role: String, // "user" or "model"
    val content: String,
    val toolCall: LlmToolCall? = null,
    val toolResponse: LlmToolResponse? = null,
    // Used to retain provider-specific data (like Gemini's thought_signature or tool calls)
    val rawProviderData: String? = null
)

@Serializable
data class LlmToolCall(
    val name: String,
    val args: JsonObject?
)

@Serializable
data class LlmToolResponse(
    val name: String,
    val result: String
)
