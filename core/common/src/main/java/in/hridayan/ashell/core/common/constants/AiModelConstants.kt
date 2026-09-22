package `in`.hridayan.ashell.core.common.constants

import `in`.hridayan.ashell.core.common.domain.model.ai.LlmModel
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider

/**
 * Static model tables for the providers whose catalogs are stable enough to hardcode.
 *
 * Every model listed here is reachable on the provider's free tier.
 *
 * OpenRouter is deliberately absent: whether a slug is free and whether it supports tools both
 * change frequently, so its chain is discovered from the provider's own catalog at runtime.
 * [openRouterSeedModels] is only the floor used when both the live fetch and the cache fail.
 */
object AiModelConstants {

    /**
     * Ordered cheapest and most available first.
     *
     * Gemini's stronger flash models are the ones that run out of free quota, and every model that
     * fails costs a full round trip before the next is tried. Leading with the lite models means the
     * common case answers on the first attempt instead of after several timeouts.
     */
    private val geminiQualityModels = listOf(
        LlmModel("gemini-3.1-flash-lite", supportsTools = true),
        LlmModel("gemini-3.5-flash-lite", supportsTools = true),
        LlmModel("gemini-2.5-flash", supportsTools = true),
        LlmModel("gemini-3.5-flash", supportsTools = true),
        LlmModel("gemini-3.6-flash", supportsTools = true),
    )

    private val geminiLiteModels = listOf(
        LlmModel("gemini-3.1-flash-lite", supportsTools = true),
        LlmModel("gemini-3.5-flash-lite", supportsTools = true),
    )

    private val groqQualityModels = listOf(
        LlmModel("openai/gpt-oss-120b", supportsTools = true),
        LlmModel("llama-3.3-70b-versatile", supportsTools = true),
        LlmModel("openai/gpt-oss-20b", supportsTools = true),
        LlmModel("llama-3.1-8b-instant", supportsTools = true),
    )

    private val groqLiteModels = listOf(
        LlmModel("llama-3.1-8b-instant", supportsTools = true),
        LlmModel("openai/gpt-oss-20b", supportsTools = true),
    )

    /**
     * Last-resort roster used only when both the live catalog fetch and its cache fail.
     *
     * `openrouter/free` leads because it is a router across whatever is free at the time, so it
     * keeps working when individual slugs below it have been retired.
     */
    val openRouterSeedModels = listOf(
        LlmModel("openrouter/free", supportsTools = true),
        LlmModel("qwen/qwen3.8-27b:free", supportsTools = true),
        LlmModel("google/gemma-4-31b-it:free", supportsTools = true),
        LlmModel("nvidia/nemotron-3.5-lightning:free", supportsTools = true),
    )

    fun models(provider: LlmProvider, tier: ModelTier): List<LlmModel> = when (provider) {
        LlmProvider.Gemini -> when (tier) {
            ModelTier.QUALITY -> geminiQualityModels
            ModelTier.LITE -> geminiLiteModels
        }

        LlmProvider.Groq -> when (tier) {
            ModelTier.QUALITY -> groqQualityModels
            ModelTier.LITE -> groqLiteModels
        }

        LlmProvider.OpenRouter -> emptyList()
    }

    fun staticChain(
        provider: LlmProvider,
        tier: ModelTier,
        toolsRequired: Boolean,
    ): List<String> = models(provider, tier)
        .filter { !toolsRequired || it.supportsTools }
        .map { it.id }
        .distinct()
}
