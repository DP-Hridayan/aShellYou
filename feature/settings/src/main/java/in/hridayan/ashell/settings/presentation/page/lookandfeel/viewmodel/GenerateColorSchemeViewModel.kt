package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.constants.AiModelConstants
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.theme.data.ColorSchemePayload
import `in`.hridayan.ashell.core.presentation.theme.data.CustomColorSchemeDao
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toDomain
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toEntity
import `in`.hridayan.ashell.core.presentation.theme.util.ColorSchemeImportHolder
import `in`.hridayan.ashell.settings.domain.usecase.GenerateCustomThemeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenerateColorSchemeViewModel @Inject constructor(
    private val customColorSchemeDao: CustomColorSchemeDao,
    private val settingsRepository: SettingsRepository,
    private val generateCustomThemeUseCase: GenerateCustomThemeUseCase,
    private val colorSchemeImportHolder: ColorSchemeImportHolder,
    private val apiKeyRepository: ApiKeyRepository
) : ViewModel() {

    private val _showApiKeyRequiredDialog = MutableStateFlow(false)
    val showApiKeyRequiredDialog = _showApiKeyRequiredDialog.asStateFlow()

    fun dismissApiKeyRequiredDialog() {
        _showApiKeyRequiredDialog.value = false
    }

    init {
        viewModelScope.launch {
            colorSchemeImportHolder.importedTheme.collect { imported ->
                imported?.let {
                    _previewPayload.value = it
                }
            }
        }
    }

    val savedColorSchemes = customColorSchemeDao.getAllColorSchemes()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val appliedThemeId = settingsRepository.getInt(SettingsKeys.AppliedCustomThemeId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsKeys.AppliedCustomThemeId.default
        )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _generationProgressMessage = MutableStateFlow("")
    val generationProgressMessage = _generationProgressMessage.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError = _generationError.asStateFlow()

    fun dismissGenerationError() {
        _generationError.value = null
    }

    fun deleteTheme(theme: UserGeneratedColorScheme) {
        viewModelScope.launch {
            customColorSchemeDao.deleteColorScheme(theme.toEntity())
        }
    }

    fun applyColorScheme(scheme: UserGeneratedColorScheme) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setBoolean(SettingsKeys.UserGeneratedColorSchemeApplied, true)
            settingsRepository.setInt(SettingsKeys.AppliedCustomThemeId, scheme.id)
            settingsRepository.setBoolean(SettingsKeys.DynamicColors, false)
            settingsRepository.setBoolean(
                SettingsKeys.IsCustomColorSchemeDarkThemed,
                scheme.isDarkTheme
            )
        }
    }

    private val _previewPayload = MutableStateFlow<ColorSchemePayload?>(null)
    val previewPayload = _previewPayload.asStateFlow()

    fun clearPreview() {
        _previewPayload.value = null
        colorSchemeImportHolder.clearImportedColorScheme()
    }

    fun setPreviewPayload(payload: ColorSchemePayload) {
        _previewPayload.value = payload
    }

    fun generateColorScheme(prompt: String) {
        if (prompt.isBlank()) return
        if (apiKeyRepository.getKey(LlmProvider.Gemini).isNullOrBlank()) {
            _showApiKeyRequiredDialog.value = true
            return
        }
        viewModelScope.launch {
            _isGenerating.value = true
            _generationProgressMessage.value = "Consulting the color wheel..."

            val result = generateCustomThemeUseCase(prompt, AiModelConstants.geminiLiteModels)

            if (result.isSuccess) {
                _generationProgressMessage.value = "Mixing digital paint..."
                _previewPayload.value = result.getOrNull()
            } else {
                val exception = result.exceptionOrNull()
                if (exception is CloudNetworkException.ProviderNotConfigured) {
                    _showApiKeyRequiredDialog.value = true
                } else {
                    val error = exception?.message ?: "Unknown error"
                    _generationError.value = "Failed to generate theme:\n$error"
                }
            }

            _isGenerating.value = false
            _generationProgressMessage.value = ""
        }
    }

    fun saveColorScheme(scheme: UserGeneratedColorScheme) {
        viewModelScope.launch {
            val id = customColorSchemeDao.insertColorScheme(scheme.toEntity())
            applyColorScheme(scheme.copy(id = id.toInt()))
            clearPreview()
        }
    }
}
