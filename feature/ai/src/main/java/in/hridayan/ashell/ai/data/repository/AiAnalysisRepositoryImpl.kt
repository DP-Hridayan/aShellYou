package `in`.hridayan.ashell.ai.data.repository

import android.util.Log
import `in`.hridayan.ashell.ai.data.local.database.AiCacheDao
import `in`.hridayan.ashell.ai.data.local.database.AiCacheEntity
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.CloudAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AiAnalysisRepository] that orchestrates the cloud analysis pipeline.
 *
 * Pipeline: Cache check -> Cloud inference -> Cache result -> Return
 */
@Singleton
class AiAnalysisRepositoryImpl @Inject constructor(
    private val cacheDao: AiCacheDao,
    private val cloudAnalysisRepository: CloudAnalysisRepository,
    private val settingsRepository: SettingsRepository
) : AiAnalysisRepository {

    companion object {
        private const val TAG = "AiAnalysis"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun analyzeCommand(command: String, ragContext: String, fallbackModels: List<String>?): AnalysisResult {
        val normalizedCommand = command.trim()
        if (normalizedCommand.isBlank()) {
            return AnalysisResult.error("Command is empty")
        }

        Log.d(TAG, "analyzeCommand() called for: '$normalizedCommand'")
        val commandHash = computeHash(normalizedCommand)
        val providerId = settingsRepository.getString(SettingsKeys.AiCloudProvider).firstOrNull()
            ?: SettingsKeys.AiCloudProvider.default
        val cacheEnabled =
            settingsRepository.getBoolean(SettingsKeys.AiCacheEnabled).firstOrNull()
                ?: SettingsKeys.AiCacheEnabled.default

        // Clean up expired cache entries
        if (cacheEnabled) {
            val maxCacheAgeDays =
                settingsRepository.getInt(SettingsKeys.AiCacheDays).firstOrNull()
                    ?: SettingsKeys.AiCacheDays.default
            val cutoff =
                System.currentTimeMillis() - (maxCacheAgeDays.toLong() * 24 * 60 * 60 * 1000)
            try {
                cacheDao.deleteOlderThan(cutoff)
            } catch (_: Exception) {
            }
        }

        // 1. Check cache
        if (cacheEnabled) {
            val cached = getCachedResult(commandHash, providerId)
            if (cached != null) {
                Log.d(TAG, "Cache HIT for command hash=$commandHash, provider=$providerId")
                return cached
            }
            Log.d(TAG, "Cache MISS for command hash=$commandHash, provider=$providerId")
        }

        val safeCommand = if (normalizedCommand.length > AiAnalysisRepository.MAX_COMMAND_LENGTH) {
            normalizedCommand.take(AiAnalysisRepository.MAX_COMMAND_LENGTH)
        } else {
            normalizedCommand
        }

        // 2. Run inference via Cloud
        val result = try {
            cloudAnalysisRepository.analyzeCommand(safeCommand, ragContext, fallbackModels)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud analysis threw exception", e)
            return AnalysisResult.error("Analysis failed: ${e.message}")
        }

        // 3. Cache result
        if (cacheEnabled) {
            try {
                val analysisJson = json.encodeToString(result)
                cacheDao.insert(
                    AiCacheEntity(
                        commandHash = commandHash,
                        command = safeCommand,
                        analysisJson = analysisJson,
                        modelId = providerId,
                        timestamp = System.currentTimeMillis()
                    )
                )
                Log.d(TAG, "Result cached successfully")
            } catch (e: Exception) {
                Log.w(TAG, "Cache insert failed (non-fatal)", e)
            }
        }

        return result
    }

    override suspend fun queryCommand(query: String, tools: List<AiTool>, fallbackModels: List<String>?): AnalysisResult {
        return try {
            cloudAnalysisRepository.queryCommand(query, tools, fallbackModels)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud query threw exception", e)
            AnalysisResult.error("Query failed: ${e.message}")
        }
    }

    override suspend fun generateRawCompletion(prompt: String, maxTokens: Int): String {
        return ""
    }

    override suspend fun getCachedAnalysis(command: String): AnalysisResult? {
        val cacheEnabled =
            settingsRepository.getBoolean(SettingsKeys.AiCacheEnabled).firstOrNull()
                ?: SettingsKeys.AiCacheEnabled.default
        if (!cacheEnabled) return null
        val hash = computeHash(command.trim())
        val providerId = settingsRepository.getString(SettingsKeys.AiCloudProvider).firstOrNull()
            ?: SettingsKeys.AiCloudProvider.default
        return getCachedResult(hash, providerId)
    }

    override suspend fun clearCache() {
        cacheDao.deleteAll()
    }

    override suspend fun getCacheSizeBytes(): Long {
        return cacheDao.getCacheSizeBytes()
    }

    private suspend fun getCachedResult(commandHash: String, modelId: String): AnalysisResult? {
        return try {
            val entity = cacheDao.getByCommandHashAndModel(commandHash, modelId) ?: return null
            json.decodeFromString<AnalysisResult>(entity.analysisJson)
        } catch (_: Exception) {
            null
        }
    }

    private fun computeHash(command: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(command.lowercase().toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
