package `in`.hridayan.ashell.ai.domain.usecase

import `in`.hridayan.ashell.ai.data.repository.AiAnalysisRepositoryImpl
import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.ai.domain.repository.AiModelRepository
import javax.inject.Inject

/**
 * Main use case for analyzing a shell/ADB command.
 *
 * Orchestrates the full analysis pipeline:
 * 1. Cache check
 * 2. Model availability check
 * 3. AI analysis
 */
class AnalyzeCommandUseCase @Inject constructor(
    private val analysisRepository: AiAnalysisRepository,
    private val modelRepository: AiModelRepository
) {
    /** Exception thrown when no AI model is installed */
    class ModelNotInstalledException : Exception("No AI model installed")

    /**
     * Analyze a command.
     *
     * @param command The shell/ADB command to analyze
     * @return The analysis result
     * @throws ModelNotInstalledException if no model is installed
     */
    suspend operator fun invoke(command: String): AnalysisResult {
        val trimmedCommand = command.trim()

        if (trimmedCommand.isBlank()) {
            return AnalysisResult.error("Command is empty")
        }

        // Truncate overly long commands to fit within the model's context window.
        // We still analyze the truncated portion rather than rejecting outright,
        // since the beginning of the command is usually the most informative part.
        val safeCommand = if (trimmedCommand.length > AiAnalysisRepositoryImpl.MAX_COMMAND_LENGTH) {
            trimmedCommand.take(AiAnalysisRepositoryImpl.MAX_COMMAND_LENGTH)
        } else {
            trimmedCommand
        }

        // 1. Check cache
        val cached = analysisRepository.getCachedAnalysis(safeCommand)
        if (cached != null) return cached

        // 2. Check if any model is installed
        val selectedModel = modelRepository.getAvailableModels()
            .firstOrNull { modelRepository.isModelInstalled(it.id) }

        if (selectedModel == null) {
            throw ModelNotInstalledException()
        }

        // 3. Run AI analysis
        return analysisRepository.analyzeCommand(safeCommand)
    }
}
