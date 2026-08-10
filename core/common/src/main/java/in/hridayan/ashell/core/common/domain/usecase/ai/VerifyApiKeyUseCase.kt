package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.constants.AiModelConstants
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerifyApiKeyUseCase @Inject constructor(
    private val clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
) {
    /**
     * Attempts a minimal completion to verify if the given API key is valid.
     * Returns true if successful, throws a CloudNetworkException otherwise.
     */
    suspend operator fun invoke(provider: LlmProvider, apiKey: String): Boolean {
        val client = clients[provider] ?: throw CloudNetworkException.ProviderNotConfigured(provider)

        val models = if (provider == LlmProvider.Gemini) {
            AiModelConstants.geminiModelsLowestToHighest
        } else {
            emptyList()
        }

        var lastException: CloudNetworkException? = null

        for (model in models) {
            try {
                // We use a tiny system prompt and user prompt to consume the minimum possible tokens.
                client.complete(model, "Reply exactly with OK", "hello", apiKey)
                return true
            } catch (e: CloudNetworkException.RateLimited) {
                lastException = e
                // Keep trying next model
            } catch (e: CloudNetworkException.ServerError) {
                if (e.code == 429 || e.code >= 500 || e.code == 404) {
                    lastException = e
                    // Keep trying next model
                } else {
                    throw e
                }
            } catch (e: CloudNetworkException.NetworkError) {
                lastException = e
                // Keep trying next model
            } catch (e: CloudNetworkException.ParseError) {
                lastException = e
                // Keep trying next model
            } catch (e: CloudNetworkException) {
                // Auth errors (400, 401, 403) should fail immediately
                throw e
            }
        }

        // If all models failed with RateLimited/ServerError, throw the last exception
        throw lastException ?: CloudNetworkException.ProviderNotConfigured(provider)
    }
}
