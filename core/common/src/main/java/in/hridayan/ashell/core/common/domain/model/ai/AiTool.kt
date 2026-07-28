package `in`.hridayan.ashell.core.common.domain.model.ai

import kotlinx.serialization.json.JsonObject

/**
 * Defines a tool that the AI can call.
 */
interface AiTool {
    /** The name of the tool (must be a valid identifier for LLMs, e.g., a-zA-Z0-9_-) */
    val name: String

    /** A description of what the tool does and when to use it */
    val description: String

    /** The JSON schema for the parameters, represented as a Kotlin Map */
    val parametersSchema: ToolSchema?

    /** Executes the tool with the given JSON arguments and returns a String response */
    suspend fun execute(args: JsonObject?): String
}

/**
 * Represents the JSON schema of a tool's parameters.
 */
data class ToolSchema(
    val type: String = "OBJECT",
    val properties: Map<String, ToolSchemaProperty> = emptyMap(),
    val required: List<String> = emptyList(),
)

data class ToolSchemaProperty(
    val type: String, // "STRING", "INTEGER", "BOOLEAN", etc.
    val description: String? = null,
)

/**
 * A coroutine context element for passing the current chat session ID to tools.
 */
data class SessionIdContext(val sessionId: String) : kotlin.coroutines.AbstractCoroutineContextElement(SessionIdContext) {
    companion object Key : kotlin.coroutines.CoroutineContext.Key<SessionIdContext>
}
