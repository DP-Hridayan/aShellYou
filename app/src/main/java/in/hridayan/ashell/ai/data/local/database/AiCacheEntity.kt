package `in`.hridayan.ashell.ai.data.local.database

import androidx.room.Entity
import `in`.hridayan.ashell.ai.domain.model.AnalysisResult

/**
 * Room entity for caching AI analysis results.
 * Uses a composite primary key of (commandHash, modelId) so each model
 * can have its own cached result for the same command.
 *
 * @param commandHash SHA-256 hash of the normalized (trimmed, lowercase) command
 * @param modelId Which AI model produced this analysis
 * @param command The original command string
 * @param analysisJson Serialized [AnalysisResult] as JSON
 * @param timestamp Unix timestamp in milliseconds when the analysis was cached
 * @param version Schema version for cache invalidation on updates
 */
@Entity(
    tableName = "ai_analysis_cache",
    primaryKeys = ["commandHash", "modelId"]
)
data class AiCacheEntity(
    val commandHash: String,
    val modelId: String,
    val command: String,
    val analysisJson: String,
    val timestamp: Long,
    val version: Int = 1
)
