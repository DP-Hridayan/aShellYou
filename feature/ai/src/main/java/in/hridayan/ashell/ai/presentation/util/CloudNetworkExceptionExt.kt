package `in`.hridayan.ashell.ai.presentation.util

import android.content.Context
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.resources.R

private const val HTTP_BAD_REQUEST = 400

/**
 * Turns a transport failure into something worth showing the user.
 *
 * The complexity suppression is deliberate: this is an exhaustive mapping over a sealed hierarchy,
 * so its branch count tracks the number of error types rather than any branching logic.
 */
@Suppress("CyclomaticComplexMethod")
fun CloudNetworkException.toUserMessage(context: Context): String = when (this) {
    is CloudNetworkException.Unauthorized ->
        context.getString(R.string.error_invalid_api_key, provider.displayName)
            .withDetail(detail)

    is CloudNetworkException.RateLimited -> retryAfterSeconds?.let {
        context.getString(R.string.error_rate_limited_retry_in, it)
    } ?: context.getString(R.string.error_rate_limited)

    is CloudNetworkException.ServerError -> if (code == HTTP_BAD_REQUEST) {
        context.getString(R.string.error_bad_request).withDetail(detail)
    } else {
        context.getString(R.string.error_provider_server, code).withDetail(detail)
    }

    is CloudNetworkException.NetworkError ->
        context.getString(R.string.error_no_connection)

    is CloudNetworkException.ParseError ->
        context.getString(R.string.error_unexpected_response)

    is CloudNetworkException.ProviderNotConfigured ->
        context.getString(R.string.error_no_api_key_for_provider, provider.displayName)

    is CloudNetworkException.ModelUnavailable ->
        context.getString(R.string.error_model_unavailable)

    is CloudNetworkException.InsufficientCredits ->
        context.getString(R.string.error_insufficient_credits, provider.displayName)

    is CloudNetworkException.NoActiveProvider ->
        context.getString(R.string.error_no_active_provider)
}

/** Appends the provider's own wording, which is usually more specific than anything we can say. */
private fun String.withDetail(detail: String?): String =
    detail?.takeIf { it.isNotBlank() }?.let { "$this\n$it" } ?: this
