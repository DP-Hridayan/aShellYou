package `in`.hridayan.ashell.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.application.AppLifecycleObserver
import `in`.hridayan.ashell.core.common.CompositionLocals
import `in`.hridayan.ashell.core.common.LocalFontFamily
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.AppDeeplinkHolder
import `in`.hridayan.ashell.core.navigation.AppDeeplinkHolder.DeeplinkDestination
import `in`.hridayan.ashell.core.presentation.components.snackbar.SnackBarHost
import `in`.hridayan.ashell.core.presentation.theme.AshellYouTheme
import `in`.hridayan.ashell.core.presentation.theme.data.toPayload
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toDomain
import `in`.hridayan.ashell.core.presentation.theme.domain.usecase.SaveAndApplyColorSchemeUseCase
import `in`.hridayan.ashell.core.presentation.theme.util.ColorSchemeImportHolder
import `in`.hridayan.ashell.core.presentation.theme.util.ColorSchemeSerializer
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.handleSharedText
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.settings.presentation.components.bottomsheet.ThemePreviewBottomSheet
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.ashell.ui.AppUiEntry
import `in`.hridayan.ashell.ui.screens.AppLockedScreen
import `in`.hridayan.ashell.ui.state.SettingsStateImpl
import `in`.hridayan.ashell.ui.viewmodel.AppFontViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val autoUpdateViewModel: AutoUpdateViewModel by viewModels()
    private val appFontViewModel: AppFontViewModel by viewModels()

    @Inject
    lateinit var logcatSessionHolder: LogcatSessionHolder

    @Inject
    lateinit var colorSchemeImportHolder: ColorSchemeImportHolder

    @Inject
    lateinit var saveAndApplyColorSchemeUseCase: SaveAndApplyColorSchemeUseCase

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val splashStartTime = System.currentTimeMillis()

        splashScreen.setKeepOnScreenCondition {
            settingsViewModel.isFirstLaunch == null
                || settingsViewModel.defaultLaunchIsLocalAdb == null
                || System.currentTimeMillis() - splashStartTime < SPLASH_SCREEN_DELAY_MS
        }

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        handleInitialIntents(intent)
        checkAutoUpdate()
        setupUi()
    }

    private fun handleInitialIntents(intent: Intent?) {
        if (intent == null) return
        handleSharedText(intent)
        handleDeepLinks(intent)
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
        setContent {
            val settingsState = remember(settingsViewModel) { SettingsStateImpl(settingsViewModel) }
            val activeCustomFontFamily by appFontViewModel.activeCustomFontFamily.collectAsState()
            val isLocked by appLifecycleObserver.isLocked.collectAsState()

            CompositionLocals(settingsState = settingsState) {
                SeedColorProvider.setSeedColor(LocalSeedColor.current)

                CompositionLocalProvider(LocalFontFamily provides activeCustomFontFamily) {
                    AshellYouTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            EnterAppUi(modifier = Modifier.fillMaxSize(), isLocked = isLocked)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EnterAppUi(modifier: Modifier = Modifier, isLocked: Boolean) {
        val coroutineScope = rememberCoroutineScope()
        val importedThemePayload by colorSchemeImportHolder.importedTheme.collectAsState()
        val context = LocalContext.current

        Box(modifier = modifier) {
            AppUiEntry()

            if (isLocked) {
                AppLockedScreen(onUnlockSuccess = { appLifecycleObserver.unlock() })
            }

            SnackBarHost(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            if (importedThemePayload != null) {
                ThemePreviewBottomSheet(
                    payload = importedThemePayload!!,
                    onDismiss = { colorSchemeImportHolder.clearImportedColorScheme() },
                    onSave = { entity ->
                        coroutineScope.launch {
                            val result = saveAndApplyColorSchemeUseCase(entity.toDomain())
                            colorSchemeImportHolder.clearImportedColorScheme()
                            if (result.isSuccess) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.theme_saved_applied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.theme_import_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleInitialIntents(intent)
    }

    private fun handleDeepLinks(intent: Intent?) {
        when (intent?.action) {
            AppDeeplinkHolder.ACTION_OPEN_LOGCAT -> {
                AppDeeplinkHolder.navigateTo(DeeplinkDestination.LOGCAT)
            }

            AppDeeplinkHolder.ACTION_OPEN_AI_AGENT -> {
                AppDeeplinkHolder.navigateTo(DeeplinkDestination.AI_CHAT)
            }
        }
    }

    private fun handleThemeImport(intent: Intent?) {
        if (intent == null) return

        val uri = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                    ?: intent.data
            }
            else -> null
        } ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader().readText()
                    val entity = ColorSchemeSerializer.deserialize(content)
                    val payload = entity.toPayload()
                    colorSchemeImportHolder.setImportedColorScheme(payload)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to import color scheme", e)
            }
        }
    }

    companion object {
        private const val SPLASH_SCREEN_DELAY_MS = 650L
        private const val TAG = "MainActivity"
    }
}
