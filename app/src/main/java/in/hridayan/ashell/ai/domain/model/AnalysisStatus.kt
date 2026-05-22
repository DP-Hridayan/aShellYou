package `in`.hridayan.ashell.ai.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the analysis status of a command.
 */
@Serializable
enum class AnalysisStatus {
    /** Command is valid and fully recognized */
    VALID,

    /** Command is partially valid — may have typos or missing arguments */
    PARTIAL,

    /** Command is invalid — unrecognized syntax or structure */
    INVALID,

    /** Input is nonsensical — not a shell command at all */
    GIBBERISH
}
