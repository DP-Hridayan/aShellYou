package `in`.hridayan.ashell.ai.data.remote

import `in`.hridayan.ashell.ai.data.remote.dto.GeminiContent
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiFunctionResponse
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiPart
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiResponse
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
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
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
        val contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = userPrompt))))

        val response: HttpResponse = try {
            val url = buildGeminiGenerateContentUrl(model, apiKey)
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequestMapper.createRequest(systemPrompt, contents, null))
            }
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        if (response.status == HttpStatusCode.OK) {
            return try {
                response.body<GeminiResponse>().candidates.first().content.parts.first().text ?: ""
            } catch (e: Exception) {
                throw CloudNetworkException.ParseError(e)
            }
        } else {
            GeminiResponseHandler.handleError(response)
        }
    }

    override suspend fun completeWithTools(
        model: String,
        systemPrompt: String,
        userPrompt: String,
        apiKey: String,
        tools: List<AiTool>
    ): String {
        val geminiTools = GeminiRequestMapper.mapTools(tools)
        val contents = mutableListOf(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userPrompt))
            )
        )

        while (true) {
            val response: HttpResponse = try {
                val url = buildGeminiGenerateContentUrl(model, apiKey)
                httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(GeminiRequestMapper.createRequest(systemPrompt, contents, geminiTools))
                }
            } catch (e: Exception) {
                throw CloudNetworkException.NetworkError(e)
            }

            if (response.status == HttpStatusCode.OK) {
                val geminiResponse = try {
                    response.body<GeminiResponse>()
                } catch (e: Exception) {
                    throw CloudNetworkException.ParseError(e)
                }

                val candidate = geminiResponse.candidates.firstOrNull() ?: return ""
                val part = candidate.content.parts.firstOrNull() ?: return ""

                if (part.functionCall != null) {
                    val functionCall = part.functionCall
                    val tool = tools.find { it.name == functionCall.name }

                    val functionResponseContent = if (tool != null) {
                        val result = try {
                            tool.execute(functionCall.args)
                        } catch (e: Exception) {
                            e.message ?: "Unknown error"
                        }
                        contents.add(candidate.content)

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
                } else if (part.text != null) {
                    return part.text
                } else {
                    return ""
                }
            } else {
                GeminiResponseHandler.handleError(response)
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
        val geminiTools = GeminiRequestMapper.mapTools(tools)
        val contents = GeminiRequestMapper.mapHistory(history)

        val response: HttpResponse = try {
            val url = buildGeminiGenerateContentUrl(model, apiKey)
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequestMapper.createRequest(systemPrompt, contents, geminiTools))
            }
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        if (response.status == HttpStatusCode.OK) {
            val geminiResponse = try {
                response.body<GeminiResponse>()
            } catch (e: Exception) {
                throw CloudNetworkException.ParseError(e)
            }

            val candidate = geminiResponse.candidates.firstOrNull() ?: return LlmMessage(
                role = "model",
                content = ""
            )
            val part = candidate.content.parts.firstOrNull() ?: return LlmMessage(
                role = "model",
                content = ""
            )
            val serializedContent = GeminiRequestMapper.json.encodeToString(
                GeminiContent.serializer(),
                candidate.content
            )

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
        } else {
            GeminiResponseHandler.handleError(response)
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
        val geminiTools = GeminiRequestMapper.mapTools(tools)
        val contents = GeminiRequestMapper.mapHistory(history)
        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
        var streamResult: StreamResult? = null

        try {
            httpClient.preparePost(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequestMapper.createRequest(systemPrompt, contents, geminiTools))
            }.execute { response ->
                if (response.status == HttpStatusCode.OK) {
                    streamResult = parseSseStream(response.bodyAsChannel(), onChunk)
                } else {
                    GeminiResponseHandler.handleError(response)
                }
            }
        } catch (e: CloudNetworkException) {
            throw e
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        val result = streamResult ?: StreamResult(null, "", "")

        return if (result.toolCall != null) {
            LlmMessage(
                role = "model",
                content = "",
                toolCall = result.toolCall,
                rawProviderData = result.rawProviderData
            )
        } else {
            LlmMessage(
                role = "model",
                content = result.finalFullContent,
                rawProviderData = GeminiRequestMapper.json.encodeToString(
                    GeminiContent.serializer(),
                    GeminiContent(
                        role = "model",
                        parts = listOf(GeminiPart(text = result.finalFullContent))
                    )
                )
            )
        }
    }

    private suspend fun parseSseStream(
        channel: ByteReadChannel,
        onChunk: suspend (String) -> Unit
    ): StreamResult {
        var finalFullContent = ""
        var rawProviderData = ""
        var toolCall: LlmToolCall? = null

        while (!channel.isClosedForRead) {
            val line = channel.readLine() ?: break
            if (line.startsWith("data: ")) {
                val jsonStr = line.substring(6).trim()
                if (jsonStr == "[DONE]") continue
                try {
                    val geminiResponse =
                        GeminiRequestMapper.json.decodeFromString<GeminiResponse>(jsonStr)
                    val candidate = geminiResponse.candidates.firstOrNull() ?: continue
                    val part = candidate.content.parts.firstOrNull() ?: continue

                    if (part.functionCall != null) {
                        toolCall = LlmToolCall(
                            name = part.functionCall.name,
                            args = part.functionCall.args
                        )
                        rawProviderData = GeminiRequestMapper.json.encodeToString(
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
        return StreamResult(toolCall, rawProviderData, finalFullContent)
    }

    private fun buildGeminiGenerateContentUrl(model: String, apiKey: String): String {
        return "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
    }

    private data class StreamResult(
        val toolCall: LlmToolCall?,
        val rawProviderData: String,
        val finalFullContent: String
    )
}
