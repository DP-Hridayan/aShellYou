package `in`.hridayan.ashell.ai.data.remote

import android.util.Log
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiContent
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiFunctionDeclaration
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiFunctionResponse
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiGenerationConfig
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiPart
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiRequest
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiResponse
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiSchema
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiSchemaProperty
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiTool
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolCall
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.readLine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

class GeminiProviderClient @Inject constructor(
    private val httpClient: HttpClient,
) : LlmProviderClient {

    override val provider = LlmProvider.Gemini

    override suspend fun complete(
        model: String,
        systemPrompt: String,
        userPrompt: String,
        apiKey: String
    ): String {
        val response: HttpResponse = try {
            val url =
                "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = userPrompt)))),
                        generationConfig = GeminiGenerationConfig(
                            temperature = 0.0f,
                            maxOutputTokens = MAX_OUTPUT_TOKENS,
                        ),
                    )
                )
            }
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        return when (response.status) {
            HttpStatusCode.OK -> try {
                response.body<GeminiResponse>().candidates.first().content.parts.first().text ?: ""
            } catch (e: Exception) {
                throw CloudNetworkException.ParseError(e)
            }

            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden -> {
                val body = runCatching { response.body<String>() }.getOrElse { "" }
                Log.w(TAG, "Gemini auth error ${response.status.value}: $body")
                throw CloudNetworkException.Unauthorized(provider)
            }

            HttpStatusCode.TooManyRequests -> {
                val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                val retryAfter =
                    RETRY_DELAY_REGEX.find(body)?.groupValues?.get(1)?.toDoubleOrNull()?.toInt()
                Log.w(TAG, "Gemini rate limit ${response.status.value}: $body")
                throw CloudNetworkException.RateLimited(retryAfter)
            }

            else -> {
                val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                Log.w(TAG, "Gemini unexpected ${response.status.value}: $body")
                throw CloudNetworkException.ServerError(response.status.value)
            }
        }
    }

    override suspend fun completeWithTools(
        model: String,
        systemPrompt: String,
        userPrompt: String,
        apiKey: String,
        tools: List<AiTool>
    ): String {
        val geminiTools = tools.map { tool ->
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

        val contents = mutableListOf(
            GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))
        )

        while (true) {
            val response: HttpResponse = try {
                val url =
                    "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        GeminiRequest(
                            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                            contents = contents,
                            generationConfig = GeminiGenerationConfig(
                                temperature = 0.0f,
                                maxOutputTokens = MAX_OUTPUT_TOKENS,
                            ),
                            tools = geminiTools.takeIf { it.isNotEmpty() }
                        )
                    )
                }
            } catch (e: Exception) {
                throw CloudNetworkException.NetworkError(e)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val geminiResponse = try {
                        response.body<GeminiResponse>()
                    } catch (e: Exception) {
                        throw CloudNetworkException.ParseError(e)
                    }

                    val candidate = geminiResponse.candidates.firstOrNull() ?: return ""
                    val part = candidate.content.parts.firstOrNull() ?: return ""

                    if (part.functionCall != null) {
                        // The model called a function
                        val functionCall = part.functionCall
                        val tool = tools.find { it.name == functionCall.name }

                        val functionResponseContent = if (tool != null) {
                            val result = try {
                                tool.execute(functionCall.args)
                            } catch (e: Exception) {
                                e.message ?: "Unknown error"
                            }
                            // Append assistant's functionCall to history
                            contents.add(candidate.content)

                            // Append our functionResponse
                            GeminiContent(
                                role = "user",
                                parts = listOf(
                                    GeminiPart(
                                        functionResponse = GeminiFunctionResponse(
                                            name = functionCall.name,
                                            response = buildJsonObject {
                                                put(
                                                    "result",
                                                    JsonPrimitive(result)
                                                )
                                            }
                                        )
                                    )
                                )
                            )
                        } else {
                            contents.add(candidate.content)
                            GeminiContent(
                                role = "user",
                                parts = listOf(
                                    GeminiPart(
                                        functionResponse = GeminiFunctionResponse(
                                            name = functionCall.name,
                                            response = buildJsonObject {
                                                put(
                                                    "error",
                                                    JsonPrimitive("Tool not found")
                                                )
                                            }
                                        )
                                    )
                                )
                            )
                        }
                        contents.add(functionResponseContent)
                        // loop again to send the tool response to the model
                    } else if (part.text != null) {
                        return part.text
                    } else {
                        return ""
                    }
                }

                HttpStatusCode.Unauthorized,
                HttpStatusCode.Forbidden -> {
                    val body = runCatching { response.body<String>() }.getOrElse { "" }
                    Log.w(TAG, "Gemini auth error ${response.status.value}: $body")
                    throw CloudNetworkException.Unauthorized(provider)
                }

                HttpStatusCode.TooManyRequests -> {
                    val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                    val retryAfter =
                        RETRY_DELAY_REGEX.find(body)?.groupValues?.get(1)?.toDoubleOrNull()?.toInt()
                    Log.w(TAG, "Gemini rate limit ${response.status.value}: $body")
                    throw CloudNetworkException.RateLimited(retryAfter)
                }

                else -> {
                    val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                    Log.w(TAG, "Gemini unexpected ${response.status.value}: $body")
                    throw CloudNetworkException.ServerError(response.status.value)
                }
            }
        }
    }

    override suspend fun completeWithHistory(
        model: String,
        systemPrompt: String,
        history: List<LlmMessage>,
        apiKey: String,
        tools: List<AiTool>
    ): LlmMessage {
        val geminiTools = tools.map { tool ->
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

        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

        val contents = history.mapNotNull { msg ->
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

        val response: HttpResponse = try {
            val url =
                "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                        contents = contents,
                        generationConfig = GeminiGenerationConfig(
                            temperature = 0.0f,
                            maxOutputTokens = MAX_OUTPUT_TOKENS,
                        ),
                        tools = geminiTools.takeIf { it.isNotEmpty() }
                    )
                )
            }
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        when (response.status) {
            HttpStatusCode.OK -> {
                val geminiResponse = try {
                    response.body<GeminiResponse>()
                } catch (e: Exception) {
                    throw CloudNetworkException.ParseError(e)
                }

                val candidate = geminiResponse.candidates.firstOrNull()
                    ?: return LlmMessage(role = "model", content = "")

                val part = candidate.content.parts.firstOrNull()
                    ?: return LlmMessage(role = "model", content = "")

                val serializedContent =
                    json.encodeToString(GeminiContent.serializer(), candidate.content)

                if (part.functionCall != null) {
                    return LlmMessage(
                        role = "model",
                        content = "",
                        toolCall = LlmToolCall(
                            name = part.functionCall.name,
                            args = part.functionCall.args
                        ),
                        rawProviderData = serializedContent
                    )
                } else {
                    return LlmMessage(
                        role = "model",
                        content = part.text ?: "",
                        rawProviderData = serializedContent
                    )
                }
            }

            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden -> {
                val body = runCatching { response.body<String>() }.getOrElse { "" }
                Log.w(TAG, "Gemini auth error ${response.status.value}: $body")
                throw CloudNetworkException.Unauthorized(provider)
            }

            HttpStatusCode.TooManyRequests -> {
                val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                val retryAfter =
                    RETRY_DELAY_REGEX.find(body)?.groupValues?.get(1)?.toDoubleOrNull()?.toInt()
                Log.w(TAG, "Gemini rate limit ${response.status.value}: $body")
                throw CloudNetworkException.RateLimited(retryAfter)
            }

            else -> {
                val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                Log.w(TAG, "Gemini unexpected ${response.status.value}: $body")
                throw CloudNetworkException.ServerError(response.status.value)
            }
        }
    }

    override suspend fun completeWithHistoryStream(
        model: String,
        systemPrompt: String,
        history: List<LlmMessage>,
        apiKey: String,
        tools: List<AiTool>,
        onChunk: suspend (String) -> Unit
    ): LlmMessage {
        val geminiTools = tools.map { tool ->
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

        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

        val contents = history.mapNotNull { msg ->
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

        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"

        var finalFullContent = ""
        var rawProviderData = ""
        var toolCall: LlmToolCall? = null

        try {
            httpClient.preparePost(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                        contents = contents,
                        generationConfig = GeminiGenerationConfig(
                            temperature = 0.0f,
                            maxOutputTokens = MAX_OUTPUT_TOKENS,
                        ),
                        tools = geminiTools.takeIf { it.isNotEmpty() }
                    )
                )
            }.execute { response ->
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val channel = response.bodyAsChannel()
                        while (!channel.isClosedForRead) {
                            val line = channel.readLine() ?: break
                            if (line.startsWith("data: ")) {
                                val jsonStr = line.substring(6).trim()
                                if (jsonStr == "[DONE]") continue
                                try {
                                    val geminiResponse =
                                        json.decodeFromString<GeminiResponse>(jsonStr)
                                    val candidate =
                                        geminiResponse.candidates.firstOrNull() ?: continue
                                    val part = candidate.content.parts.firstOrNull() ?: continue
                                    if (part.functionCall != null) {
                                        toolCall = LlmToolCall(
                                            name = part.functionCall.name,
                                            args = part.functionCall.args
                                        )
                                        rawProviderData = json.encodeToString(
                                            GeminiContent.serializer(),
                                            candidate.content
                                        )
                                        break
                                    } else {
                                        val text = part.text ?: ""
                                        finalFullContent += text
                                        onChunk(text)
                                    }
                                } catch (e: Exception) {
                                    // ignore unparseable chunks
                                }
                            }
                        }
                    }

                    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                        throw CloudNetworkException.Unauthorized(provider)
                    }

                    HttpStatusCode.TooManyRequests -> {
                        throw CloudNetworkException.RateLimited(null)
                    }

                    else -> {
                        throw CloudNetworkException.ServerError(response.status.value)
                    }
                }
            }
        } catch (e: CloudNetworkException) {
            throw e
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        if (toolCall != null) {
            return LlmMessage(
                role = "model",
                content = "",
                toolCall = toolCall,
                rawProviderData = rawProviderData
            )
        } else {
            return LlmMessage(
                role = "model",
                content = finalFullContent,
                rawProviderData = json.encodeToString(
                    GeminiContent.serializer(),
                    GeminiContent("model", listOf(GeminiPart(text = finalFullContent)))
                )
            )
        }
    }

    private companion object {
        const val TAG = "GeminiClient"
        const val MAX_OUTPUT_TOKENS = 8192
        val RETRY_DELAY_REGEX = Regex("""Please retry in ([\d.]+)s""")
    }
}

