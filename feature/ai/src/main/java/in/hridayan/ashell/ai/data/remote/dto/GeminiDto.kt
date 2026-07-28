package `in`.hridayan.ashell.ai.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class GeminiRequest(
    @SerialName("system_instruction") val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
    @SerialName("generationConfig") val generationConfig: GeminiGenerationConfig,
    val tools: List<GeminiTool>? = null,
)

@Serializable
internal data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>,
)

@Serializable
internal data class GeminiPart(
    val text: String? = null,
    val functionCall: GeminiFunctionCall? = null,
    val functionResponse: GeminiFunctionResponse? = null,
    val executableCode: kotlinx.serialization.json.JsonElement? = null,
    val codeExecutionResult: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("thought_signature") val thoughtSignatureSnakeCase: String? = null,
    @SerialName("thoughtSignature") val thoughtSignatureCamelCase: String? = null,
    val thoughtCall: kotlinx.serialization.json.JsonElement? = null,
    val thought: kotlinx.serialization.json.JsonElement? = null,
)

@Serializable
internal data class GeminiFunctionCall(
    val name: String,
    val args: JsonObject? = null,
    @SerialName("thought_signature") val thoughtSignatureSnakeCase: String? = null,
    @SerialName("thoughtSignature") val thoughtSignatureCamelCase: String? = null,
    val thoughtCall: kotlinx.serialization.json.JsonElement? = null,
    val thought: kotlinx.serialization.json.JsonElement? = null,
)

@Serializable
internal data class GeminiFunctionResponse(
    val name: String,
    val response: JsonObject,
)

@Serializable
internal data class GeminiTool(
    val functionDeclarations: List<GeminiFunctionDeclaration>,
)

@Serializable
internal data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: GeminiSchema? = null,
)

@Serializable
internal data class GeminiSchema(
    val type: String,
    val properties: Map<String, GeminiSchemaProperty>? = null,
    val required: List<String>? = null,
)

@Serializable
internal data class GeminiSchemaProperty(
    val type: String,
    val description: String? = null,
)

@Serializable
internal data class GeminiGenerationConfig(
    val temperature: Float,
    @SerialName("maxOutputTokens") val maxOutputTokens: Int,
)

@Serializable
internal data class GeminiResponse(
    val candidates: List<GeminiCandidate>,
)

@Serializable
internal data class GeminiCandidate(
    val content: GeminiContent,
)
