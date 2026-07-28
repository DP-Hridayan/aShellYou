package `in`.hridayan.ashell.core.common.domain.model.ai

import kotlinx.serialization.Serializable

/**
 * Represents the analysis status of a command.
 */
@Serializable
enum class AnalysisStatus {
    /** Command is valid and fully recognized */
    VALID,

    /** Command is partially valid â€” may have typos or missing arguments */
    PARTIAL,

    /** Command is invalid â€” unrecognized syntax or structure */
    INVALID,

    /** Input is nonsensical or completely unrelated */
    GIBBERISH,

    /** Input is a natural language question or request (e.g., "how do I delete a file") */
    NATURAL_LANGUAGE
}
