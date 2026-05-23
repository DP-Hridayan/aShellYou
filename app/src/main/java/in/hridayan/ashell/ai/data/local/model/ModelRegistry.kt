package `in`.hridayan.ashell.ai.data.local.model

import `in`.hridayan.ashell.ai.domain.model.AiModel

/**
 * Registry of all supported AI models for command analysis.
 *
 * Models are downloaded on-demand from Hugging Face and stored locally.
 * This registry defines the available models, their download URLs, and metadata.
 *
 * Models are organized by size tier:
 *   - Tiny (< 300 MB): Ultra-fast, basic analysis
 *   - Small (300–500 MB): Best balance for most phones
 *   - Medium (800 MB–1.1 GB): More capable, needs more RAM
 *   - Large (1.5–2.5 GB): Most accurate, for flagship devices
 */
object ModelRegistry {
    /** SmolLM2 360M — Smallest option for devices with limited storage */
    val SMOLLM2_360M_Q4 = AiModel(
        id = "smollm2-360m-instruct-q4_k_m",
        name = "SmolLM2 360M Instruct",
        fileName = "SmolLM2-360M-Instruct-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/SmolLM2-360M-Instruct-GGUF/resolve/main/SmolLM2-360M-Instruct-Q4_K_M.gguf",
        sizeBytes = 250_000_000L,
        description = "Ultra-compact 360M parameter model. Fastest inference, smallest download. Good for basic analysis on low-end devices.",
        quantization = "Q4_K_M",
        parameterCount = "360M",
        isDefault = false,
        isRecommended = false
    )

    /** Qwen 2.5 0.5B — Default and recommended model for command analysis */
    val QWEN_05B_Q4 = AiModel(
        id = "qwen2.5-0.5b-instruct-q4_k_m",
        name = "Qwen 2.5 0.5B Instruct",
        fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
        sizeBytes = 397_000_000L,
        description = "Compact 0.5B parameter model. Excellent balance of size and quality for command analysis on most mobile devices.",
        quantization = "Q4_K_M",
        parameterCount = "0.5B",
        isDefault = true,
        isRecommended = true
    )

    /** Gemma 3 1B IT — Google's efficient instruction-tuned model */
    val GEMMA3_1B_Q4 = AiModel(
        id = "gemma-3-1b-it-q4_k_m",
        name = "Gemma 3 1B IT",
        fileName = "gemma-3-1b-it-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/google_gemma-3-1b-it-GGUF/resolve/main/google_gemma-3-1b-it-Q4_K_M.gguf",
        sizeBytes = 806_000_000L,
        description = "Google's efficient 1B parameter model. Strong reasoning and instruction following with low resource usage.",
        quantization = "Q4_K_M",
        parameterCount = "1B",
        isDefault = false,
        isRecommended = false
    )

    /** Llama 3.2 1B Instruct — Meta's fast and efficient model */
    val LLAMA32_1B_Q4 = AiModel(
        id = "llama-3.2-1b-instruct-q4_k_m",
        name = "Llama 3.2 1B Instruct",
        fileName = "Llama-3.2-1B-Instruct-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
        sizeBytes = 808_000_000L,
        description = "Meta's lightweight 1B model. Extremely fast inference with good general understanding and instruction following.",
        quantization = "Q4_K_M",
        parameterCount = "1B",
        isDefault = false,
        isRecommended = false
    )

    /** Qwen 2.5 Coder 1.5B — Specialized for code understanding */
    val QWEN_CODER_15B_Q4 = AiModel(
        id = "qwen2.5-coder-1.5b-instruct-q4_k_m",
        name = "Qwen 2.5 Coder 1.5B",
        fileName = "qwen2.5-coder-1.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF/resolve/main/qwen2.5-coder-1.5b-instruct-q4_k_m.gguf",
        sizeBytes = 986_000_000L,
        description = "Code-specialized 1.5B model. Best at understanding shell commands, scripts, and technical syntax. Recommended for power users.",
        quantization = "Q4_K_M",
        parameterCount = "1.5B",
        isDefault = false,
        isRecommended = true
    )

    /** Qwen 2.5 1.5B Instruct — General purpose, more capable */
    val QWEN_15B_Q4 = AiModel(
        id = "qwen2.5-1.5b-instruct-q4_k_m",
        name = "Qwen 2.5 1.5B Instruct",
        fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
        sizeBytes = 1_100_000_000L,
        description = "Larger 1.5B parameter model. More detailed and accurate analysis with better reasoning capabilities.",
        quantization = "Q4_K_M",
        parameterCount = "1.5B",
        isDefault = false,
        isRecommended = false
    )

    /** SmolLM3 3B — Strong open-source reasoning model */
    val SMOLLM3_3B_Q4 = AiModel(
        id = "smollm3-3b-q4_k_m",
        name = "SmolLM3 3B",
        fileName = "HuggingFaceTB-SmolLM3-3B-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/HuggingFaceTB_SmolLM3-3B-GGUF/resolve/main/HuggingFaceTB_SmolLM3-3B-Q4_K_M.gguf",
        sizeBytes = 1_920_000_000L,
        description = "Powerful 3B model with excellent reasoning. Best analysis quality for devices with 6+ GB free RAM. Supports 128K context.",
        quantization = "Q4_K_M",
        parameterCount = "3B",
        isDefault = false,
        isRecommended = false
    )

    /** Phi-4 Mini — Microsoft's logic-focused model */
    val PHI4_MINI_Q4 = AiModel(
        id = "phi-4-mini-instruct-q4_k_m",
        name = "Phi-4 Mini 3.8B",
        fileName = "Phi-4-mini-instruct-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/microsoft_Phi-4-mini-instruct-GGUF/resolve/main/microsoft_Phi-4-mini-instruct-Q4_K_M.gguf",
        sizeBytes = 2_490_000_000L,
        description = "Microsoft's 3.8B reasoning model. Top-tier analysis accuracy and logic. Requires flagship device with 8+ GB free RAM.",
        quantization = "Q4_K_M",
        parameterCount = "3.8B",
        isDefault = false,
        isRecommended = false
    )

    /** All available models, ordered by size (smallest first) */
    val allModels = listOf(
        SMOLLM2_360M_Q4,
        QWEN_05B_Q4,
        GEMMA3_1B_Q4,
        LLAMA32_1B_Q4,
        QWEN_CODER_15B_Q4,
        QWEN_15B_Q4,
        SMOLLM3_3B_Q4,
        PHI4_MINI_Q4
    )

    /** Default model ID */
    val defaultModel= QWEN_05B_Q4

    /** Find a model by its ID */
    fun findById(id: String): AiModel? = allModels.find { it.id == id }
}
