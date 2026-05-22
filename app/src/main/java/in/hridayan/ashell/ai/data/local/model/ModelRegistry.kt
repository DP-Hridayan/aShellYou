package `in`.hridayan.ashell.ai.data.local.model

import `in`.hridayan.ashell.ai.domain.model.AiModel

/**
 * Registry of all supported AI models for command analysis.
 *
 * Models are downloaded on-demand from Hugging Face and stored locally.
 * This registry defines the available models, their download URLs, and metadata.
 */
object ModelRegistry {

    /** Qwen 2.5 0.5B — Default and recommended model for command analysis */
    val QWEN_05B_Q4 = AiModel(
        id = "qwen2.5-0.5b-instruct-q4_k_m",
        name = "Qwen 2.5 0.5B Instruct",
        fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
        sizeBytes = 397_000_000L,
        description = "Compact 0.5B parameter model. Excellent balance of size and quality for command analysis on mobile devices.",
        quantization = "Q4_K_M",
        parameterCount = "0.5B",
        isDefault = true,
        isRecommended = true
    )

    /** SmolLM2 360M — Smallest option for devices with limited storage */
    val SMOLLM2_360M_Q4 = AiModel(
        id = "smollm2-360m-instruct-q4_k_m",
        name = "SmolLM2 360M Instruct",
        fileName = "SmolLM2-360M-Instruct-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/SmolLM2-360M-Instruct-GGUF/resolve/main/SmolLM2-360M-Instruct-Q4_K_M.gguf",
        sizeBytes = 250_000_000L,
        description = "Ultra-compact 360M parameter model. Fastest inference, smallest download. Good for basic analysis.",
        quantization = "Q4_K_M",
        parameterCount = "360M",
        isDefault = false,
        isRecommended = false
    )

    /** Qwen 2.5 1.5B — More capable model for detailed analysis */
    val QWEN_15B_Q4 = AiModel(
        id = "qwen2.5-1.5b-instruct-q4_k_m",
        name = "Qwen 2.5 1.5B Instruct",
        fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
        sizeBytes = 1_100_000_000L,
        description = "Larger 1.5B parameter model. More detailed and accurate analysis, requires more storage and memory.",
        quantization = "Q4_K_M",
        parameterCount = "1.5B",
        isDefault = false,
        isRecommended = false
    )

    /** All available models */
    val allModels = listOf(QWEN_05B_Q4, SMOLLM2_360M_Q4, QWEN_15B_Q4)

    /** Default model ID */
    val defaultModelId = QWEN_05B_Q4.id

    /** Find a model by its ID */
    fun findById(id: String): AiModel? = allModels.find { it.id == id }
}
