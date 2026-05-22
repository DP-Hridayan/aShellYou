package `in`.hridayan.ashell.ai.domain.model

import kotlinx.serialization.Serializable

/**
 * Confidence level of a correction suggestion.
 */
@Serializable
enum class CorrectionConfidence {
    /** High confidence — usually from exact database matches */
    HIGH,

    /** Medium confidence — from fuzzy matches or heuristics */
    MEDIUM,

    /** Low confidence — AI-generated suggestions */
    LOW
}
