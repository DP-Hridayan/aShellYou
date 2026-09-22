package `in`.hridayan.ashell.ai.data.remote.openai

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolCall
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolResponse
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider

private const val ROLE_USER = "user"
private const val TOOL_NOT_FOUND = "Tool not found"
private const val UNKNOWN_TOOL_ERROR = "Unknown error"

/**
 * Runs the tool calls of a single turn for the self-contained `completeWithTools` loop.
 *
 * Calls run one at a time in the order the model returned them, and every call is answered even
 * when it fails: an unanswered `tool_call_id` makes the provider reject the following turn.
 */
internal object OpenAiToolExecutor {

    suspend fun execute(
        provider: LlmProvider,
        toolCalls: List<LlmToolCall>,
        tools: List<AiTool>,
    ): LlmMessage = LlmMessage(
        role = ROLE_USER,
        content = "",
        toolResponses = toolCalls.map { call -> respond(call, tools) },
        providerId = provider.id,
    )

    private suspend fun respond(call: LlmToolCall, tools: List<AiTool>): LlmToolResponse {
        val tool = tools.find { it.name == call.name }
        val result = if (tool == null) {
            TOOL_NOT_FOUND
        } else {
            runCatching { tool.execute(call.args) }.getOrElse { it.message ?: UNKNOWN_TOOL_ERROR }
        }

        return LlmToolResponse(name = call.name, result = result, toolCallId = call.id)
    }
}
