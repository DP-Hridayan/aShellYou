package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import javax.inject.Inject
import javax.inject.Singleton

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_SERVER_ERROR_FLOOR = 500

/**
 * Runs [action] against each model in a fallback chain until one succeeds.
 *
 * This is the single retry policy for every cloud LLM call in the app. Transient failures and dead
 * models walk the chain; failures that another model cannot fix are rethrown immediately so the
 * user sees the real cause instead of a slow march through every remaining model.
 */
@Singleton
class ModelFallbackExecutor @Inject constructor() {

    suspend fun <T> execute(
        provider: LlmProvider,
        models: List<String>,
        action: suspend (String) -> T,
    ): T {
        var lastRetryable: CloudNetworkException? = null

        for (model in models) {
            try {
                return action(model)
            } catch (e: CloudNetworkException) {
                if (!isRetryable(e)) throw e
                lastRetryable = e
            }
        }

        throw lastRetryable ?: CloudNetworkException.ProviderNotConfigured(provider)
    }

    private fun isRetryable(e: CloudNetworkException): Boolean = when (e) {
        is CloudNetworkException.RateLimited,
        is CloudNetworkException.NetworkError,
        is CloudNetworkException.ParseError,
        is CloudNetworkException.ModelUnavailable -> true

        is CloudNetworkException.ServerError -> isRetryableStatus(e.code)

        else -> false
    }

    private fun isRetryableStatus(code: Int): Boolean =
        code == HTTP_TOO_MANY_REQUESTS || code >= HTTP_SERVER_ERROR_FLOOR
}
