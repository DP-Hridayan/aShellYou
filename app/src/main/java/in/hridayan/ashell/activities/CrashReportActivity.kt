package `in`.hridayan.ashell.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.core.common.CompositionLocals
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.core.presentation.theme.AshellYouTheme
import `in`.hridayan.ashell.crashreporter.presentation.screens.CrashReportScreen
import `in`.hridayan.ashell.ui.SettingsStateImpl
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import androidx.activity.viewModels
import androidx.compose.runtime.remember

@AndroidEntryPoint
class CrashReportActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val settingsState = remember(settingsViewModel) { SettingsStateImpl(settingsViewModel) }
            CompositionLocals(settingsState = settingsState) {
                SeedColorProvider.setSeedColor(LocalSeedColor.current)

                AshellYouTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) { CrashReportScreen() }
                }
            }
        }
    }
}