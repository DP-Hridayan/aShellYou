package `in`.hridayan.ashell.ai.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val ERROR_KEY = "error"
private const val MESSAGE_KEY = "message"
private const val MAX_DETAIL_LENGTH = 300

/**
 * Pulls the human-readable reason out of a provider's error payload.
 *
 * All three providers wrap failures as `{"error":{"message":"..."}}`. Showing that wording is the
 * difference between "Invalid API key" and knowing the account merely needs a privacy setting
 * changed, so it is worth reading rather than discarding.
 */
internal object ProviderErrorBody {

    private val json = Json { ignoreUnknownKeys = true }

    fun detail(body: String): String? {
        if (body.isBlank()) return null

        val parsed = runCatching { json.parseToJsonElement(body) as? JsonObject }.getOrNull()
        val message = parsed?.get(ERROR_KEY)
            ?.let { runCatching { it.jsonObject[MESSAGE_KEY]?.jsonPrimitive?.content }.getOrNull() }

        return (message ?: body).trim().takeIf { it.isNotEmpty() }?.take(MAX_DETAIL_LENGTH)
    }
}
