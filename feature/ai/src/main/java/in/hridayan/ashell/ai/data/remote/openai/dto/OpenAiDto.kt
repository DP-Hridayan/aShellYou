package `in`.hridayan.ashell.ai.data.remote.openai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val tools: List<OpenAiTool>? = null,
    val temperature: Float,
    @SerialName("max_tokens") val maxTokens: Int,
    val stream: Boolean = false,
)

@Serializable
internal data class OpenAiMessage(
    val role: String,
    val content: String? = null,
    /** Some models place their whole answer here and leave [content] empty. Never sent back. */
    val reasoning: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenAiToolCall>? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
)

@Serializable
internal data class OpenAiToolCall(
    val id: String,
    val type: String = FUNCTION_TYPE,
    val function: OpenAiFunctionCall,
)

@Serializable
internal data class OpenAiFunctionCall(
    val name: String,
    val arguments: String,
)

@Serializable
internal data class OpenAiTool(
    val type: String = FUNCTION_TYPE,
    val function: OpenAiFunctionDeclaration,
)

@Serializable
internal data class OpenAiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: OpenAiSchema? = null,
)

@Serializable
internal data class OpenAiSchema(
    val type: String,
    val properties: Map<String, OpenAiSchemaProperty>? = null,
    val required: List<String>? = null,
)

@Serializable
internal data class OpenAiSchemaProperty(
    val type: String,
    val description: String? = null,
    val items: JsonElement? = null,
)

@Serializable
internal data class OpenAiResponse(
    val choices: List<OpenAiChoice> = emptyList(),
    val error: OpenAiError? = null,
)

@Serializable
internal data class OpenAiChoice(
    val message: OpenAiMessage? = null,
    val delta: OpenAiDelta? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
internal data class OpenAiDelta(
    val role: String? = null,
    val content: String? = null,
    val reasoning: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenAiDeltaToolCall> = emptyList(),
)

/**
 * A fragment of a tool call arriving over the stream.
 *
 * Only [index] is guaranteed on every fragment. [id] and the function name typically arrive once on
 * the first fragment, while [OpenAiDeltaFunctionCall.arguments] arrives in pieces that must be
 * concatenated before they can be parsed as JSON.
 */
@Serializable
internal data class OpenAiDeltaToolCall(
    val index: Int = 0,
    val id: String? = null,
    val function: OpenAiDeltaFunctionCall? = null,
)

@Serializable
internal data class OpenAiDeltaFunctionCall(
    val name: String? = null,
    val arguments: String? = null,
)

@Serializable
internal data class OpenAiError(
    val message: String? = null,
    val code: JsonElement? = null,
    val metadata: JsonObject? = null,
)

internal const val FUNCTION_TYPE = "function"
