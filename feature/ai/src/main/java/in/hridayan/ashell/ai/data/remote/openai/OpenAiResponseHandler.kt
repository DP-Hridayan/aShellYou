package `in`.hridayan.ashell.ai.data.remote.openai

import android.util.Log
import `in`.hridayan.ashell.ai.data.remote.ProviderErrorBody
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

private const val TAG = "OpenAiResponseHandler"
private const val HEADER_RETRY_AFTER = "retry-after"
private const val HTTP_PAYMENT_REQUIRED = 402

private val RETRY_DELAY_REGEX = Regex("""try again in ([\d.]+)s""", RegexOption.IGNORE_CASE)

private val DEAD_MODEL_MARKERS = listOf(
    "model_decommissioned",
    "model_not_found",
    "is not a valid model id",
    "no endpoints found",
)

/**
 * Maps OpenAI-compatible failures onto the app's typed error hierarchy.
 *
 * Distinguishing a dead model from a genuine server error matters: the former should walk the
 * fallback chain, the latter should not be retried against every remaining model.
 */
internal object OpenAiResponseHandler {

    suspend fun handleError(provider: LlmProvider, response: HttpResponse): Nothing {
        val body = runCatching { response.bodyAsText() }.getOrElse { "" }
        Log.w(TAG, "${provider.id} error ${response.status.value}: $body")

        throw toException(provider, response.status.value, body, response.headers[HEADER_RETRY_AFTER])
    }

    fun toException(
        provider: LlmProvider,
        status: Int,
        body: String,
        retryAfterHeader: String? = null,
    ): CloudNetworkException {
        val detail = ProviderErrorBody.detail(body)

        return when {
            // A dead model is checked before the auth statuses because some providers report an
            // unreachable model as a permission failure, and that is worth retrying, not fatal.
            status == HttpStatusCode.NotFound.value || mentionsDeadModel(body) ->
                CloudNetworkException.ModelUnavailable(detail ?: status.toString())

            status == HttpStatusCode.Unauthorized.value ||
                status == HttpStatusCode.Forbidden.value ->
                CloudNetworkException.Unauthorized(provider, detail)

            status == HTTP_PAYMENT_REQUIRED -> CloudNetworkException.InsufficientCredits(provider)

            status == HttpStatusCode.TooManyRequests.value ->
                CloudNetworkException.RateLimited(parseRetryAfter(body, retryAfterHeader))

            else -> CloudNetworkException.ServerError(status, detail)
        }
    }

    /**
     * OpenRouter can report a failure inside an HTTP 200 body instead of an error status.
     *
     * @return the mapped exception, or null when the payload carries no error.
     */
    fun inBandError(provider: LlmProvider, message: String?, code: Int?): CloudNetworkException? {
        if (message == null && code == null) return null
        return toException(provider, code ?: HttpStatusCode.InternalServerError.value, message ?: "")
    }

    private fun mentionsDeadModel(body: String): Boolean {
        val lowercased = body.lowercase()
        return DEAD_MODEL_MARKERS.any { lowercased.contains(it) }
    }

    private fun parseRetryAfter(body: String, header: String?): Int? =
        header?.toIntOrNull()
            ?: RETRY_DELAY_REGEX.find(body)?.groupValues?.get(1)?.toDoubleOrNull()?.toInt()
}
