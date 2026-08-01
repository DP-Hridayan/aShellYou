package `in`.hridayan.ashell.ai.data.remote

import `in`.hridayan.ashell.ai.data.remote.dto.GeminiContent
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

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

    fun mapHistory(history: List<LlmMessage>): MutableList<GeminiContent> {
        return history.mapNotNull { msg ->
            val rawData = msg.rawProviderData
            val toolRes = msg.toolResponse

            if (rawData != null) {
                try {
                    json.decodeFromString<GeminiContent>(rawData)
                } catch (e: Exception) {
                    null
                }
            } else if (toolRes != null) {
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(
                            functionResponse = GeminiFunctionResponse(
                                name = toolRes.name,
                                response = buildJsonObject {
                                    put(
                                        "result",
                                        JsonPrimitive(toolRes.result)
                                    )
                                }
                            )
                        )
                    )
                )
            } else {
                GeminiContent(role = msg.role, parts = listOf(GeminiPart(text = msg.content)))
            }
        }.toMutableList()
    }

    fun createRequest(
        systemPrompt: String,
        contents: List<GeminiContent>,
        geminiTools: List<GeminiTool>?,
        maxOutputTokens: Int = 8192
    ): GeminiRequest {
        return GeminiRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.0f,
                maxOutputTokens = maxOutputTokens,
            ),
            tools = geminiTools?.takeIf { it.isNotEmpty() }
        )
    }
}
