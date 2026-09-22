package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import `in`.hridayan.ashell.core.common.domain.provider.LlmModelCatalog
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import javax.inject.Inject
import javax.inject.Singleton

private const val VERIFY_SYSTEM_PROMPT = "Reply exactly with OK"
private const val VERIFY_USER_PROMPT = "hello"

@Singleton
class VerifyApiKeyUseCase @Inject constructor(
    private val clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
    private val modelCatalog: LlmModelCatalog,
    private val fallbackExecutor: ModelFallbackExecutor,
) {
    /**
     * Attempts a minimal completion to verify if the given API key is valid.
     *
     * Returns true if successful, throws a [CloudNetworkException] otherwise. Uses the cheapest
     * tier and the smallest possible prompts so verification costs close to nothing.
     */
    suspend operator fun invoke(provider: LlmProvider, apiKey: String): Boolean {
        val client = clients[provider] ?: throw CloudNetworkException.ProviderNotConfigured(provider)

        // The lite chain is already ordered cheapest first, and walking it means a model that is
        // merely unavailable never gets mistaken for a bad key.
        val models = modelCatalog.chain(provider, ModelTier.LITE, toolsRequired = false)

        fallbackExecutor.execute(provider, models) { model ->
            client.complete(model, VERIFY_SYSTEM_PROMPT, VERIFY_USER_PROMPT, apiKey)
        }

        return true
    }
}
