package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
import javax.inject.Inject

/**
 * Use case for querying the AI with a natural language instruction and having it
 * formulate an ADB command, potentially using device tools.
 */
class QueryCommandUseCase @Inject constructor(
    private val analysisRepository: AiAnalysisRepository,
) {
    /**
     * Query the AI to generate a command based on user request.
     *
     * @param query The natural language user request
     * @param tools The tools the AI is allowed to use
     * @return The generated analysis result
     */
    suspend operator fun invoke(query: String, tools: List<AiTool>, fallbackModels: List<String>? = null): AnalysisResult {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            return AnalysisResult.error("Query is empty")
        }

        // We do not check cache for tools-enabled natural language queries because
        // device state (e.g., installed packages) can change.
        return analysisRepository.queryCommand(trimmedQuery, tools, fallbackModels)
    }
}
