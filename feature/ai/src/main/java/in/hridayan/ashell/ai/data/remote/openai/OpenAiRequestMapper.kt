package `in`.hridayan.ashell.ai.data.remote.openai

import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiFunctionCall
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiFunctionDeclaration
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiMessage
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiRequest
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiSchema
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiSchemaProperty
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiTool
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiToolCall
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolCall
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import kotlinx.serialization.json.Json
import java.util.Locale

private const val ROLE_SYSTEM = "system"
private const val ROLE_USER = "user"
private const val ROLE_ASSISTANT = "assistant"
private const val ROLE_TOOL = "tool"
private const val MODEL_ROLE = "model"
private const val EMPTY_ARGUMENTS = "{}"
private const val DEFAULT_TEMPERATURE = 0.0f
private const val DEFAULT_MAX_TOKENS = 8192

/**
 * Translates the app's portable conversation model into OpenAI chat-completions requests.
 *
 * Two rules drive most of this class:
 * - Every tool call the model requested must be answered by exactly one `tool` message carrying the
 *   matching `tool_call_id`. Providers reject a turn where any call is left unanswered.
 * - Tool arguments travel as a JSON-encoded string, not an object.
 */
internal object OpenAiRequestMapper {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun mapTools(tools: List<AiTool>): List<OpenAiTool> = tools.map { tool ->
        OpenAiTool(
            function = OpenAiFunctionDeclaration(
                name = tool.name,
                description = tool.description,
                parameters = tool.parametersSchema?.let { schema ->
                    OpenAiSchema(
                        type = normalizeType(schema.type),
                        properties = schema.properties.mapValues { (_, property) ->
                            OpenAiSchemaProperty(
                                type = normalizeType(property.type),
                                description = property.description,
                                items = itemsFor(property.type),
                            )
                        },
                        required = schema.required.takeIf { it.isNotEmpty() },
                    )
                },
            )
        )
    }

    fun mapHistory(systemPrompt: String, history: List<LlmMessage>): List<OpenAiMessage> =
        buildList {
            add(OpenAiMessage(role = ROLE_SYSTEM, content = systemPrompt))
            history.forEach { message -> addAll(mapMessage(message)) }
        }

    fun createRequest(
        model: String,
        messages: List<OpenAiMessage>,
        tools: List<OpenAiTool>?,
        stream: Boolean,
        maxOutputTokens: Int = DEFAULT_MAX_TOKENS,
    ): OpenAiRequest = OpenAiRequest(
        model = model,
        messages = messages,
        tools = tools?.takeIf { it.isNotEmpty() },
        temperature = DEFAULT_TEMPERATURE,
        maxTokens = maxOutputTokens,
        stream = stream,
    )

    fun encodeArguments(toolCall: LlmToolCall): String =
        toolCall.args?.toString() ?: EMPTY_ARGUMENTS

    private fun mapMessage(message: LlmMessage): List<OpenAiMessage> = when {
        message.toolResponses.isNotEmpty() -> message.toolResponses.map { response ->
            OpenAiMessage(
                role = ROLE_TOOL,
                content = response.result,
                toolCallId = response.toolCallId ?: response.name,
            )
        }

        message.toolCalls.isNotEmpty() -> listOf(
            OpenAiMessage(
                role = ROLE_ASSISTANT,
                content = message.content.takeIf { it.isNotBlank() },
                toolCalls = message.toolCalls.mapIndexed { index, call ->
                    OpenAiToolCall(
                        id = call.id ?: syntheticCallId(call, index),
                        function = OpenAiFunctionCall(
                            name = call.name,
                            arguments = encodeArguments(call),
                        ),
                    )
                },
            )
        )

        else -> listOf(
            OpenAiMessage(role = normalizeRole(message.role), content = message.content)
        )
    }

    private fun normalizeRole(role: String): String =
        if (role == MODEL_ROLE || role == ROLE_ASSISTANT) ROLE_ASSISTANT else ROLE_USER

    private fun syntheticCallId(call: LlmToolCall, index: Int): String = "${call.name}_$index"

    private fun normalizeType(type: String): String = type.lowercase(Locale.US)

    /**
     * Both OpenAI-compatible providers reject an array schema without an `items` member. No tool
     * declares one today, so a permissive string element type keeps such a schema valid rather than
     * failing the whole request.
     */
    private fun itemsFor(type: String) =
        if (type == ToolSchemaType.ARRAY) DEFAULT_ARRAY_ITEMS else null

    private val DEFAULT_ARRAY_ITEMS =
        json.parseToJsonElement("""{"type":"string"}""")
}
