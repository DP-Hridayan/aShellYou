package `in`.hridayan.ashell.ai.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import `in`.hridayan.ashell.ai.data.local.model.ModelRegistry
import `in`.hridayan.ashell.ai.data.local.preferences.AiPreferencesManager
import `in`.hridayan.ashell.ai.domain.model.AiModel
import `in`.hridayan.ashell.ai.domain.repository.AiModelRepository
import `in`.hridayan.ashell.ai.presentation.model.DownloadProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AiModelRepository] using Android's DownloadManager
 * for model downloads and local file system for storage.
 */
@Singleton
class AiModelRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: AiPreferencesManager
) : AiModelRepository {

    companion object {
        private const val TAG = "AiModelRepo"
    }

    private val modelsDir: File
        get() = File(context.filesDir, "ai_models").apply { mkdirs() }

    private val activeDownloads = mutableMapOf<String, Long>() // modelId -> downloadId

    override fun getAvailableModels(): List<AiModel> = ModelRegistry.allModels

    override fun getInstalledModels(): Flow<List<AiModel>> = flow {
        while (true) {
            val installed = ModelRegistry.allModels.filter { model ->
                File(modelsDir, model.fileName).exists()
            }
            emit(installed)
            delay(2000) // Refresh every 2 seconds
        }
    }.flowOn(Dispatchers.IO)

    override fun getSelectedModel(): Flow<AiModel?> =
        preferencesManager.selectedModelId.map { id ->
            ModelRegistry.findById(id)
        }

    override suspend fun selectModel(modelId: String) {
        preferencesManager.setSelectedModelId(modelId)
    }

    override fun downloadModel(modelId: String): Flow<DownloadProgress> = flow {
        android.util.Log.d(TAG, "downloadModel() called for modelId=$modelId")

        val model = ModelRegistry.findById(modelId)
            ?: run {
                android.util.Log.e(TAG, "Model not found for id=$modelId")
                emit(DownloadProgress.Failed("Model not found"))
                return@flow
            }

        android.util.Log.d(TAG, "Model found: ${model.name}, url=${model.downloadUrl}")

        val targetFile = File(modelsDir, model.fileName)
        android.util.Log.d(TAG, "Target file: ${targetFile.absolutePath}")

        // Remove partial download if exists
        if (targetFile.exists()) {
            targetFile.delete()
            android.util.Log.d(TAG, "Deleted existing target file")
        }

        emit(DownloadProgress.Downloading(0, model.sizeBytes))

        // DownloadManager cannot write to app-private internal storage.
        // Download to external cache dir first, then move on completion.
        val externalCache = context.externalCacheDir
        android.util.Log.d(TAG, "externalCacheDir=$externalCache")
        val tempDir = File(externalCache ?: context.cacheDir, "ai_downloads").apply { mkdirs() }
        val tempFile = File(tempDir, model.fileName)
        android.util.Log.d(TAG, "Temp file: ${tempFile.absolutePath}, tempDir exists=${tempDir.exists()}")
        if (tempFile.exists()) tempFile.delete()

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val request = DownloadManager.Request(Uri.parse(model.downloadUrl)).apply {
                setTitle("Downloading ${model.name}")
                setDescription("AI model for command analysis")
                setDestinationUri(Uri.fromFile(tempFile))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(false)
            }

            android.util.Log.d(TAG, "Enqueuing download request...")
            val downloadId = downloadManager.enqueue(request)
            android.util.Log.d(TAG, "Download enqueued with id=$downloadId")
            activeDownloads[modelId] = downloadId

            // Poll download progress
            var isComplete = false
            while (!isComplete) {
                delay(500)

                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor == null || !cursor.moveToFirst()) {
                    cursor?.close()
                    android.util.Log.e(TAG, "Download query returned null/empty cursor")
                    emit(DownloadProgress.Failed("Download query failed"))
                    return@flow
                }

                val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val bytesIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)

                val status = if (statusIdx >= 0) cursor.getInt(statusIdx) else -1
                val bytesDownloaded = if (bytesIdx >= 0) cursor.getLong(bytesIdx) else 0
                val totalBytes = if (totalIdx >= 0) cursor.getLong(totalIdx) else model.sizeBytes
                val reason = if (reasonIdx >= 0) cursor.getInt(reasonIdx) else -1

                cursor.close()

                android.util.Log.d(TAG, "Download status=$status, bytes=$bytesDownloaded/$totalBytes, reason=$reason")

                when (status) {
                    DownloadManager.STATUS_RUNNING -> {
                        emit(DownloadProgress.Downloading(bytesDownloaded, totalBytes))
                    }

                    DownloadManager.STATUS_SUCCESSFUL -> {
                        isComplete = true
                        android.util.Log.d(TAG, "Download successful, moving file...")
                        // Move from external cache to internal app storage
                        try {
                            tempFile.copyTo(targetFile, overwrite = true)
                            tempFile.delete()
                            android.util.Log.d(TAG, "File moved to ${targetFile.absolutePath}, size=${targetFile.length()}")
                            emit(DownloadProgress.Completed)
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Failed to move model", e)
                            emit(DownloadProgress.Failed("Failed to move model: ${e.message}"))
                        }
                    }

                    DownloadManager.STATUS_FAILED -> {
                        isComplete = true
                        android.util.Log.e(TAG, "Download FAILED, reason=$reason")
                        tempFile.delete()
                        emit(DownloadProgress.Failed("Download failed (reason=$reason)"))
                    }

                    DownloadManager.STATUS_PAUSED -> {
                        android.util.Log.d(TAG, "Download PAUSED, reason=$reason")
                        emit(DownloadProgress.Downloading(bytesDownloaded, totalBytes))
                    }

                    DownloadManager.STATUS_PENDING -> {
                        android.util.Log.d(TAG, "Download PENDING")
                        emit(DownloadProgress.Downloading(0, totalBytes))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Download exception", e)
            emit(DownloadProgress.Failed("Download error: ${e.message}"))
        } finally {
            activeDownloads.remove(modelId)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteModel(modelId: String) = withContext(Dispatchers.IO) {
        val model = ModelRegistry.findById(modelId) ?: return@withContext
        val file = File(modelsDir, model.fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    override suspend fun isModelInstalled(modelId: String): Boolean = withContext(Dispatchers.IO) {
        val model = ModelRegistry.findById(modelId) ?: return@withContext false
        File(modelsDir, model.fileName).exists()
    }

    override suspend fun verifyModelIntegrity(modelId: String): Boolean =
        withContext(Dispatchers.IO) {
            val model = ModelRegistry.findById(modelId) ?: return@withContext false
            val file = File(modelsDir, model.fileName)
            if (!file.exists()) return@withContext false

            // Basic size check (within 5% tolerance for different quantizations)
            val sizeDiff = kotlin.math.abs(file.length() - model.sizeBytes)
            val tolerance = model.sizeBytes * 0.05
            sizeDiff <= tolerance
        }

    override suspend fun getStorageUsage(): Long = withContext(Dispatchers.IO) {
        if (!modelsDir.exists()) return@withContext 0L
        modelsDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    override fun getModelPath(modelId: String): String? {
        val model = ModelRegistry.findById(modelId) ?: return null
        val file = File(modelsDir, model.fileName)
        return if (file.exists()) file.absolutePath else null
    }

    override fun cancelDownload(modelId: String) {
        val downloadId = activeDownloads[modelId] ?: return
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.remove(downloadId)
        activeDownloads.remove(modelId)

        // Clean up partial file
        val model = ModelRegistry.findById(modelId) ?: return
        val file = File(modelsDir, model.fileName)
        if (file.exists()) file.delete()
    }
}
