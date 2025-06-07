package `in`.hridayan.ashell.core.presentation

import android.util.Log
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.presentation.components.bottomsheet.ChangelogBottomSheet
import `in`.hridayan.ashell.core.presentation.components.bottomsheet.UpdateBottomSheet
import `in`.hridayan.ashell.navigation.Navigation
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppEntry(
    autoUpdateViewModel: AutoUpdateViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val isFirstLaunch = settingsViewModel.isFirstLaunch
    if (isFirstLaunch == null) return

    var showUpdateSheet by rememberSaveable { mutableStateOf(false) }
    var showChangelogSheet by rememberSaveable { mutableStateOf(false) }
    var tagName by rememberSaveable { mutableStateOf(BuildConfig.VERSION_NAME) }
    var apkUrl by rememberSaveable { mutableStateOf("") }
    val savedVersionCode = LocalSettings.current.savedVersionCode
    val firstLaunchFlow = LocalSettings.current.isFirstLaunch

    LaunchedEffect(Unit) {
        autoUpdateViewModel.updateEvents.collectLatest { result ->
            if (result is UpdateResult.Success && result.isUpdateAvailable) {
                tagName = result.release.tagName
                apkUrl = result.release.apkUrl.toString()
                Log.d("AppEntry", result.release.apkUrl.toString())
                showUpdateSheet = true
            }
        }
    }

    LaunchedEffect(savedVersionCode, firstLaunchFlow) {
        showChangelogSheet = savedVersionCode < BuildConfig.VERSION_CODE && !firstLaunchFlow
    }

    Surface {
        Navigation(isFirstLaunch)

        if (showUpdateSheet) {
            UpdateBottomSheet(
                onDismiss = { showUpdateSheet = false },
                latestVersion = tagName,
                apkUrl = apkUrl,
            )
        }

        if (showChangelogSheet) {
            ChangelogBottomSheet(
                onDismiss = {
                    showChangelogSheet = false
                    settingsViewModel.setInt(
                        SettingsKeys.SAVED_VERSION_CODE,
                        BuildConfig.VERSION_CODE
                    )
                }
            )
        }
    }
}
