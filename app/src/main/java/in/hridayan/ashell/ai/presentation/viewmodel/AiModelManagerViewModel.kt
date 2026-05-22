package `in`.hridayan.ashell.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.ai.domain.model.AiModel
import `in`.hridayan.ashell.ai.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.ai.domain.repository.AiModelRepository
import `in`.hridayan.ashell.ai.presentation.model.DownloadProgress
import `in`.hridayan.ashell.ai.presentation.model.ModelCardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the AI Model Manager settings screen.
 *
 * Manages model listing, downloading, selection, deletion, and storage display.
 */
@HiltViewModel
class AiModelManagerViewModel @Inject constructor(
    private val modelRepository: AiModelRepository,
    private val analysisRepository: AiAnalysisRepository
) : ViewModel() {

    /** Data class representing a model's full UI state */
    data class ModelUiState(
        val model: AiModel,
        val cardState: ModelCardState,
        val downloadProgress: DownloadProgress = DownloadProgress.Idle,
        val errorMessage: String? = null
    )

    private val _downloadProgresses = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _storageUsage = MutableStateFlow(0L)
    private val _cacheSizeBytes = MutableStateFlow(0L)

    val storageUsage: StateFlow<Long> = _storageUsage.asStateFlow()
    val cacheSizeBytes: StateFlow<Long> = _cacheSizeBytes.asStateFlow()

    /** All models with their current UI state */
    val models: StateFlow<List<ModelUiState>> = combine(
        modelRepository.getInstalledModels(),
        modelRepository.getSelectedModel(),
        _downloadProgresses,
        _errors
    ) { installed, selected, downloads, errors ->
        val installedIds = installed.map { it.id }.toSet()
        val selectedId = selected?.id

        modelRepository.getAvailableModels().map { model ->
            val downloadProgress = downloads[model.id] ?: DownloadProgress.Idle
            val error = errors[model.id]

            val state = when {
                error != null -> ModelCardState.ERROR
                downloadProgress is DownloadProgress.Downloading -> ModelCardState.DOWNLOADING
                model.id == selectedId && model.id in installedIds -> ModelCardState.SELECTED
                model.id in installedIds -> ModelCardState.INSTALLED
                else -> ModelCardState.NOT_INSTALLED
            }

            ModelUiState(
                model = model,
                cardState = state,
                downloadProgress = downloadProgress,
                errorMessage = error
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshStorageUsage()
    }

    /**
     * Download a model.
     */
    fun downloadModel(modelId: String) {
        _errors.value = _errors.value - modelId

        viewModelScope.launch {
            modelRepository.downloadModel(modelId).collect { progress ->
                _downloadProgresses.value = _downloadProgresses.value + (modelId to progress)

                when (progress) {
                    is DownloadProgress.Completed -> {
                        _downloadProgresses.value = _downloadProgresses.value - modelId
                        refreshStorageUsage()
                        // Auto-select if it's the first model
                        val installed = modelRepository.getAvailableModels()
                            .count { modelRepository.isModelInstalled(it.id) }
                        if (installed <= 1) {
                            modelRepository.selectModel(modelId)
                        }
                    }
                    is DownloadProgress.Failed -> {
                        _downloadProgresses.value = _downloadProgresses.value - modelId
                        _errors.value = _errors.value + (modelId to progress.error)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Cancel an ongoing download.
     */
    fun cancelDownload(modelId: String) {
        modelRepository.cancelDownload(modelId)
        _downloadProgresses.value = _downloadProgresses.value - modelId
    }

    /**
     * Delete a downloaded model.
     */
    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            modelRepository.deleteModel(modelId)
            refreshStorageUsage()
        }
    }

    /**
     * Select a model as the active inference model.
     */
    fun selectModel(modelId: String) {
        viewModelScope.launch {
            modelRepository.selectModel(modelId)
        }
    }

    /**
     * Clear the analysis cache.
     */
    fun clearCache() {
        viewModelScope.launch {
            analysisRepository.clearCache()
            refreshCacheSize()
        }
    }

    /**
     * Dismiss an error for a model.
     */
    fun dismissError(modelId: String) {
        _errors.value = _errors.value - modelId
    }

    private fun refreshStorageUsage() {
        viewModelScope.launch {
            _storageUsage.value = modelRepository.getStorageUsage()
        }
    }

    private fun refreshCacheSize() {
        viewModelScope.launch {
            _cacheSizeBytes.value = analysisRepository.getCacheSizeBytes()
        }
    }
}
