package `in`.hridayan.ashell.ai.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.ai.domain.tool.ToolRegistry
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolCall
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolResponse
import `in`.hridayan.ashell.core.common.domain.model.ai.SessionIdContext
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TURN_OUTPUT_BUDGET = 50_000
private const val MIN_PER_CALL_BUDGET = 4_000
private const val TOOL_NOT_FOUND = "Tool not found"
private const val UNKNOWN_TOOL_ERROR = "Unknown error"

/**
 * Runs the tool calls a single model turn requested.
 *
 * Calls run **one at a time, in the order the model returned them**. Several of these tools execute
 * shell commands, mutate the database or create Quick Settings tiles, and the permission prompt
 * that guards them can only ask about one command at a time, so overlapping executions would be
 * both unsafe and unanswerable.
 *
 * Every call is answered, including ones that fail: providers reject a turn where a requested call
 * is left without a result.
 */
class ChatToolDispatcher @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val toolRegistry: ToolRegistry,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun dispatch(
        sessionId: String,
        toolCalls: List<LlmToolCall>,
    ): List<LlmToolResponse> {
        val budget = perCallBudget(toolCalls.size)

        return toolCalls.map { call ->
            LlmToolResponse(
                name = call.name,
                result = truncate(execute(sessionId, call), budget),
                toolCallId = call.id,
            )
        }
    }

    private suspend fun execute(sessionId: String, call: LlmToolCall): String {
        val tool = toolRegistry.getToolByName(call.name) ?: return TOOL_NOT_FOUND

        val skill = toolRegistry.getSkillForToolName(call.name)
        if (skill != null) {
            val isEnabled = settingsRepository.getBoolean(skill.settingsKey).firstOrNull()
                ?: skill.settingsKey.default
            if (!isEnabled) {
                val name = context.getString(skill.displayNameRes)
                return "Execution Blocked: The user has disabled the \"$name\" skill. " +
                    "Please inform the user."
            }
        }

        return try {
            withContext(SessionIdContext(sessionId)) { tool.execute(call.args) }
        } catch (e: Exception) {
            e.message ?: UNKNOWN_TOOL_ERROR
        }
    }

    /**
     * Shares one turn's output allowance across its calls so a parallel turn cannot overrun the
     * model's context or Room's cursor window, while keeping a single call's allowance generous.
     */
    private fun perCallBudget(callCount: Int): Int =
        maxOf(TURN_OUTPUT_BUDGET / callCount.coerceAtLeast(1), MIN_PER_CALL_BUDGET)

    private fun truncate(result: String, budget: Int): String =
        if (result.length <= budget) {
            result
        } else {
            result.take(budget) +
                "\n\n[OUTPUT TRUNCATED: Output exceeded $budget characters.]"
        }
}
