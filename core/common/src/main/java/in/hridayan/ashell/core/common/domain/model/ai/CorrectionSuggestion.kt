package `in`.hridayan.ashell.core.common.domain.model.ai

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * A suggested correction for an invalid or partially valid command.
 *
 * @param suggestedCommand The corrected command string
 * @param confidence How confident we are in this correction
 * @param source Where this correction came from
 */
@Immutable
@Serializable
data class CorrectionSuggestion(
    val suggestedCommand: String,
    val confidence: CorrectionConfidence,
    val source: CorrectionSource
)
