package `in`.hridayan.ashell.core.common.domain.model.ai

import kotlinx.serialization.Serializable

/**
 * Complete result of analyzing a shell/ADB command.
 *
 * @param status Whether the command is valid, partial, invalid, or gibberish
 * @param description Human-readable description of what the command does
 * @param feedback Additional feedback or notes from the analysis
 */
@Serializable
data class AnalysisResult(
    val command: String = "",
    val status: AnalysisStatus,
    val description: String = "",
    val feedback: String = "",
    val suggestedCorrection: String? = null,
    val autocomplete: String? = null,
    val useCases: List<String> = emptyList(),
    val dangerLevel: DangerLevel = DangerLevel.SAFE
) {
    companion object {
        /** Creates a gibberish result with a friendly message */
        fun gibberish(feedback: String = "") = AnalysisResult(
            status = AnalysisStatus.GIBBERISH,
            description = "This doesn't appear to be a valid shell command.",
            feedback = feedback
        )

        /** Creates an error result */
        fun error(message: String) = AnalysisResult(
            status = AnalysisStatus.INVALID,
            description = message,
            feedback = "Analysis could not be completed."
        )
    }
}
