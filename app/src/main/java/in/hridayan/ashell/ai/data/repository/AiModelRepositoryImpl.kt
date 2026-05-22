package `in`.hridayan.ashell.ai.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.ai.data.local.model.ModelRegistry
import `in`.hridayan.ashell.ai.data.local.preferences.AiPreferencesManager
import `in`.hridayan.ashell.ai.domain.model.AiModel
import `in`.hridayan.ashell.ai.domain.repository.AiModelRepository
import `in`.hridayan.ashell.ai.presentation.model.DownloadProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AiModelRepository] using standard HTTP connections
 * for model downloads and local file system for storage.
 */
@Singleton
class AiModelRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferencesManager: AiPreferencesManager
) : AiModelRepository {

    companion object {
        private const val TAG = "AiModelRepo"
    }

    private val modelsDir: File
        get() = File(context.filesDir, "ai_models").apply { mkdirs() }

    private val activeDownloadJobs = ConcurrentHashMap<String, Job>() // modelId -> Job

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
        Log.d(TAG, "downloadModel() called for modelId=$modelId")

        val model = ModelRegistry.findById(modelId)
            ?: run {
                Log.e(TAG, "Model not found for id=$modelId")
                emit(DownloadProgress.Failed("Model not found"))
                return@flow
            }

        Log.d(TAG, "Model found: ${model.name}, url=${model.downloadUrl}")

        val targetFile = File(modelsDir, model.fileName)
        Log.d(TAG, "Target file: ${targetFile.absolutePath}")

        // Remove target file if exists
        if (targetFile.exists()) {
            targetFile.delete()
            Log.d(TAG, "Deleted existing target file")
        }

        emit(DownloadProgress.Downloading(0, model.sizeBytes))

        val tempFile = File(modelsDir, "${model.fileName}.tmp")
        Log.d(TAG, "Temp file: ${tempFile.absolutePath}")
        if (tempFile.exists()) tempFile.delete()

        // Register the active coroutine job
        val currentJob = currentCoroutineContext()[Job]
        if (currentJob != null) {
            activeDownloadJobs[modelId] = currentJob
        }

        try {
            Log.d(TAG, "Starting HTTP download from URL: ${model.downloadUrl}")
            val url = URL(model.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.instanceFollowRedirects = true

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.e(TAG, "Server returned response code $responseCode")
                emit(DownloadProgress.Failed("Server returned response code $responseCode"))
                return@flow
            }

            val contentLength = connection.contentLengthLong
            Log.d(TAG, "Content length from server: $contentLength")

            val inputStream = BufferedInputStream(connection.inputStream)
            val outputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(8192)
            var bytesRead = 0L
            var lastProgressUpdate = 0L

            inputStream.use { input ->
                outputStream.use { output ->
                    var bytes = input.read(buffer)
                    while (bytes != -1) {
                        currentCoroutineContext().ensureActive()

                        output.write(buffer, 0, bytes)
                        bytesRead += bytes

                        // Throttle progress updates to 100ms to avoid overloading the flow
                        val now = System.currentTimeMillis()
                        if (now - lastProgressUpdate > 100) {
                            val total = if (contentLength > 0) contentLength else model.sizeBytes
                            emit(DownloadProgress.Downloading(bytesRead, total))
                            lastProgressUpdate = now
                        }

                        bytes = input.read(buffer)
                    }
                }
            }

            currentCoroutineContext().ensureActive()

            Log.d(TAG, "Download finished. Renaming temp file to target file...")
            if (tempFile.renameTo(targetFile)) {
                Log.d(TAG, "Rename successful. Model installed.")
                emit(DownloadProgress.Completed)
            } else {
                Log.e(TAG, "Failed to rename temp file to target file")
                emit(DownloadProgress.Failed("Failed to install model file"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "HTTP download failed", e)
            if (tempFile.exists()) tempFile.delete()
            emit(DownloadProgress.Failed("Download failed: ${e.message}"))
        } finally {
            activeDownloadJobs.remove(modelId)
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
        Log.d(TAG, "cancelDownload() called for modelId=$modelId")
        activeDownloadJobs[modelId]?.cancel()
        activeDownloadJobs.remove(modelId)

        // Clean up temp file
        val model = ModelRegistry.findById(modelId) ?: return
        val tempFile = File(modelsDir, "${model.fileName}.tmp")
        if (tempFile.exists()) tempFile.delete()
        val targetFile = File(modelsDir, model.fileName)
        if (targetFile.exists()) targetFile.delete()
    }
}
