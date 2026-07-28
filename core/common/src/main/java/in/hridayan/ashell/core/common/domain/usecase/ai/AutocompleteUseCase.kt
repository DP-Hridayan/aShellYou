package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
import javax.inject.Inject

class AutocompleteUseCase @Inject constructor(
    private val analysisRepository: AiAnalysisRepository
) {
    suspend operator fun invoke(prefix: String): String {
        val trimmed = prefix.trimStart()
        if (trimmed.length < 3) return ""
        
        // We ask for exactly 5 tokens for a quick hint
        val response = analysisRepository.generateRawCompletion(trimmed, maxTokens = 5)
        
        // Clean up any conversational wrappers if the instruct model disobeyed
        var cleaned = response.trim().removePrefix("`").removeSuffix("`")
        if (cleaned.startsWith("suggestion:", ignoreCase = true)) {
            cleaned = cleaned.substringAfter("suggestion:").trim()
        }
        return cleaned
    }
}
