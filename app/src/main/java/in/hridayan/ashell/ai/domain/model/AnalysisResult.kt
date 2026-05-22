package `in`.hridayan.ashell.ai.domain.model

import kotlinx.serialization.Serializable

/**
 * Complete result of analyzing a shell/ADB command.
 *
 * @param status Whether the command is valid, partial, invalid, or gibberish
 * @param description Human-readable description of what the command does
 * @param dangerLevel How dangerous the command is
 * @param requiresRoot Whether the command requires root/superuser access
 * @param reversible Whether the command's effects can be undone
 * @param examples Example usages of the command
 * @param warnings Important warnings about the command
 * @param useCases Common use cases for the command
 * @param corrections Suggested corrections for invalid/partial commands
 * @param feedback Additional feedback or notes from the analysis
 */
@Serializable
data class AnalysisResult(
    val status: AnalysisStatus,
    val description: String = "",
    val dangerLevel: DangerLevel = DangerLevel.SAFE,
    val requiresRoot: Boolean = false,
    val reversible: Boolean = true,
    val examples: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val useCases: List<String> = emptyList(),
    val corrections: List<CorrectionSuggestion> = emptyList(),
    val feedback: String = ""
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
