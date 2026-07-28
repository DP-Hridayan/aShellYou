package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import kotlinx.coroutines.flow.firstOrNull
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
    private val commandRepository: CommandRepository
) {

    /**
     * Analyze a command.
     *
     * @param command The shell/ADB command to analyze
     * @return The analysis result
     * @throws ModelNotInstalledException if no model is installed
     */
    suspend operator fun invoke(command: String, fallbackModels: List<String>? = null): AnalysisResult {
        val trimmedCommand = command.trim()

        if (trimmedCommand.isBlank()) {
            return AnalysisResult.error("Command is empty")
        }

        // Truncate overly long commands to fit within the model's context window.
        // We still analyze the truncated portion rather than rejecting outright,
        // since the beginning of the command is usually the most informative part.
        val safeCommand = if (trimmedCommand.length > AiAnalysisRepository.MAX_COMMAND_LENGTH) {
            trimmedCommand.take(AiAnalysisRepository.MAX_COMMAND_LENGTH)
        } else {
            trimmedCommand
        }

        // 1. Check cache
        val cached = analysisRepository.getCachedAnalysis(safeCommand)
        if (cached != null) return cached

        // 3. Perform RAG Lookup for Context
        val rootCommand = safeCommand.split(" ").firstOrNull() ?: safeCommand
        val searchResults = commandRepository.searchCommands(rootCommand).firstOrNull()
        val ragContext = searchResults?.firstOrNull()?.description ?: ""

        // 4. Run AI analysis
        return analysisRepository.analyzeCommand(safeCommand, ragContext, fallbackModels)
    }
}
