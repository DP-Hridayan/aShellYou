package `in`.hridayan.ashell.onboarding.presentation.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.domain.repository.ShellRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val shellRepository: ShellRepository
) : ViewModel() {

    fun shizukuPermissionState(): StateFlow<Boolean> = shellRepository.shizukuPermissionState()

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setBoolean(SettingsKeys.FirstLaunch, false)
        }
    }

    fun setInt(key: SettingsKeys<Int>, value: Int) {
        viewModelScope.launch {
            settingsRepository.setInt(key, value)
        }
    }

    fun hasRootAccess(): Boolean {
        return shellRepository.hasRootAccess()
    }

    suspend fun executeRootCommand(command: String) {
        shellRepository.executeRootCommand(command)
    }

    fun requestShizukuPermission() {
        shellRepository.requestShizukuPermission()
    }
}
