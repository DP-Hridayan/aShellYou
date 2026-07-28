package `in`.hridayan.ashell.core.common.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool

/**
 * Repository interface for AI command analysis operations.
 */
interface AiAnalysisRepository {
    /** Analyze a command using the hybrid pipeline (cache â†’ heuristics â†’ AI) */
    suspend fun analyzeCommand(command: String, ragContext: String = "", fallbackModels: List<String>? = null): AnalysisResult

    /** Query the AI with a specific command and set of available tools */
    suspend fun queryCommand(query: String, tools: List<AiTool>, fallbackModels: List<String>? = null): AnalysisResult

    /** Generate raw text completion for smart autocomplete */
    suspend fun generateRawCompletion(prompt: String, maxTokens: Int = 10): String

    /** Check if a cached analysis exists for the given command */
    suspend fun getCachedAnalysis(command: String): AnalysisResult?

    /** Clear all cached analyses */
    suspend fun clearCache()

    /** Get total size of cached analyses in bytes */
    suspend fun getCacheSizeBytes(): Long

    companion object {
        const val MAX_COMMAND_LENGTH = 4000
    }
}
