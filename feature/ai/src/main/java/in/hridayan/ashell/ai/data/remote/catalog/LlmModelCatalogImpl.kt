package `in`.hridayan.ashell.ai.data.remote.catalog

import `in`.hridayan.ashell.core.common.constants.AiModelConstants
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import `in`.hridayan.ashell.core.common.domain.provider.LlmModelCatalog
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serves each provider's fallback chain from whichever source that provider needs.
 *
 * Gemini and Groq publish stable enough rosters to ship in the app; OpenRouter's free roster turns
 * over too fast for that and is read from its catalog at runtime.
 */
@Singleton
class LlmModelCatalogImpl @Inject constructor(
    private val openRouterModelCatalog: OpenRouterModelCatalog,
) : LlmModelCatalog {

    override suspend fun chain(
        provider: LlmProvider,
        tier: ModelTier,
        toolsRequired: Boolean,
    ): List<String> = when (provider) {
        LlmProvider.OpenRouter -> openRouterModelCatalog.chain(tier, toolsRequired)
        else -> AiModelConstants.staticChain(provider, tier, toolsRequired)
    }
}
