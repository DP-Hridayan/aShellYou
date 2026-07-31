package `in`.hridayan.ashell.ai.presentation.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the AI Model Manager settings screen (Cloud & Cache settings).
 */
@Stable
@HiltViewModel
class AiModelManagerViewModel @Inject constructor(
    private val analysisRepository: AiAnalysisRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val preferences = settingsRepository.preferences

    fun setInt(key: SettingsKeys<Int>, value: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            settingsRepository.setInt(key, value)
        }
    }

    fun toggleSetting(key: SettingsKeys<Boolean>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            settingsRepository.toggleSetting(key)
        }
    }

    private val _cacheSizeBytes = MutableStateFlow(0L)
    val cacheSizeBytes: StateFlow<Long> = _cacheSizeBytes.asStateFlow()

    init {
        refreshCacheSize()
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

    fun refreshCacheSize() {
        viewModelScope.launch {
            _cacheSizeBytes.value = analysisRepository.getCacheSizeBytes()
        }
    }
}


