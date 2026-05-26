package `in`.hridayan.ashell.ai.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ai.data.local.model.ModelRegistry
import `in`.hridayan.ashell.ai.domain.model.AiModel
import `in`.hridayan.ashell.ai.domain.repository.AiModelRepository
import `in`.hridayan.ashell.ai.presentation.model.DownloadProgress
import `in`.hridayan.ashell.ai.service.AiModelDownloadService
import `in`.hridayan.ashell.core.utils.isNetworkAvailable
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AiModelRepository] using standard HTTP connections
 * for model downloads and local file system for storage.
 *
 * Downloads run in a Singleton-scoped CoroutineScope that survives ViewModel
 * destruction, so downloads persist when navigating away from the settings screen.
 * A foreground service is started to keep the process alive in the background.
 */
@Singleton
class AiModelRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : AiModelRepository {

    companion object {
        private const val TAG = "AiModelRepo"
    }

    private val modelsDir: File
        get() = File(context.filesDir, "ai_models").apply { mkdirs() }

    /**
     * Singleton-scoped coroutine scope. Downloads launched here survive
     * ViewModel recreation (e.g. when user navigates away from the screen).
     */
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Active download jobs keyed by modelId, for cancellation */
    private val activeDownloadJobs = ConcurrentHashMap<String, kotlinx.coroutines.Job>()

    /** Shared download progress state — survives ViewModel recreation */
    private val _downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())

    /** Shared error state — survives ViewModel recreation */
    private val _downloadErrors = MutableStateFlow<Map<String, String>>(emptyMap())

    override fun getAvailableModels(): List<AiModel> = ModelRegistry.allModels

    override fun getInstalledModels(): Flow<List<AiModel>> = flow {
        while (true) {
            val installed = ModelRegistry.allModels.filter { model ->
                File(modelsDir, model.fileName).exists()
            }
            emit(installed)
            delay(2000)
        }
    }.flowOn(Dispatchers.IO)

    override fun getSelectedModel(): Flow<AiModel?> =
        settingsRepository.getString(SettingsKeys.SELECTED_MODEL_ID).map { id ->
            ModelRegistry.findById(id)
        }

    override suspend fun selectModel(modelId: String) {
        settingsRepository.setString(SettingsKeys.SELECTED_MODEL_ID, modelId)
    }

    override fun observeDownloadProgress(): StateFlow<Map<String, DownloadProgress>> =
        _downloadProgress.asStateFlow()

    override fun observeDownloadErrors(): StateFlow<Map<String, String>> =
        _downloadErrors.asStateFlow()

    override fun downloadModel(modelId: String) {
        Log.d(TAG, "downloadModel() called for modelId=$modelId")

        // Clear any previous error for this model
        _downloadErrors.value -= modelId

        // Don't start if already downloading
        if (activeDownloadJobs.containsKey(modelId)) {
            Log.w(TAG, "Download already in progress for $modelId")
            return
        }

        val model = ModelRegistry.findById(modelId)
        if (model == null) {
            Log.e(TAG, "Model not found for id=$modelId")
            _downloadErrors.value += (modelId to "Model not found")
            return
        }

        // Check network connection first
        if (!isNetworkAvailable(context)) {
            val errorMessage = context.getString(R.string.network_error)
            _downloadErrors.value += (modelId to errorMessage)
            showToast(context, errorMessage)
            return
        }

        // Start foreground service to keep download alive in background
        AiModelDownloadService.start(context)

        val job = downloadScope.launch {
            try {
                executeDownload(model)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Download cancelled for ${model.id}")
                    _downloadProgress.value -= model.id
                } else {
                    Log.e(TAG, "Download failed for ${model.id}", e)
                    _downloadProgress.value -= model.id

                    val errorMessage = when (e) {
                        is SocketTimeoutException -> context.getString(R.string.request_timeout)
                        is UnknownHostException,
                        is ConnectException,
                        is UnresolvedAddressException -> context.getString(R.string.network_error)

                        is IOException -> e.message ?: context.getString(R.string.network_error)
                        else -> e.message ?: context.getString(R.string.unknown_error)
                    }
                    _downloadErrors.value += (model.id to errorMessage)

                    withContext(Dispatchers.Main) {
                        showToast(context, errorMessage)
                    }
                }
            } finally {
                activeDownloadJobs.remove(model.id)
                // Stop service if no more active downloads
                if (activeDownloadJobs.isEmpty()) {
                    AiModelDownloadService.stop(context)
                }
            }
        }

        activeDownloadJobs[modelId] = job
    }

    private suspend fun executeDownload(model: AiModel) {
        Log.d(TAG, "Starting download: ${model.name}, url=${model.downloadUrl}")

        val targetFile = File(modelsDir, model.fileName)
        if (targetFile.exists()) {
            targetFile.delete()
            Log.d(TAG, "Deleted existing target file")
        }

        _downloadProgress.value += (model.id to DownloadProgress.Downloading(0, model.sizeBytes))

        val tempFile = File(modelsDir, "${model.fileName}.tmp")
        if (tempFile.exists()) tempFile.delete()

        try {
            val url = URL(model.downloadUrl)
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.instanceFollowRedirects = true

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.e(TAG, "Server returned response code $responseCode")
                _downloadProgress.value -= model.id
                _downloadErrors.value += (model.id to "Server returned $responseCode")
                return
            }

            val contentLength = connection.contentLengthLong
            Log.d(TAG, "Content length: $contentLength")

            val inputStream = BufferedInputStream(connection.inputStream)
            val outputStream = withContext(Dispatchers.IO) {
                FileOutputStream(tempFile)
            }

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

                        val now = System.currentTimeMillis()
                        if (now - lastProgressUpdate > 100) {
                            val total = if (contentLength > 0) contentLength else model.sizeBytes
                            _downloadProgress.value += (model.id to DownloadProgress.Downloading(
                                bytesRead,
                                total
                            ))
                            lastProgressUpdate = now
                        }

                        bytes = input.read(buffer)
                    }
                }
            }

            currentCoroutineContext().ensureActive()

            if (tempFile.renameTo(targetFile)) {
                Log.d(TAG, "Download complete: ${model.name}")
                _downloadProgress.value += (model.id to DownloadProgress.Completed)

                // Auto-select if it's the first installed model
                val installedCount = ModelRegistry.allModels.count { m ->
                    File(modelsDir, m.fileName).exists()
                }
                if (installedCount <= 1) {
                    selectModel(model.id)
                }

                // Clear completed status after a short delay
                delay(2000)
                _downloadProgress.value -= model.id
            } else {
                Log.e(TAG, "Failed to rename temp file")
                _downloadProgress.value -= model.id
                _downloadErrors.value += (model.id to "Failed to install model file")
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            if (tempFile.exists()) tempFile.delete()
            throw e
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            throw e
        }
    }

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
        _downloadProgress.value -= modelId

        val model = ModelRegistry.findById(modelId) ?: return
        val tempFile = File(modelsDir, "${model.fileName}.tmp")
        if (tempFile.exists()) tempFile.delete()
        val targetFile = File(modelsDir, model.fileName)
        if (targetFile.exists()) targetFile.delete()

        if (activeDownloadJobs.isEmpty()) {
            AiModelDownloadService.stop(context)
        }
    }

    override fun dismissError(modelId: String) {
        _downloadErrors.value -= modelId
    }
}
