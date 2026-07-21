package `in`.hridayan.ashell.ai.domain.repository

import `in`.hridayan.ashell.ai.domain.model.AnalysisResult

/**
 * Repository interface for AI command analysis operations.
 */
interface AiAnalysisRepository {
    /** Analyze a command using the hybrid pipeline (cache → heuristics → AI) */
    suspend fun analyzeCommand(command: String): AnalysisResult

    /** Check if a cached analysis exists for the given command */
    suspend fun getCachedAnalysis(command: String): AnalysisResult?

    /** Clear all cached analyses */
    suspend fun clearCache()

    /** Get the total cache size in bytes */
    suspend fun getCacheSizeBytes(): Long
}
