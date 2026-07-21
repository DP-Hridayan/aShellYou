package `in`.hridayan.ashell.onboarding.presentation.viewmodel


import `in`.hridayan.ashell.core.common.SettingsKeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.domain.repository.SettingsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setBoolean(SettingsKeys.FirstLaunch, false)
        }
    }
}
