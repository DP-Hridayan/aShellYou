package `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.core.domain.model.DownloadState
import `in`.hridayan.ashell.core.domain.usecase.DownloadApkUseCase
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.domain.usecase.CheckUpdateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoUpdateViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val checkUpdateUseCase: CheckUpdateUseCase,
    private val downloadApkUseCase: DownloadApkUseCase
) : ViewModel() {
    private val _updateEvents = MutableSharedFlow<UpdateResult>()
    val updateEvents = _updateEvents.asSharedFlow()

    fun checkForUpdates(includePrerelease: Boolean) {
        viewModelScope.launch {
            val result = checkUpdateUseCase(BuildConfig.VERSION_NAME, includePrerelease)
            _updateEvents.emit(result)
        }
    }

    fun select(option: Int) {
        viewModelScope.launch {
            settingsRepository.setInt(SettingsKeys.GITHUB_RELEASE_TYPE, option)
        }
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    fun downloadApk(url: String, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadApkUseCase(url, fileName) {
                _downloadState.value = it
            }
        }
    }

    fun cancelDownload() {
        downloadApkUseCase.cancel()
    }
}