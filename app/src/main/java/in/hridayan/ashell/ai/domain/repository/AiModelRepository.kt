package `in`.hridayan.ashell.ai.domain.repository

import `in`.hridayan.ashell.ai.domain.model.AiModel
import `in`.hridayan.ashell.ai.presentation.model.DownloadProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for AI model management.
 */
interface AiModelRepository {
    /** Get all models defined in the registry */
    fun getAvailableModels(): List<AiModel>

    /** Get models that are currently installed on disk, as a reactive flow */
    fun getInstalledModels(): Flow<List<AiModel>>

    /** Get the currently selected model, as a reactive flow */
    fun getSelectedModel(): Flow<AiModel?>

    /** Select a model as the active model for inference */
    suspend fun selectModel(modelId: String)

    /** Start downloading a model. Progress is reported via [observeDownloadProgress]. */
    fun downloadModel(modelId: String)

    /** Observe download progress for all active downloads */
    fun observeDownloadProgress(): StateFlow<Map<String, DownloadProgress>>

    /** Observe download errors for all models */
    fun observeDownloadErrors(): StateFlow<Map<String, String>>

    /** Delete a downloaded model from disk */
    suspend fun deleteModel(modelId: String)

    /** Check if a specific model is installed on disk */
    suspend fun isModelInstalled(modelId: String): Boolean

    /** Verify model file integrity (size check) */
    suspend fun verifyModelIntegrity(modelId: String): Boolean

    /** Get total storage used by AI models in bytes */
    suspend fun getStorageUsage(): Long

    /** Get the file path for a model */
    fun getModelPath(modelId: String): String?

    /** Cancel an ongoing download */
    fun cancelDownload(modelId: String)

    /** Dismiss a download error */
    fun dismissError(modelId: String)
}
