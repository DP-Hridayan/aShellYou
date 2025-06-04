package `in`.hridayan.ashell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.core.common.CompositionLocals
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.SeedColorProvider
import `in`.hridayan.ashell.core.common.constants.GithubReleaseType
import `in`.hridayan.ashell.core.presentation.AppEntry
import `in`.hridayan.ashell.core.presentation.ui.theme.AshellYouTheme
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val autoUpdateViewModel: AutoUpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val autoUpdateEnabled = settingsViewModel.getBoolean(SettingsKeys.AUTO_UPDATE).first()
            val includePrerelease =
                settingsViewModel.getInt(SettingsKeys.GITHUB_RELEASE_TYPE)
                    .first() == GithubReleaseType.PRE_RELEASE

            if (autoUpdateEnabled) {
                autoUpdateViewModel.checkForUpdates(
                    includePrerelease = includePrerelease
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            CompositionLocals {
                SeedColorProvider.seedColor = LocalSeedColor.current

                AshellYouTheme {
                    Surface(
                        modifier = Modifier.Companion.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        AppEntry()
                    }
                }
            }
        }
    }
}