package `in`.hridayan.ashell.ai.domain.usecase

import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.repository.AiAnalysisRepository
import javax.inject.Inject

/**
 * Retrieves a cached analysis result for a command, if available.
 */
class GetCachedAnalysisUseCase @Inject constructor(
    private val repository: AiAnalysisRepository
) {
    suspend operator fun invoke(command: String): AnalysisResult? {
        return repository.getCachedAnalysis(command)
    }
}
