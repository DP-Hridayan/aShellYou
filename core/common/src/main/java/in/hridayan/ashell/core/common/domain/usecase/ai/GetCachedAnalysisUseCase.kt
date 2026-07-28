package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
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
