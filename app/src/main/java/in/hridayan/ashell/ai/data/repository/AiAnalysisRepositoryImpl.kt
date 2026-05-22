package `in`.hridayan.ashell.ai.data.repository

import android.content.Context
import `in`.hridayan.ashell.ai.data.local.database.AiCacheDao
import `in`.hridayan.ashell.ai.data.local.database.AiCacheEntity
import `in`.hridayan.ashell.ai.data.local.model.ModelRegistry
import `in`.hridayan.ashell.ai.data.local.preferences.AiPreferencesManager
import `in`.hridayan.ashell.ai.data.parser.AiResponseParser
import `in`.hridayan.ashell.ai.data.parser.PromptBuilder
import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.repository.AiAnalysisRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AiAnalysisRepository] that orchestrates the hybrid analysis pipeline.
 *
 * Pipeline: Cache check → AI inference → Cache result → Return
 */
@Singleton
class AiAnalysisRepositoryImpl @Inject constructor(
    private val cacheDao: AiCacheDao,
    private val inferenceEngine: LlamaInferenceEngine,
    private val preferencesManager: AiPreferencesManager,
    @ApplicationContext private val context: Context
) : AiAnalysisRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun analyzeCommand(command: String): AnalysisResult {
        val normalizedCommand = command.trim()
        if (normalizedCommand.isBlank()) {
            return AnalysisResult.error("Command is empty")
        }

        val commandHash = computeHash(normalizedCommand)

        // 1. Check cache
        val cached = getCachedResult(commandHash)
        if (cached != null) return cached

        // 2. Ensure model is loaded
        val modelId = preferencesManager.selectedModelId.firstOrNull() ?: ModelRegistry.defaultModelId
        val model = ModelRegistry.findById(modelId)
            ?: return AnalysisResult.error("Selected model not found")

        val modelPath = File(context.filesDir, "ai_models/${model.fileName}").absolutePath
        if (!File(modelPath).exists()) {
            return AnalysisResult.error("Model not installed")
        }

        if (!inferenceEngine.isModelLoaded()) {
            val loaded = inferenceEngine.loadModel(modelPath)
            if (!loaded) {
                return AnalysisResult.error("Failed to load AI model")
            }
        }

        // 3. Run inference
        val systemPrompt = PromptBuilder.buildSystemPrompt()
        val userPrompt = PromptBuilder.buildUserPrompt(normalizedCommand)

        val rawResponse = try {
            inferenceEngine.runInference(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                maxTokens = 512,
                temperature = 0.1f
            )
        } catch (e: Exception) {
            return AnalysisResult.error("Inference failed: ${e.message}")
        }

        // 4. Parse response
        val result = AiResponseParser.parse(rawResponse)

        // 5. Cache result
        try {
            val analysisJson = json.encodeToString(result)
            cacheDao.insert(
                AiCacheEntity(
                    commandHash = commandHash,
                    command = normalizedCommand,
                    analysisJson = analysisJson,
                    modelId = modelId,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {
            // Cache failure should not prevent returning the result
        }

        return result
    }

    override suspend fun getCachedAnalysis(command: String): AnalysisResult? {
        val hash = computeHash(command.trim())
        return getCachedResult(hash)
    }

    override suspend fun clearCache() {
        cacheDao.deleteAll()
    }

    override suspend fun getCacheSizeBytes(): Long {
        return cacheDao.getCacheSizeBytes()
    }

    private suspend fun getCachedResult(commandHash: String): AnalysisResult? {
        return try {
            val entity = cacheDao.getByCommandHash(commandHash) ?: return null
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
