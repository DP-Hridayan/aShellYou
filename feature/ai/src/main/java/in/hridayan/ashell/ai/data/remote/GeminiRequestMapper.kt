package `in`.hridayan.ashell.ai.data.remote

import `in`.hridayan.ashell.ai.data.remote.dto.GeminiContent
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiFunctionCall
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiFunctionDeclaration
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiFunctionResponse
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiGenerationConfig
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiPart
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiRequest
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiSchema
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiSchemaProperty
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

private const val ROLE_USER = "user"
private const val RESULT_KEY = "result"
private const val DEFAULT_TEMPERATURE = 0.0f
private const val DEFAULT_MAX_TOKENS = 8192

internal object GeminiRequestMapper {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun mapTools(tools: List<AiTool>): List<GeminiTool> {
        return tools.map { tool ->
            GeminiTool(
                functionDeclarations = listOf(
                    GeminiFunctionDeclaration(
                        name = tool.name,
                        description = tool.description,
                        parameters = tool.parametersSchema?.let { schema ->
                            GeminiSchema(
                                type = schema.type,
                                properties = schema.properties.mapValues {
                                    GeminiSchemaProperty(
                                        type = it.value.type,
                                        description = it.value.description
                                    )
                                },
                                required = schema.required.takeIf { it.isNotEmpty() }
                            )
                        }
                    )
                )
            )
        }
    }

    fun mapHistory(history: List<LlmMessage>): MutableList<GeminiContent> =
        history.mapNotNull(::mapMessage).toMutableList()

    fun createRequest(
        systemPrompt: String,
        contents: List<GeminiContent>,
        geminiTools: List<GeminiTool>?,
        maxOutputTokens: Int = DEFAULT_MAX_TOKENS
    ): GeminiRequest {
        return GeminiRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = DEFAULT_TEMPERATURE,
                maxOutputTokens = maxOutputTokens,
            ),
            tools = geminiTools?.takeIf { it.isNotEmpty() }
        )
    }

    fun toolResponseContent(message: LlmMessage): GeminiContent = GeminiContent(
        role = ROLE_USER,
        parts = message.toolResponses.map { response ->
            GeminiPart(
                functionResponse = GeminiFunctionResponse(
                    name = response.name,
                    response = buildJsonObject { put(RESULT_KEY, JsonPrimitive(response.result)) }
                )
            )
        }
    )

    private fun mapMessage(message: LlmMessage): GeminiContent? = when {
        message.toolResponses.isNotEmpty() -> toolResponseContent(message)
        else -> decodeOwnPayload(message) ?: fallbackContent(message)
    }

    /**
     * Replays the exact payload Gemini produced, which preserves details the portable fields cannot
     * carry, such as thought signatures.
     *
     * A payload written by a different provider is meaningless here, so it is ignored and the
     * message is rebuilt from the portable fields instead. Messages recorded before providers were
     * distinguished carry no id and are assumed to be Gemini's.
     */
    private fun decodeOwnPayload(message: LlmMessage): GeminiContent? {
        val raw = message.rawProviderData ?: return null
        val providerId = message.providerId
        if (providerId != null && providerId != LlmProvider.Gemini.id) return null

        return try {
            json.decodeFromString<GeminiContent>(raw)
        } catch (e: Exception) {
            null
        }
    }

    private fun fallbackContent(message: LlmMessage): GeminiContent = when {
        message.toolCalls.isNotEmpty() -> GeminiContent(
            role = message.role,
            parts = message.toolCalls.map { call ->
                GeminiPart(functionCall = GeminiFunctionCall(name = call.name, args = call.args))
            }
        )

        else -> GeminiContent(role = message.role, parts = listOf(GeminiPart(text = message.content)))
    }
}
