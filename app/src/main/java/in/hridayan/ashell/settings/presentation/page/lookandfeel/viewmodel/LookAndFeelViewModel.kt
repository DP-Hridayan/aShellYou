package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.data.local.provider.SeedColor
import `in`.hridayan.ashell.core.domain.model.PaletteStyle
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LookAndFeelViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
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
            settingsRepository.setInt(SettingsKeys.PRIMARY_SEED, seed.primary)
        }
    }

    fun setPaletteStyle(style: PaletteStyle) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setInt(SettingsKeys.PALETTE_STYLE, style.ordinal)
        }
    }

    fun disableDynamicColors() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setBoolean(SettingsKeys.DYNAMIC_COLORS, false)
        }
    }
}
