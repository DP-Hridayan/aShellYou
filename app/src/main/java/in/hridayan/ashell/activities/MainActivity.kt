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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.core.common.CompositionLocals
import `in`.hridayan.ashell.core.common.LocalFontFamily
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.domain.model.AppFont
import `in`.hridayan.ashell.core.common.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.snackbar.SnackBarHost
import `in`.hridayan.ashell.core.presentation.theme.AshellYouTheme
import `in`.hridayan.ashell.core.presentation.theme.data.toPayload
import `in`.hridayan.ashell.core.presentation.theme.util.ColorSchemeImportHolder
import `in`.hridayan.ashell.core.presentation.theme.util.ColorSchemeSerializer
import `in`.hridayan.ashell.core.utils.handleSharedText
import `in`.hridayan.ashell.logcat.data.session.LogcatDeeplinkHolder
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.settings.domain.repository.CustomFontRepository
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.ashell.ui.AppUiEntry
import `in`.hridayan.ashell.ui.state.SettingsStateImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val autoUpdateViewModel: AutoUpdateViewModel by viewModels()

    @Inject
    lateinit var logcatSessionHolder: LogcatSessionHolder

    @Inject
    lateinit var colorSchemeImportHolder: ColorSchemeImportHolder

    @Inject
    lateinit var customFontRepository: CustomFontRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val splashStartTime = System.currentTimeMillis()

        splashScreen.setKeepOnScreenCondition {
            settingsViewModel.isFirstLaunch == null ||
                    System.currentTimeMillis() - splashStartTime < SPLASH_SCREEN_DELAY_MS
        }

        super.onCreate(savedInstanceState)

        handleInitialIntents(intent)
        checkAutoUpdate()
        setupUi()
    }

    private fun handleInitialIntents(intent: Intent?) {
        if (intent == null) return
        handleSharedText(intent)
        handleLogcatDeeplink(intent)
        handleThemeImport(intent)
    }

    private fun checkAutoUpdate() {
        lifecycleScope.launch {
            val autoUpdateEnabled = settingsViewModel.getBoolean(SettingsKeys.AutoUpdate).first()
            if (autoUpdateEnabled) {
                autoUpdateViewModel.checkForUpdates(BuildConfig.VERSION_NAME)
            }
        }
    }

    private fun setupUi() {
        enableEdgeToEdge()
        setContent {
            val settingsState = remember(settingsViewModel) { SettingsStateImpl(settingsViewModel) }

            val customFonts by customFontRepository.getAllCustomFonts()
                .collectAsState(initial = emptyList())
            val selectedFontId = settingsState[SettingsKeys.FontFamily]
            val activeCustomFontFamily = remember(selectedFontId, customFonts) {
                if (selectedFontId >= AppFont.CUSTOM_FONT_ID_OFFSET) {
                    customFonts.find { it.id == selectedFontId }
                        ?.let { FontFamily(Font(File(it.filePath))) }
                } else null
            }

            CompositionLocals(settingsState = settingsState) {
                SeedColorProvider.setSeedColor(LocalSeedColor.current)

                CompositionLocalProvider(LocalFontFamily provides activeCustomFontFamily) {
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleInitialIntents(intent)
    }

    private fun handleLogcatDeeplink(intent: Intent?) {
        if (intent?.action == LogcatDeeplinkHolder.ACTION_OPEN_LOGCAT) {
            logcatSessionHolder.triggerLogcatNavigation()
        }
    }

    private fun handleThemeImport(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            try {
                val uri = intent.data!!
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val base64String = inputStream.bufferedReader().readText()
                    val entity = ColorSchemeSerializer.deserialize(base64String)
                    val payload = entity.toPayload()
                    colorSchemeImportHolder.setImportedColorScheme(payload)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to import theme", e)
            }
        }
    }

    companion object {
        private const val SPLASH_SCREEN_DELAY_MS = 650L
    }
}
