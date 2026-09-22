package `in`.hridayan.ashell.ai.data.remote.openai

import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiDeltaToolCall
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolCall

/**
 * Rebuilds whole tool calls from the fragments an OpenAI-compatible stream delivers.
 *
 * A single tool call is split across many SSE chunks: the id and function name usually arrive once,
 * then the arguments arrive as partial JSON that is only valid once concatenated. Several calls can
 * be interleaved in the same stream, distinguished only by their `index`, so fragments are grouped
 * by index and parsed at the end rather than per chunk.
 */
internal class OpenAiStreamAccumulator {

    private val fragments = LinkedHashMap<Int, ToolCallFragment>()
    private val text = StringBuilder()
    private val reasoning = StringBuilder()

    fun appendText(chunk: String) {
        text.append(chunk)
    }

    /**
     * Records a chunk of the model's thinking.
     *
     * Several models answer entirely in this channel and leave the content channel empty, so it is
     * kept as a fallback rather than discarded.
     */
    fun appendReasoning(chunk: String) {
        reasoning.append(chunk)
    }

    fun appendToolCalls(deltas: List<OpenAiDeltaToolCall>) {
        deltas.forEach { delta ->
            val fragment = fragments.getOrPut(delta.index) { ToolCallFragment() }
            delta.id?.let { fragment.id = it }
            delta.function?.name?.let { fragment.name = it }
            delta.function?.arguments?.let { fragment.arguments.append(it) }
        }
    }

    fun text(): String = text.toString()

    fun reasoning(): String? = reasoning.toString().takeIf { it.isNotBlank() }

    fun hasToolCalls(): Boolean = fragments.isNotEmpty()

    /** Fragments with no function name are dropped: they cannot be dispatched to a tool. */
    fun toolCalls(): List<LlmToolCall> = fragments.values.mapNotNull { fragment ->
        val name = fragment.name ?: return@mapNotNull null
        LlmToolCall(
            name = name,
            args = OpenAiArguments.parse(fragment.arguments.toString()),
            id = fragment.id,
        )
    }

    private class ToolCallFragment {
        var id: String? = null
        var name: String? = null
        val arguments = StringBuilder()
    }
}
