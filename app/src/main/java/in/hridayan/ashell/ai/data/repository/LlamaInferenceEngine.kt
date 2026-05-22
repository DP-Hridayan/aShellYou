package `in`.hridayan.ashell.ai.data.repository

import `in`.hridayan.ashell.ai.native.LlamaCppBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe wrapper around [LlamaCppBridge] that manages model lifecycle.
 *
 * Features:
 * - Lazy model loading (loads on first inference)
 * - Mutex-guarded inference (only one concurrent request)
 * - Automatic model unloading after idle timeout (5 minutes)
 * - Memory-aware loading
 */
@Singleton
class LlamaInferenceEngine @Inject constructor() {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var idleTimeoutJob: Job? = null
    private var currentModelPath: String? = null

    companion object {
        /** Idle timeout before automatically unloading the model */
        private const val IDLE_TIMEOUT_MS = 5L * 60 * 1000 // 5 minutes
    }

    /**
     * Load a GGUF model from the given path.
     * If a different model is already loaded, it will be unloaded first.
     *
     * @param modelPath Absolute path to the .gguf model file
     * @param contextSize Context window size (default: 2048)
     * @return true if the model was loaded successfully
     */
    suspend fun loadModel(modelPath: String, contextSize: Int = 2048): Boolean =
        withContext(Dispatchers.Default) {
            mutex.withLock {
                cancelIdleTimeout()

                // If same model is already loaded, skip
                if (currentModelPath == modelPath && LlamaCppBridge.isModelLoaded()) {
                    resetIdleTimeout()
                    return@withContext true
                }

                // Unload any existing model
                if (LlamaCppBridge.isModelLoaded()) {
                    LlamaCppBridge.unloadModel()
                }

                val success = LlamaCppBridge.loadModel(modelPath, contextSize)
                if (success) {
                    currentModelPath = modelPath
                    resetIdleTimeout()
                } else {
                    currentModelPath = null
                }
                success
            }
        }

    /**
     * Run inference with the currently loaded model.
     * Blocks until inference completes.
     *
     * @param systemPrompt System prompt for the model
     * @param userPrompt User prompt (the command to analyze)
     * @param maxTokens Maximum tokens to generate
     * @param temperature Sampling temperature
     * @return Generated text response
     * @throws IllegalStateException if no model is loaded
     */
    suspend fun runInference(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.1f
    ): String = withContext(Dispatchers.Default) {
        mutex.withLock {
            cancelIdleTimeout()

            if (!LlamaCppBridge.isModelLoaded()) {
                throw IllegalStateException("No model loaded. Call loadModel() first.")
            }

            val result = LlamaCppBridge.runInference(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                maxTokens = maxTokens,
                temperature = temperature
            )

            resetIdleTimeout()
            result
        }
    }

    /**
     * Unload the current model and free native resources.
     */
    fun unloadModel() {
        cancelIdleTimeout()
        if (LlamaCppBridge.isModelLoaded()) {
            LlamaCppBridge.unloadModel()
        }
        currentModelPath = null
    }

    /**
     * Check if a model is currently loaded and ready for inference.
     */
    fun isModelLoaded(): Boolean = LlamaCppBridge.isModelLoaded()

    /**
     * Get the path of the currently loaded model, if any.
     */
    fun getCurrentModelPath(): String? = currentModelPath

    private fun cancelIdleTimeout() {
        idleTimeoutJob?.cancel()
        idleTimeoutJob = null
    }

    private fun resetIdleTimeout() {
        cancelIdleTimeout()
        idleTimeoutJob = scope.launch {
            delay(IDLE_TIMEOUT_MS)
            mutex.withLock {
                if (LlamaCppBridge.isModelLoaded()) {
                    LlamaCppBridge.unloadModel()
                    currentModelPath = null
                }
            }
        }
    }
}
