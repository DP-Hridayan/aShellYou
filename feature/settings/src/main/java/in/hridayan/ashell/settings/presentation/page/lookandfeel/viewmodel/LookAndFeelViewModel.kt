package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel

import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.data.provider.SeedColor
import `in`.hridayan.ashell.core.common.domain.model.PaletteStyle
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.CustomFontEntity
import `in`.hridayan.ashell.settings.domain.repository.CustomFontRepository
import `in`.hridayan.ashell.settings.domain.usecase.DeleteCustomFontUseCase
import `in`.hridayan.ashell.settings.domain.usecase.ImportCustomFontUseCase
import `in`.hridayan.ashell.settings.domain.usecase.ReadFontNameUseCase
import `in`.hridayan.ashell.settings.domain.usecase.ValidateFontFileUseCase
import `in`.hridayan.ashell.settings.presentation.model.FontImportState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LookAndFeelViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val customFontRepository: CustomFontRepository,
    private val validateFontFileUseCase: ValidateFontFileUseCase,
    private val readFontNameUseCase: ReadFontNameUseCase,
    private val importCustomFontUseCase: ImportCustomFontUseCase,
    private val deleteCustomFontUseCase: DeleteCustomFontUseCase
) : ViewModel() {

    private var lastSeed: SeedColor? = null

    private val _isCheckedMatchCase = MutableStateFlow(false)
    val isCheckedMatchCase: StateFlow<Boolean> = _isCheckedMatchCase

    private val _isCheckedBold = MutableStateFlow(false)
    val isCheckedBold: StateFlow<Boolean> = _isCheckedBold

    private val _isCheckedItalic = MutableStateFlow(false)
    val isCheckedItalic: StateFlow<Boolean> = _isCheckedItalic

    private val _isCheckedUnderline = MutableStateFlow(false)
    val isCheckedUnderline: StateFlow<Boolean> = _isCheckedUnderline

    val customFonts: StateFlow<List<CustomFontEntity>> = customFontRepository
        .getAllCustomFonts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Pre-built [FontFamily] instances for every custom font, keyed by [CustomFontEntity.id].
     *
     * Built on [Dispatchers.IO] via [map] so the Compose composition thread never
     * performs file I/O when rendering font list rows.
     */
    val fontFamilyCache: StateFlow<Map<Int, FontFamily>> = customFonts
        .map { entities ->
            entities.associate { entity ->
                entity.id to runCatching {
                    val typeface = Typeface.createFromFile(File(entity.filePath))
                    FontFamily(typeface)
                }.getOrElse { FontFamily.Default }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _fontImportState = MutableStateFlow<FontImportState>(FontImportState.Idle)
    val fontImportState: StateFlow<FontImportState> = _fontImportState

    private var pendingFontUri: Uri? = null

    fun toggleMatchCase() {
        _isCheckedMatchCase.value = !_isCheckedMatchCase.value
    }

    fun toggleBold() {
        _isCheckedBold.value = !_isCheckedBold.value
    }

    fun toggleItalic() {
        _isCheckedItalic.value = !_isCheckedItalic.value
    }

    fun toggleUnderline() {
        _isCheckedUnderline.value = !_isCheckedUnderline.value
    }

    fun formatClear() {
        _isCheckedMatchCase.value = false
        _isCheckedBold.value = false
        _isCheckedItalic.value = false
        _isCheckedUnderline.value = false
    }

    fun setSeedColor(seed: SeedColor) {
        if (seed == lastSeed) return
        lastSeed = seed
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setInt(SettingsKeys.PrimarySeed, seed.seed)
        }
    }

    fun setPaletteStyle(style: PaletteStyle) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setInt(SettingsKeys.PaletteStyle, style.ordinal)
        }
    }

    fun disableDynamicColors() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setBoolean(SettingsKeys.DynamicColors, false)
        }
    }

    fun disableUserGeneratedColorScheme() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setBoolean(SettingsKeys.UserGeneratedColorSchemeApplied, false)
        }
    }

    fun onFontFilePicked(uri: Uri) {
        pendingFontUri = uri
        _fontImportState.value = FontImportState.Validating
        viewModelScope.launch(Dispatchers.IO) {
            val isValid = validateFontFileUseCase(uri).getOrElse {
                _fontImportState.value = FontImportState.InvalidFile
                return@launch
            }
            if (!isValid) {
                _fontImportState.value = FontImportState.InvalidFile
                return@launch
            }
            val result = readFontNameUseCase(uri).getOrNull()
            _fontImportState.value = FontImportState.NamingPrompt(
                prefilledName = result?.name ?: "",
                tempFilePath = result?.tempFilePath ?: ""
            )
        }
    }

    fun confirmFontImport(displayName: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val uri = pendingFontUri ?: return
        _fontImportState.value = FontImportState.Saving
        viewModelScope.launch(Dispatchers.IO) {
            importCustomFontUseCase(uri, displayName)
                .onSuccess {
                    _fontImportState.value = FontImportState.Idle
                    pendingFontUri = null
                    onSuccess()
                }
                .onFailure {
                    _fontImportState.value = FontImportState.Idle
                    pendingFontUri = null
                    onError()
                }
        }
    }

    fun dismissImportDialog() {
        pendingFontUri = null
        _fontImportState.value = FontImportState.Idle
    }

    fun deleteCustomFont(
        entity: CustomFontEntity,
        onDeleted: (wasSelected: Boolean, deletedId: Int) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteCustomFontUseCase(entity)
                .onSuccess { onDeleted(false, entity.id) }
        }
    }
}
