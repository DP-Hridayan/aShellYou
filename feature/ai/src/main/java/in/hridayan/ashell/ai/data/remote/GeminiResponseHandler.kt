package `in`.hridayan.ashell.ai.data.remote

import android.util.Log
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

internal object GeminiResponseHandler {
    private const val TAG = "GeminiResponseHandler"
    private val RETRY_DELAY_REGEX = Regex("""Please retry in ([\d.]+)s""")

    suspend fun handleError(response: HttpResponse): Nothing {
        when (response.status) {
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden -> {
                val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                Log.w(TAG, "Gemini auth error ${response.status.value}: $body")
                throw CloudNetworkException.Unauthorized(LlmProvider.Gemini)
            }

            HttpStatusCode.TooManyRequests -> {
                val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                val retryAfter =
                    RETRY_DELAY_REGEX.find(body)?.groupValues?.get(1)?.toDoubleOrNull()?.toInt()
                Log.w(TAG, "Gemini rate limit ${response.status.value}: $body")
                throw CloudNetworkException.RateLimited(retryAfter)
            }

            else -> {
                val body = runCatching { response.bodyAsText() }.getOrElse { "" }
                Log.w(TAG, "Gemini unexpected ${response.status.value}: $body")
                throw CloudNetworkException.ServerError(response.status.value)
            }
        }
    }
}
