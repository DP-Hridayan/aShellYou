package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.data.local.provider.SeedColor
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LookAndFeelViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private var lastSeed: SeedColor? = null

    fun setSeedColor(seed: SeedColor) {
        if (seed == lastSeed) return
        lastSeed = seed

        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setInt(SettingsKeys.PRIMARY_SEED, seed.primary)
            settingsRepository.setInt(SettingsKeys.SECONDARY_SEED, seed.secondary)
            settingsRepository.setInt(SettingsKeys.TERTIARY_SEED, seed.tertiary)
        }
    }

    fun disableDynamicColors() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setBoolean(SettingsKeys.DYNAMIC_COLORS, false)
        }
    }
}
