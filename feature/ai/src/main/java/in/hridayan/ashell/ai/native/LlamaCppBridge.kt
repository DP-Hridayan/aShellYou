package `in`.hridayan.ashell.ai.native

import `in`.hridayan.ashell.ai.data.repository.LlamaInferenceEngine

/**
 * JNI bridge to the native llama.cpp inference engine.
 *
 * This object provides the low-level interface to load GGUF models,
 * run inference, and manage model lifecycle. All methods are blocking
 * and should be called from a background thread.
 *
 * Thread safety is NOT provided at this level — use [LlamaInferenceEngine]
 * for thread-safe access.
 */
object LlamaCppBridge {

    init {
        System.loadLibrary("ashell_llama")
    }

    /**
     * Load a GGUF model from the given file path.
     *
     * @param modelPath Absolute path to the .gguf model file
     * @param contextSize Maximum context window size (default: 2048)
     * @return true if the model was loaded successfully
     */
    external fun loadModel(modelPath: String, contextSize: Int = 512): Boolean

    /**
     * Run inference with the loaded model.
     *
     * @param systemPrompt System prompt for the model
     * @param userPrompt User prompt (the command to analyze)
     * @param maxTokens Maximum tokens to generate (default: 512)
     * @param temperature Sampling temperature (default: 0.1 for deterministic output)
     * @return Generated text response
     */
    external fun runInference(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int = 150,
        temperature: Float = 0.1f
    ): String

    /**
     * Unload the current model and free all native resources.
     */
    external fun unloadModel()

    /**
     * Check if a model is currently loaded.
     *
     * @return true if a model is loaded and ready for inference
     */
    external fun isModelLoaded(): Boolean
}
