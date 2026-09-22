package `in`.hridayan.ashell.core.common.domain.model.ai

import kotlinx.serialization.Serializable

/**
 * A single model offered by a provider, together with the capabilities the app cares about.
 *
 * @param id The provider's own model identifier, sent verbatim on the wire.
 * @param supportsTools Whether the model can be given function declarations. Chat and Ask AI
 * require this; command analysis, theme generation and title generation do not.
 */
@Serializable
data class LlmModel(
    val id: String,
    val supportsTools: Boolean,
)

/**
 * How much capability a caller is willing to pay for in latency.
 *
 * [QUALITY] leads with the strongest models, [LITE] with the fastest.
 */
enum class ModelTier {
    QUALITY,
    LITE,
}
