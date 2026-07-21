package `in`.hridayan.ashell.ai.domain.model

/**
 * Represents an AI model available for command analysis.
 *
 * @param id Unique identifier for the model
 * @param name Human-readable display name
 * @param fileName GGUF file name on disk
 * @param downloadUrl Direct download URL (Hugging Face)
 * @param sizeBytes Expected file size in bytes
 * @param description Brief description of the model's capabilities
 * @param quantization Quantization method (e.g., Q4_K_M)
 * @param parameterCount Human-readable parameter count (e.g., "0.5B")
 * @param isDefault Whether this is the default model
 * @param isRecommended Whether this model is recommended for this task
 */
data class AiModel(
    val id: String,
    val name: String,
    val fileName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val description: String,
    val quantization: String,
    val parameterCount: String,
    val isDefault: Boolean = false,
    val isRecommended: Boolean = false
) {
    /** Human-readable file size */
    val formattedSize: String
        get() = when {
            sizeBytes >= 1_073_741_824L -> "%.1f GB".format(sizeBytes / 1_073_741_824.0)
            sizeBytes >= 1_048_576L -> "%.0f MB".format(sizeBytes / 1_048_576.0)
            else -> "%.0f KB".format(sizeBytes / 1024.0)
        }
}
