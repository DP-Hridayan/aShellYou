package `in`.hridayan.ashell.ai.data.remote.openai

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Decodes the JSON-encoded string that OpenAI-compatible providers use for tool arguments.
 *
 * Returns null rather than throwing: a malformed argument payload should surface to the model as a
 * tool invoked without arguments, not as a failed turn.
 */
internal object OpenAiArguments {

    private const val TAG = "OpenAiArguments"

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String?): JsonObject? {
        if (raw.isNullOrBlank()) return null
        return try {
            json.parseToJsonElement(raw) as? JsonObject
        } catch (e: Exception) {
            Log.w(TAG, "Malformed tool arguments; invoking the tool without them", e)
            null
        }
    }
}
