package `in`.hridayan.ashell.ai.data.repository

import android.util.Log
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
 * - Non-blocking busy checks to prevent ANR when inference is in progress
 * - Automatic model unloading after idle timeout (5 minutes)
 * - Memory-aware loading
 *
 * **Important**: All JNI calls to [LlamaCppBridge] acquire a native C++ mutex (`g_mutex`).
 * To prevent ANR, callers MUST check [isBusy] before calling any method that touches JNI.
 * Methods in this class are designed to fail-fast when the engine is busy.
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
        private const val TAG = "LlamaEngine"
    }

    /**
     * Whether the engine is currently busy (inference or model loading in progress).
     *
     * When true, ALL JNI calls will block on the native mutex, so callers
     * must check this and bail early to avoid ANR.
     */
    fun isBusy(): Boolean = mutex.isLocked

    /**
     * Load a GGUF model from the given path.
     * If a different model is already loaded, it will be unloaded first.
     *
     * @param modelPath Absolute path to the .gguf model file
     * @param contextSize Context window size (default: 2048)
     * @return true if the model was loaded successfully
     * @throws IllegalStateException if the engine is busy with another operation
     */
    suspend fun loadModel(modelPath: String, contextSize: Int = 512): Boolean =
        withContext(Dispatchers.Default) {
            if (!mutex.tryLock()) {
                throw IllegalStateException("Engine is busy. Please wait and try again.")
            }
            try {
                cancelIdleTimeout()

                // If same model is already loaded, skip
                if (currentModelPath == modelPath && LlamaCppBridge.isModelLoaded()) {
                    Log.d(TAG, "Model already loaded at $modelPath, reusing")
                    resetIdleTimeout()
                    return@withContext true
                }

                // Unload any existing model
                if (LlamaCppBridge.isModelLoaded()) {
                    Log.d(TAG, "Unloading previous model before loading new one")
                    LlamaCppBridge.unloadModel()
                }

                Log.d(TAG, "Loading model: $modelPath (contextSize=$contextSize)")
                val startTime = System.currentTimeMillis()
                val success = LlamaCppBridge.loadModel(modelPath, contextSize)
                val elapsed = System.currentTimeMillis() - startTime

                if (success) {
                    currentModelPath = modelPath
                    Log.d(TAG, "Model loaded successfully in ${elapsed}ms")
                    resetIdleTimeout()
                } else {
                    currentModelPath = null
                    Log.e(TAG, "Model loading FAILED after ${elapsed}ms")
                }
                success
            } finally {
                mutex.unlock()
            }
        }

    /**
     * Run inference with the currently loaded model.
     *
     * If a previous inference is still running (mutex held), this throws
     * immediately instead of blocking, preventing ANR.
     *
     * @param systemPrompt System prompt for the model
     * @param userPrompt User prompt (the command to analyze)
     * @param maxTokens Maximum tokens to generate
     * @param temperature Sampling temperature
     * @return Generated text response
     * @throws IllegalStateException if no model is loaded or inference is already running
     */
    suspend fun runInference(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int = 150,
        temperature: Float = 0.1f
    ): String = withContext(Dispatchers.Default) {
        // Use tryLock instead of withLock to avoid blocking indefinitely
        // when a previous JNI inference call is still in progress.
        if (!mutex.tryLock()) {
            throw IllegalStateException("Another inference is already running. Please wait and try again.")
        }
        try {
            cancelIdleTimeout()

            if (!LlamaCppBridge.isModelLoaded()) {
                throw IllegalStateException("No model loaded. Call loadModel() first.")
            }

            Log.d(TAG, "Running inference: maxTokens=$maxTokens, temp=$temperature")
            Log.d(TAG, "System prompt length: ${systemPrompt.length}, User prompt: '$userPrompt'")
            val startTime = System.currentTimeMillis()

            val result = LlamaCppBridge.runInference(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                maxTokens = maxTokens,
                temperature = temperature
            )

            val elapsed = System.currentTimeMillis() - startTime
            Log.d(TAG, "Inference completed in ${elapsed}ms, output length=${result.length} chars")
            Log.d(TAG, "Inference output preview: ${result.take(200)}")

            resetIdleTimeout()
            result
        } finally {
            mutex.unlock()
        }
    }

    /**
     * Unload the current model and free native resources.
     * No-op if the engine is busy (to avoid blocking on native mutex).
     */
    fun unloadModel() {
        if (isBusy()) {
            Log.w(TAG, "Cannot unload model while engine is busy")
            return
        }
        cancelIdleTimeout()
        if (LlamaCppBridge.isModelLoaded()) {
            LlamaCppBridge.unloadModel()
        }
        currentModelPath = null
    }

    /**
     * Check if a model is currently loaded and ready for inference.
     *
     * Uses Kotlin-level state ([currentModelPath]) instead of calling JNI,
     * so this method is safe to call even while the engine is busy.
     * The [currentModelPath] is always kept in sync with native state.
     */
    fun isModelLoaded(): Boolean = currentModelPath != null

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
            // Use tryLock to avoid blocking if inference started during the delay
            if (mutex.tryLock()) {
                try {
                    if (LlamaCppBridge.isModelLoaded()) {
                        LlamaCppBridge.unloadModel()
                        currentModelPath = null
                    }
                } finally {
                    mutex.unlock()
                }
            }
        }
    }
}
