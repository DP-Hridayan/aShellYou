package `in`.hridayan.ashell.ai.data.remote.catalog

import `in`.hridayan.ashell.ai.data.remote.catalog.dto.OpenRouterModelDto
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmModel
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier

private const val TOOLS_PARAMETER = "tools"
private const val MAX_CHAIN_LENGTH = 5

/**
 * Turns OpenRouter's full catalog into a short, ordered fallback chain of free models.
 *
 * OpenRouter's free roster turns over constantly, so nothing here hardcodes a slug as a
 * requirement. [PREFERRED_QUALITY] and [PREFERRED_LITE] express a preference that is honoured when
 * a slug happens to still exist; everything else falls back to ranking by context window, which
 * keeps the chain sensible even after a complete catalog turnover.
 */
internal object OpenRouterModelSelector {

    /** `openrouter/free` is a router across the free roster, so it outlives individual slugs. */
    private val PREFERRED_QUALITY = listOf(
        "openrouter/free",
        "qwen/qwen3.8-27b:free",
        "google/gemma-4-31b-it:free",
        "nvidia/nemotron-3-super-120b-a12b:free",
    )

    private val PREFERRED_LITE = listOf(
        "openrouter/free",
        "nvidia/nemotron-3.5-lightning:free",
        "google/gemma-4-26b-a4b-it:free",
    )

    fun toFreeModels(catalog: List<OpenRouterModelDto>): List<LlmModel> = catalog
        .filter { it.isFree() }
        .map { LlmModel(id = it.id, supportsTools = it.supportsTools()) }

    fun chain(
        models: List<LlmModel>,
        tier: ModelTier,
        toolsRequired: Boolean,
        contextLengths: Map<String, Long> = emptyMap(),
    ): List<String> {
        val eligible = models.filter { !toolsRequired || it.supportsTools }
        val eligibleIds = eligible.map { it.id }.toSet()

        val preferred = preferenceOrder(tier).filter { it in eligibleIds }
        val remainder = eligible
            .map { it.id }
            .filterNot { it in preferred }
            .sortedByDescending { contextLengths[it] ?: 0L }

        return (preferred + remainder).distinct().take(MAX_CHAIN_LENGTH)
    }

    fun contextLengths(catalog: List<OpenRouterModelDto>): Map<String, Long> =
        catalog.mapNotNull { model -> model.contextLength?.let { model.id to it } }.toMap()

    private fun preferenceOrder(tier: ModelTier): List<String> = when (tier) {
        ModelTier.QUALITY -> PREFERRED_QUALITY
        ModelTier.LITE -> PREFERRED_LITE
    }

    private fun OpenRouterModelDto.isFree(): Boolean {
        val prompt = pricing?.prompt?.toDoubleOrNull() ?: return false
        val completion = pricing.completion?.toDoubleOrNull() ?: return false
        return prompt == 0.0 && completion == 0.0
    }

    private fun OpenRouterModelDto.supportsTools(): Boolean =
        supportedParameters.contains(TOOLS_PARAMETER)
}
