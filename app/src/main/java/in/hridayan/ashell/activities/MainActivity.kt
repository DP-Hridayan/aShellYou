package `in`.hridayan.ashell.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.core.common.CompositionLocals
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.core.presentation.AppUiEntry
import `in`.hridayan.ashell.core.presentation.components.snackbar.SnackBarHost
import `in`.hridayan.ashell.core.presentation.theme.AshellYouTheme
import `in`.hridayan.ashell.core.utils.handleSharedText
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val autoUpdateViewModel: AutoUpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val splashStartTime = System.currentTimeMillis()

        splashScreen.setKeepOnScreenCondition {
            settingsViewModel.isFirstLaunch == null ||
                    System.currentTimeMillis() - splashStartTime < 750L
        }

        super.onCreate(savedInstanceState)

        handleSharedText(intent)

        lifecycleScope.launch {
            val autoUpdateEnabled = settingsViewModel.getBoolean(SettingsKeys.AutoUpdate).first()

            if (autoUpdateEnabled) {
                autoUpdateViewModel.checkForUpdates()
            }
        }

        enableEdgeToEdge()
        setContent {
            CompositionLocals {
                SeedColorProvider.setSeedColor(LocalSeedColor.current)

                AshellYouTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppUiEntry()
                            SnackBarHost(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedText(intent)
    }
}
