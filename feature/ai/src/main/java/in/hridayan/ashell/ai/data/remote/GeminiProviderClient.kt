package `in`.hridayan.ashell.ai.data.remote

import `in`.hridayan.ashell.ai.data.remote.dto.GeminiCandidate
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiContent
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiFunctionResponse
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiPart
import `in`.hridayan.ashell.ai.data.remote.dto.GeminiResponse
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
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

private const val ROLE_MODEL = "model"
private const val ROLE_USER = "user"
private const val RESULT_KEY = "result"
private const val ERROR_KEY = "error"
private const val TOOL_NOT_FOUND = "Tool not found"
private const val UNKNOWN_TOOL_ERROR = "Unknown error"
private const val SSE_DATA_PREFIX = "data: "
private const val SSE_DONE = "[DONE]"
private const val API_BASE = "https://generativelanguage.googleapis.com/v1beta/models"

private fun encode(content: GeminiContent): String =
    GeminiRequestMapper.json.encodeToString(GeminiContent.serializer(), content)

private fun generateContentUrl(model: String, apiKey: String): String =
    "$API_BASE/$model:generateContent?key=$apiKey"

private fun streamUrl(model: String, apiKey: String): String =
    "$API_BASE/$model:streamGenerateContent?alt=sse&key=$apiKey"

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
        val candidate = requestCandidate(model, systemPrompt, contents, null, apiKey)
        return candidate?.content?.parts?.firstOrNull { it.text != null }?.text.orEmpty()
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
            GeminiContent(role = ROLE_USER, parts = listOf(GeminiPart(text = userPrompt)))
        )

        while (true) {
            val candidate = requestCandidate(model, systemPrompt, contents, geminiTools, apiKey)
                ?: return ""
            val toolCalls = candidate.content.toolCalls()

            if (toolCalls.isEmpty()) {
                return candidate.content.parts.firstOrNull { it.text != null }?.text.orEmpty()
            }

            contents.add(candidate.content)
            contents.add(executeToolCalls(toolCalls, tools))
        }
    }

    override suspend fun completeWithHistory(
        model: String,
        systemPrompt: String,
        history: List<LlmMessage>,
        apiKey: String,
        tools: List<AiTool>
    ): LlmMessage {
        val candidate = requestCandidate(
            model = model,
            systemPrompt = systemPrompt,
            contents = GeminiRequestMapper.mapHistory(history),
            geminiTools = GeminiRequestMapper.mapTools(tools),
            apiKey = apiKey,
        ) ?: return emptyModelMessage()

        return candidate.content.toLlmMessage()
    }

    override suspend fun completeWithHistoryStream(
        model: String,
        systemPrompt: String,
        history: List<LlmMessage>,
        apiKey: String,
        tools: List<AiTool>,
        onChunk: suspend (String) -> Unit
    ): LlmMessage {
        val stream = openStream(model, systemPrompt, history, apiKey, tools, onChunk)
            ?: return emptyModelMessage()

        return if (stream.toolCalls.isNotEmpty()) {
            LlmMessage(
                role = ROLE_MODEL,
                content = "",
                toolCalls = stream.toolCalls,
                rawProviderData = stream.rawProviderData,
                providerId = provider.id,
            )
        } else {
            LlmMessage(
                role = ROLE_MODEL,
                content = stream.text,
                rawProviderData = encode(
                    GeminiContent(role = ROLE_MODEL, parts = listOf(GeminiPart(text = stream.text)))
                ),
                providerId = provider.id,
            )
        }
    }

    private suspend fun openStream(
        model: String,
        systemPrompt: String,
        history: List<LlmMessage>,
        apiKey: String,
        tools: List<AiTool>,
        onChunk: suspend (String) -> Unit,
    ): StreamResult? {
        val request = GeminiRequestMapper.createRequest(
            systemPrompt = systemPrompt,
            contents = GeminiRequestMapper.mapHistory(history),
            geminiTools = GeminiRequestMapper.mapTools(tools),
        )
        var result: StreamResult? = null

        try {
            httpClient.preparePost(streamUrl(model, apiKey)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.execute { response ->
                if (response.status != HttpStatusCode.OK) {
                    GeminiResponseHandler.handleError(provider, response)
                }
                result = parseSseStream(response.bodyAsChannel(), onChunk)
            }
        } catch (e: CloudNetworkException) {
            throw e
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        return result
    }

    private suspend fun requestCandidate(
        model: String,
        systemPrompt: String,
        contents: List<GeminiContent>,
        geminiTools: List<GeminiTool>?,
        apiKey: String,
    ): GeminiCandidate? {
        val response: HttpResponse = try {
            httpClient.post(generateContentUrl(model, apiKey)) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequestMapper.createRequest(systemPrompt, contents, geminiTools))
            }
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        if (response.status != HttpStatusCode.OK) {
            GeminiResponseHandler.handleError(provider, response)
        }

        return try {
            response.body<GeminiResponse>().candidates.firstOrNull()
        } catch (e: Exception) {
            throw CloudNetworkException.ParseError(e)
        }
    }

    private suspend fun executeToolCalls(
        toolCalls: List<LlmToolCall>,
        tools: List<AiTool>,
    ): GeminiContent = GeminiContent(
        role = ROLE_USER,
        parts = toolCalls.map { call ->
            val tool = tools.find { it.name == call.name }
            val payload = if (tool == null) {
                buildJsonObject { put(ERROR_KEY, JsonPrimitive(TOOL_NOT_FOUND)) }
            } else {
                val result = runCatching { tool.execute(call.args) }
                    .getOrElse { it.message ?: UNKNOWN_TOOL_ERROR }
                buildJsonObject { put(RESULT_KEY, JsonPrimitive(result)) }
            }
            GeminiPart(
                functionResponse = GeminiFunctionResponse(name = call.name, response = payload)
            )
        }
    )

    private suspend fun parseSseStream(
        channel: ByteReadChannel,
        onChunk: suspend (String) -> Unit
    ): StreamResult {
        val text = StringBuilder()
        val toolCalls = mutableListOf<LlmToolCall>()
        var rawProviderData = ""

        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            val candidate = decodeCandidate(line) ?: continue

            val calls = candidate.content.toolCalls()
            if (calls.isNotEmpty()) {
                toolCalls += calls
                rawProviderData = encode(candidate.content)
                break
            }

            candidate.content.parts.mapNotNull { it.text }.forEach { chunk ->
                text.append(chunk)
                onChunk(chunk)
            }
        }

        return StreamResult(toolCalls, rawProviderData, text.toString())
    }

    /** @return the candidate carried by an SSE line, or null for framing lines and noise. */
    private fun decodeCandidate(line: String): GeminiCandidate? {
        if (!line.startsWith(SSE_DATA_PREFIX)) return null

        val payload = line.removePrefix(SSE_DATA_PREFIX).trim()
        if (payload == SSE_DONE) return null

        return runCatching {
            GeminiRequestMapper.json.decodeFromString<GeminiResponse>(payload)
        }.getOrNull()?.candidates?.firstOrNull()
    }

    private fun GeminiContent.toolCalls(): List<LlmToolCall> = parts.mapNotNull { part ->
        part.functionCall?.let { LlmToolCall(name = it.name, args = it.args) }
    }

    private fun GeminiContent.toLlmMessage(): LlmMessage {
        val toolCalls = toolCalls()
        return LlmMessage(
            role = ROLE_MODEL,
            content = if (toolCalls.isEmpty()) {
                parts.firstOrNull { it.text != null }?.text.orEmpty()
            } else {
                ""
            },
            toolCalls = toolCalls,
            rawProviderData = encode(this),
            providerId = provider.id,
        )
    }

    private fun emptyModelMessage(): LlmMessage =
        LlmMessage(role = ROLE_MODEL, content = "", providerId = provider.id)

    private data class StreamResult(
        val toolCalls: List<LlmToolCall>,
        val rawProviderData: String,
        val text: String
    )
}
