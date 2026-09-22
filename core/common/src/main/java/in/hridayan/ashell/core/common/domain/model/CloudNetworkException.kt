package `in`.hridayan.ashell.core.common.domain.model

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider

/**
 * Typed error hierarchy for cloud LLM network operations.
 *
 * Callers should `when`-match exhaustively to produce user-facing messages
 * without coupling UI to raw HTTP codes or exception strings.
 */
sealed class CloudNetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * The configured API key was rejected by the provider.
     *
     * @param detail The provider's own wording, when it sent any. Providers reject keys for reasons
     * that are not always about the key itself, so this is surfaced rather than swallowed.
     */
    class Unauthorized(val provider: LlmProvider, val detail: String? = null) :
        CloudNetworkException(
            detail?.let { "Invalid API key for ${provider.displayName}: $it" }
                ?: "Invalid API key for ${provider.displayName}"
        )

    /**
     * The provider is throttling requests.
     *
     * @param retryAfterSeconds Hint from the provider's `Retry-After` header, if present.
     */
    class RateLimited(val retryAfterSeconds: Int? = null) : CloudNetworkException(
        if (retryAfterSeconds != null) {
            "Rate limited — retry after ${retryAfterSeconds}s"
        } else {
            "Rate limited by provider"
        }
    )

    /** The provider returned an unexpected HTTP error code. */
    class ServerError(val code: Int, val detail: String? = null) :
        CloudNetworkException(
            detail?.let { "Provider server error: HTTP $code: $it" }
                ?: "Provider server error: HTTP $code"
        )

    /** A transport-level failure occurred before a response was received. */
    class NetworkError(cause: Throwable) : CloudNetworkException("Network error: ${cause.message}", cause)

    /** The provider returned a response that could not be parsed into [AnalysisResult]. */
    class ParseError(cause: Throwable) : CloudNetworkException("Response parse error: ${cause.message}", cause)

    /** No API key has been saved for the requested provider. */
    class ProviderNotConfigured(val provider: LlmProvider) :
        CloudNetworkException("No API key configured for ${provider.displayName}")

    /**
     * The requested model no longer exists or was decommissioned by the provider.
     *
     * Retryable: callers should fall through to the next model in the chain.
     */
    class ModelUnavailable(val model: String) :
        CloudNetworkException("Model no longer available: $model")

    /** The account has no remaining credit with the provider. Trying another model will not help. */
    class InsufficientCredits(val provider: LlmProvider) :
        CloudNetworkException("No remaining credit for ${provider.displayName}")

    /** The user has not chosen an active provider. */
    class NoActiveProvider : CloudNetworkException("No active AI provider selected")
}
