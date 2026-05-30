package `in`.hridayan.ashell.ai.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.ai.data.local.database.AiCacheDao
import `in`.hridayan.ashell.ai.data.local.database.AiCacheEntity
import `in`.hridayan.ashell.ai.data.local.model.ModelRegistry
import `in`.hridayan.ashell.ai.data.parser.AiResponseParser
import `in`.hridayan.ashell.ai.data.parser.PromptBuilder
import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AiAnalysisRepository] that orchestrates the hybrid analysis pipeline.
 *
 * Pipeline: Cache check → AI inference → Cache result → Return
 *
 * Cache is per-model: switching models produces separate cache entries for the
 * same command, so results from different models don't interfere.
 */
@Singleton
class AiAnalysisRepositoryImpl @Inject constructor(
    private val cacheDao: AiCacheDao,
    private val inferenceEngine: LlamaInferenceEngine,
    private val settingsRepository: SettingsRepository,
    @param:ApplicationContext private val context: Context
) : AiAnalysisRepository {

    companion object {
        private const val TAG = "AiAnalysis"
        private const val MAX_TOKENS = 50
        private const val TEMPERATURE = 0.0f

        /**
         * Maximum character length for commands sent to AI analysis.
         *
         * The context window is 512 tokens. Budget:
         * - System prompt: ~80-100 tokens
         * - ChatML wrapping: ~30 tokens
         * - Generation (MAX_TOKENS): 150 tokens
         * - Available for user command: ~200 tokens ≈ 1000 chars
         *
         * Commands exceeding this are truncated with a notice to the model.
         */
        const val MAX_COMMAND_LENGTH = 2000
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun analyzeCommand(command: String): AnalysisResult {
        val normalizedCommand = command.trim()
        if (normalizedCommand.isBlank()) {
            return AnalysisResult.error("Command is empty")
        }

        Log.d(TAG, "analyzeCommand() called for: '$normalizedCommand'")
        val commandHash = computeHash(normalizedCommand)
        val modelId = settingsRepository.getString(SettingsKeys.SELECTED_MODEL_ID).firstOrNull()
            ?: SettingsKeys.SELECTED_MODEL_ID.default
        val cacheEnabled =
            settingsRepository.getBoolean(SettingsKeys.AI_CACHE_ENABLED).firstOrNull()
                ?: SettingsKeys.AI_CACHE_ENABLED.default

        // Clean up expired cache entries
        if (cacheEnabled) {
            val maxCacheAgeDays =
                settingsRepository.getInt(SettingsKeys.AI_CACHE_DAYS).firstOrNull()
                    ?: SettingsKeys.AI_CACHE_DAYS.default
            val cutoff =
                System.currentTimeMillis() - (maxCacheAgeDays.toLong() * 24 * 60 * 60 * 1000)
            try {
                cacheDao.deleteOlderThan(cutoff)
            } catch (_: Exception) {
            }
        }

        // 1. Check cache
        if (cacheEnabled) {
            val cached = getCachedResult(commandHash, modelId)
            if (cached != null) {
                Log.d(TAG, "Cache HIT for command hash=$commandHash, model=$modelId")
                return cached
            }
            Log.d(TAG, "Cache MISS for command hash=$commandHash, model=$modelId")
        }

        // 2. Check if engine is busy (previous inference still running)
        if (inferenceEngine.isBusy()) {
            Log.w(TAG, "Inference engine is busy, cannot start new analysis")
            return AnalysisResult.error("Analysis is already in progress. Please wait and try again.")
        }

        // 3. Ensure model is loaded
        val model = ModelRegistry.findById(modelId)
            ?: return AnalysisResult.error("Selected model not found").also {
                Log.e(TAG, "Model not found for id=$modelId")
            }

        val modelPath = File(context.filesDir, "ai_models/${model.fileName}").absolutePath
        if (!File(modelPath).exists()) {
            Log.e(TAG, "Model file does not exist: $modelPath")
            return AnalysisResult.error("Model not installed")
        }

        if (!inferenceEngine.isModelLoaded()) {
            Log.d(TAG, "Loading model from $modelPath...")
            val loaded = inferenceEngine.loadModel(modelPath)
            if (!loaded) {
                Log.e(TAG, "Failed to load model from $modelPath")
                return AnalysisResult.error("Failed to load AI model")
            }
            Log.d(TAG, "Model loaded successfully")
        }

        // 3. Run inference
        val systemPrompt = PromptBuilder.buildSystemPrompt()
        val userPrompt = PromptBuilder.buildUserPrompt(normalizedCommand)
        Log.d(TAG, "Running inference with maxTokens=$MAX_TOKENS, temperature=$TEMPERATURE")

        val rawResponse = try {
            inferenceEngine.runInference(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                maxTokens = MAX_TOKENS,
            temperature = TEMPERATURE
            )
        } catch (e: Exception) {
            Log.e(TAG, "Inference threw exception", e)
            return AnalysisResult.error("Inference failed: ${e.message}")
        }

        // 4. Log raw response and parse
        Log.d(TAG, "=== RAW AI MODEL OUTPUT START ===")
        Log.d(TAG, rawResponse)
        Log.d(TAG, "=== RAW AI MODEL OUTPUT END (${rawResponse.length} chars) ===")

        val result = AiResponseParser.parse(rawResponse)
        Log.d(
            TAG,
            "Parsed result: status=${result.status}, description=${result.description.take(100)}"
        )

        // 5. Cache result
        if (cacheEnabled) {
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
                Log.d(TAG, "Result cached successfully")
            } catch (e: Exception) {
                Log.w(TAG, "Cache insert failed (non-fatal)", e)
            }
        }

        return result
    }

    override suspend fun getCachedAnalysis(command: String): AnalysisResult? {
        val cacheEnabled =
            settingsRepository.getBoolean(SettingsKeys.AI_CACHE_ENABLED).firstOrNull()
                ?: SettingsKeys.AI_CACHE_ENABLED.default
        if (!cacheEnabled) return null
        val hash = computeHash(command.trim())
        val modelId = settingsRepository.getString(SettingsKeys.SELECTED_MODEL_ID).firstOrNull()
            ?: SettingsKeys.SELECTED_MODEL_ID.default
        return getCachedResult(hash, modelId)
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
