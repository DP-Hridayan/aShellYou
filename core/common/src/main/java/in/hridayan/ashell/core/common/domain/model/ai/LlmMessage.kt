package `in`.hridayan.ashell.core.common.domain.model.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a generic message in an LLM conversation history.
 *
 * @param rawProviderData Provider-specific payload retained verbatim so a conversation can be
 * replayed without losing details the portable fields cannot express, such as Gemini's thought
 * signatures. It is only meaningful to the provider named by [providerId]; every other provider
 * must ignore it and rebuild the message from the portable fields.
 * @param providerId The [in.hridayan.ashell.core.common.domain.provider.LlmProvider.id] that
 * produced this message. Null for messages written before providers were distinguished, or for
 * messages the app itself constructed.
 * @param reasoning The model's thinking, when it reports that separately from its answer. Shown
 * alongside tool activity rather than in the reply, and never sent back to a provider.
 */
@Serializable
data class LlmMessage(
    val role: String,
    val content: String,
    val toolCalls: List<LlmToolCall> = emptyList(),
    val toolResponses: List<LlmToolResponse> = emptyList(),
    val rawProviderData: String? = null,
    val providerId: String? = null,
    val reasoning: String? = null
)

/**
 * A single function call requested by the model.
 *
 * @param id The provider's identifier for this call. Required by OpenAI-compatible providers, which
 * match a result to its call by id rather than by name. Null for providers that match by name.
 */
@Serializable
data class LlmToolCall(
    val name: String,
    val args: JsonObject?,
    val id: String? = null
)

/**
 * The result of executing one [LlmToolCall].
 *
 * @param toolCallId Mirrors [LlmToolCall.id]. OpenAI-compatible providers reject a turn where any
 * requested call is left unanswered, so this must be carried back verbatim.
 */
@Serializable
data class LlmToolResponse(
    val name: String,
    val result: String,
    val toolCallId: String? = null
)
