package `in`.hridayan.ashell.core.common.domain.provider

import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier

/**
 * Resolves the ordered list of model identifiers a caller should try for a given provider.
 *
 * The returned list is a fallback chain, best candidate first. Callers walk it through
 * [in.hridayan.ashell.core.common.domain.usecase.ai.ModelFallbackExecutor].
 *
 * This is a suspending contract because some providers publish their catalog over the network
 * rather than shipping it in the app.
 */
interface LlmModelCatalog {

    /**
     * @param toolsRequired When true, only models that accept function declarations are returned.
     * Chat and Ask AI require this; command analysis, theme generation and title generation do not.
     * @return Model identifiers in preference order, possibly empty if the provider currently
     * offers nothing matching.
     */
    suspend fun chain(
        provider: LlmProvider,
        tier: ModelTier,
        toolsRequired: Boolean,
    ): List<String>
}
