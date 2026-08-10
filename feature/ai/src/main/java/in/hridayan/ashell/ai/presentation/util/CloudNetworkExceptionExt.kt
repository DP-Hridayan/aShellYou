package `in`.hridayan.ashell.ai.presentation.util

import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException

fun CloudNetworkException.toUserMessage(): String = when (this) {
    is CloudNetworkException.Unauthorized ->
        "Invalid API key for ${provider.displayName}. Check your settings."
    is CloudNetworkException.RateLimited ->
        if (retryAfterSeconds != null) {
            "Rate limited — try again in ${retryAfterSeconds}s."
        } else {
            "Rate limited by provider. Try again shortly."
        }
    is CloudNetworkException.ServerError ->
        if (code == 400) {
            "Request error (HTTP 400): Invalid request or tool schema."
        } else {
            "Provider server error ($code). Try again later."
        }
    is CloudNetworkException.NetworkError ->
        "No internet connection or request timed out."
    is CloudNetworkException.ParseError ->
        "Unexpected response from provider. Try again."
    is CloudNetworkException.ProviderNotConfigured ->
        "No API key set for ${provider.displayName}. Enter one below."
}
