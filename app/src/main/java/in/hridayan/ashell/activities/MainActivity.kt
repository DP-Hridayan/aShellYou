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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.core.common.CompositionLocals
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.core.presentation.components.snackbar.SnackBarHost
import `in`.hridayan.ashell.core.presentation.theme.AshellYouTheme
import `in`.hridayan.ashell.core.utils.handleSharedText
import `in`.hridayan.ashell.logcat.data.session.LogcatDeeplinkHolder
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.ashell.ui.AppUiEntry
import `in`.hridayan.ashell.ui.SettingsStateImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val autoUpdateViewModel: AutoUpdateViewModel by viewModels()

    @Inject
    lateinit var logcatSessionHolder: LogcatSessionHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val splashStartTime = System.currentTimeMillis()

        splashScreen.setKeepOnScreenCondition {
            settingsViewModel.isFirstLaunch == null ||
                    System.currentTimeMillis() - splashStartTime < 650L
        }

        super.onCreate(savedInstanceState)

        handleSharedText(intent)
        handleLogcatDeeplink(intent)

        lifecycleScope.launch {
            val autoUpdateEnabled = settingsViewModel.getBoolean(SettingsKeys.AutoUpdate).first()
            if (autoUpdateEnabled) {
                autoUpdateViewModel.checkForUpdates()
            }
        }

        enableEdgeToEdge()
        setContent {
            val settingsState = remember(settingsViewModel) { SettingsStateImpl(settingsViewModel) }
            CompositionLocals(settingsState = settingsState) {
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
        handleLogcatDeeplink(intent)
    }

    private fun handleLogcatDeeplink(intent: Intent?) {
        if (intent?.action == LogcatDeeplinkHolder.ACTION_OPEN_LOGCAT) {
            logcatSessionHolder.triggerLogcatNavigation()
        }
    }
}
