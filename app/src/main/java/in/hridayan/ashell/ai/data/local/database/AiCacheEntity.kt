package `in`.hridayan.ashell.ai.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching AI analysis results.
 * Uses a SHA-256 hash of the normalized command as the primary key
 * to enable fast lookups and deduplication.
 *
 * @param commandHash SHA-256 hash of the normalized (trimmed, lowercase) command
 * @param command The original command string
 * @param analysisJson Serialized [AnalysisResult] as JSON
 * @param modelId Which AI model produced this analysis
 * @param timestamp Unix timestamp in milliseconds when the analysis was cached
 * @param version Schema version for cache invalidation on updates
 */
@Entity(tableName = "ai_analysis_cache")
data class AiCacheEntity(
    @PrimaryKey val commandHash: String,
    val command: String,
    val analysisJson: String,
    val modelId: String,
    val timestamp: Long,
    val version: Int = 1
)
