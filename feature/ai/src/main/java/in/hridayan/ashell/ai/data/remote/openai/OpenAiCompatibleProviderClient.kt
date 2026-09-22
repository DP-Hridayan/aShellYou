package `in`.hridayan.ashell.ai.data.remote.openai

import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiMessage
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiRequest
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiResponse
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiTool
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolCall
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

private const val ROLE_MODEL = "model"
private const val SSE_DATA_PREFIX = "data: "
private const val SSE_COMMENT_PREFIX = ":"
private const val SSE_DONE = "[DONE]"
private const val FINISH_REASON_TOOL_CALLS = "tool_calls"

/**
 * Transport for any provider speaking the OpenAI chat-completions protocol.
 *
 * Unlike the Gemini client this never retains provider-specific payloads: the portable fields of
 * [LlmMessage] express an OpenAI conversation completely, so history is always rebuilt from them
 * and a transcript recorded under a different provider replays without special handling.
 */
internal class OpenAiCompatibleProviderClient(
    private val httpClient: HttpClient,
    private val config: OpenAiCompatibleConfig,
) : LlmProviderClient {

    override val provider: LlmProvider = config.provider

    override suspend fun complete(
        model: String,
        systemPrompt: String,
        userPrompt: String,
        apiKey: String
    ): String {
        val history = listOf(LlmMessage(role = "user", content = userPrompt))
        val response = requestCompletion(model, systemPrompt, history, apiKey, null)
        return response.content
    }

    override suspend fun completeWithTools(
        model: String,
        systemPrompt: String,
        userPrompt: String,
        apiKey: String,
        tools: List<AiTool>
    ): String {
        val mappedTools = OpenAiRequestMapper.mapTools(tools)
        val history = mutableListOf(LlmMessage(role = "user", content = userPrompt))

        while (true) {
            val response = requestCompletion(model, systemPrompt, history, apiKey, mappedTools)
            if (response.toolCalls.isEmpty()) return response.content

            history.add(response)
            history.add(OpenAiToolExecutor.execute(provider, response.toolCalls, tools))
        }
    }

    override suspend fun completeWithHistory(
        model: String,
        systemPrompt: String,
        history: List<LlmMessage>,
        apiKey: String,
        tools: List<AiTool>
    ): LlmMessage = requestCompletion(
        model = model,
        systemPrompt = systemPrompt,
        history = history,
        apiKey = apiKey,
        tools = OpenAiRequestMapper.mapTools(tools),
    )

    override suspend fun completeWithHistoryStream(
        model: String,
        systemPrompt: String,
        history: List<LlmMessage>,
        apiKey: String,
        tools: List<AiTool>,
        onChunk: suspend (String) -> Unit
    ): LlmMessage {
        val accumulator = OpenAiStreamAccumulator()
        val request = OpenAiRequestMapper.createRequest(
            model = model,
            messages = OpenAiRequestMapper.mapHistory(systemPrompt, history),
            tools = OpenAiRequestMapper.mapTools(tools),
            stream = true,
        )
        streamInto(accumulator, request, apiKey, onChunk)

        return LlmMessage(
            role = ROLE_MODEL,
            content = accumulator.text(),
            toolCalls = accumulator.toolCalls(),
            providerId = provider.id,
            reasoning = accumulator.reasoning(),
        )
    }

    private suspend fun streamInto(
        accumulator: OpenAiStreamAccumulator,
        request: OpenAiRequest,
        apiKey: String,
        onChunk: suspend (String) -> Unit,
    ) {
        try {
            httpClient.preparePost(config.chatCompletionsUrl) {
                applyCommonHeaders(apiKey)
                setBody(request)
            }.execute { response ->
                if (response.status != HttpStatusCode.OK) {
                    OpenAiResponseHandler.handleError(provider, response)
                }
                consumeStream(response.bodyAsChannel(), accumulator, onChunk)
            }
        } catch (e: CloudNetworkException) {
            throw e
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }
    }

    private suspend fun requestCompletion(
        model: String,
        systemPrompt: String,
        history: List<LlmMessage>,
        apiKey: String,
        tools: List<OpenAiTool>?,
    ): LlmMessage {
        val request = OpenAiRequestMapper.createRequest(
            model = model,
            messages = OpenAiRequestMapper.mapHistory(systemPrompt, history),
            tools = tools,
            stream = false,
        )

        val response: HttpResponse = try {
            httpClient.post(config.chatCompletionsUrl) {
                applyCommonHeaders(apiKey)
                setBody(request)
            }
        } catch (e: Exception) {
            throw CloudNetworkException.NetworkError(e)
        }

        if (response.status != HttpStatusCode.OK) {
            OpenAiResponseHandler.handleError(provider, response)
        }

        val body = try {
            response.body<OpenAiResponse>()
        } catch (e: Exception) {
            throw CloudNetworkException.ParseError(e)
        }

        throwIfInBandError(body)

        return toLlmMessage(body.choices.firstOrNull()?.message)
    }

    private fun throwIfInBandError(body: OpenAiResponse) {
        val error = body.error ?: return
        val code = error.code?.jsonPrimitive?.intOrNull
        OpenAiResponseHandler.inBandError(provider, error.message, code)?.let { throw it }
    }

    private fun toLlmMessage(message: OpenAiMessage?): LlmMessage = LlmMessage(
        role = ROLE_MODEL,
        content = message?.content.orEmpty(),
        toolCalls = message?.toolCalls.orEmpty().map { call ->
            LlmToolCall(
                name = call.function.name,
                args = OpenAiArguments.parse(call.function.arguments),
                id = call.id,
            )
        },
        providerId = provider.id,
        reasoning = message?.reasoning?.takeIf { it.isNotBlank() },
    )

    private suspend fun consumeStream(
        channel: ByteReadChannel,
        accumulator: OpenAiStreamAccumulator,
        onChunk: suspend (String) -> Unit,
    ) {
        var finished = false
        while (!channel.isClosedForRead && !finished) {
            val line = channel.readUTF8Line()
            finished = line == null ||
                isStreamEnd(line) ||
                consumeStreamLine(line, accumulator, onChunk)
        }
    }

    /** @return true when the stream has delivered everything this turn needs. */
    private suspend fun consumeStreamLine(
        line: String,
        accumulator: OpenAiStreamAccumulator,
        onChunk: suspend (String) -> Unit,
    ): Boolean {
        val payload = ssePayload(line) ?: return false

        val chunk = runCatching {
            OpenAiRequestMapper.json.decodeFromString<OpenAiResponse>(payload)
        }.getOrNull() ?: return false

        throwIfInBandError(chunk)

        val choice = chunk.choices.firstOrNull() ?: return false
        val delta = choice.delta ?: return false

        delta.content?.takeIf { it.isNotEmpty() }?.let {
            accumulator.appendText(it)
            onChunk(it)
        }
        delta.reasoning?.takeIf { it.isNotEmpty() }?.let(accumulator::appendReasoning)
        accumulator.appendToolCalls(delta.toolCalls)

        return choice.finishReason == FINISH_REASON_TOOL_CALLS
    }

    private fun isStreamEnd(line: String): Boolean = ssePayload(line) == SSE_DONE

    /** Returns the data payload of an SSE line, or null for keep-alive comments and blank lines. */
    private fun ssePayload(line: String): String? = when {
        line.startsWith(SSE_DATA_PREFIX) -> line.removePrefix(SSE_DATA_PREFIX).trim()
        line.startsWith(SSE_COMMENT_PREFIX) -> null
        else -> null
    }

    private fun HttpRequestBuilder.applyCommonHeaders(apiKey: String) {
        contentType(ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $apiKey")
        config.extraHeaders.forEach { (name, value) -> header(name, value) }
    }

    private companion object {
        const val TOOL_NOT_FOUND = "Tool not found"
        const val UNKNOWN_TOOL_ERROR = "Unknown error"
    }
}
