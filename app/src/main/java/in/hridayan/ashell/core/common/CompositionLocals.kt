@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.hridayan.ashell.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.data.local.provider.AppSeedColors
import `in`.hridayan.ashell.core.data.local.provider.SeedColor
import `in`.hridayan.ashell.core.domain.model.PaletteStyle
import `in`.hridayan.ashell.core.presentation.utils.HapticUtils.strongHaptic
import `in`.hridayan.ashell.core.presentation.utils.HapticUtils.weakHaptic
import `in`.hridayan.ashell.core.presentation.viewmodel.DialogViewModel
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.SettingsState
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import kotlin.math.abs

val LocalWeakHaptic = staticCompositionLocalOf { {} }
val LocalStrongHaptic = staticCompositionLocalOf { {} }
val LocalDarkMode = staticCompositionLocalOf<Boolean> {
    error("No dark mode provided")
}
val LocalSeedColor = staticCompositionLocalOf<SeedColor> {
    error("No seed color provided")
}
val LocalTonalPalette = staticCompositionLocalOf<List<AppSeedColors>> {
    error("No tonal palette provided")
}
val LocalPaletteStyle = staticCompositionLocalOf { PaletteStyle.TONAL_SPOT }

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> {
    error("No shared transition scope provided")
}

val LocalAnimatedContentScope = staticCompositionLocalOf<AnimatedContentScope> {
    error("No AnimatedContentScope provided")
}

val LocalDialogManager = staticCompositionLocalOf<DialogViewModel> {
    error { "No DialogViewModel provided" }
}

@Composable
fun CompositionLocals(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    dialogViewModel: DialogViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val baseDensity = LocalDensity.current
    val configuration = LocalConfiguration.current

    val autoUpdate by settingsViewModel.booleanState(SettingsKeys.AUTO_UPDATE)

    val themeMode by settingsViewModel.intState(SettingsKeys.THEME_MODE)

    val primarySeed by settingsViewModel.intState(SettingsKeys.PRIMARY_SEED)

    val seedColor = SeedColor(primarySeed)

    val paletteStyleOrdinal by settingsViewModel.intState(SettingsKeys.PALETTE_STYLE)
    val paletteStyle = remember(paletteStyleOrdinal) {
        PaletteStyle.entries.getOrElse(paletteStyleOrdinal) { PaletteStyle.TONAL_SPOT }
    }

    val isDynamicColor by settingsViewModel.booleanState(SettingsKeys.DYNAMIC_COLORS)

    val isHighContrastDarkMode by settingsViewModel.booleanState(SettingsKeys.HIGH_CONTRAST_DARK_MODE)

    val isHapticEnabled by settingsViewModel.booleanState(SettingsKeys.HAPTICS_AND_VIBRATION)

    val githubReleaseType by settingsViewModel.intState(SettingsKeys.GITHUB_RELEASE_TYPE)

    val savedVersionCode by settingsViewModel.intState(SettingsKeys.SAVED_VERSION_CODE)

    val enableDirectDownload by settingsViewModel.booleanState(SettingsKeys.ENABLE_DIRECT_DOWNLOAD)

    val smoothScrolling by settingsViewModel.booleanState(SettingsKeys.SMOOTH_SCROLLING)

    val clearOutputConfirmation by settingsViewModel.booleanState(SettingsKeys.CLEAR_OUTPUT_CONFIRMATION)

    val overrideBookmarksLimit by settingsViewModel.booleanState(SettingsKeys.OVERRIDE_MAXIMUM_BOOKMARKS_LIMIT)

    val disableSoftKeyboard by settingsViewModel.booleanState(SettingsKeys.DISABLE_SOFT_KEYBOARD)

    val localAdbMode by settingsViewModel.intState(SettingsKeys.LOCAL_ADB_WORKING_MODE)

    val outputSaveDirectory by settingsViewModel.stringState(SettingsKeys.OUTPUT_SAVE_DIRECTORY)

    val saveWholeOutput by settingsViewModel.booleanState(SettingsKeys.SAVE_WHOLE_OUTPUT)

    val lastSavedFileUri by settingsViewModel.stringState(SettingsKeys.LAST_SAVED_FILE_URI)

    val isFirstLaunch by settingsViewModel.booleanState(SettingsKeys.FIRST_LAUNCH)

    val bookmarkSortType by settingsViewModel.intState(SettingsKeys.BOOKMARK_SORT_TYPE)

    val commandSortType by settingsViewModel.intState(SettingsKeys.COMMAND_SORT_TYPE)

    val terminalFontStyle by settingsViewModel.intState(SettingsKeys.TERMINAL_FONT_STYLE)

    val isNewCommandsAvailable by settingsViewModel.booleanState(SettingsKeys.NEW_COMMANDS_AVAILABLE)

    val lastLocalBackupTime by settingsViewModel.stringState(SettingsKeys.LAST_LOCAL_BACKUP_TIME)

    val lastCloudBackupTime by settingsViewModel.stringState(SettingsKeys.LAST_CLOUD_BACKUP_TIME)

    val lastLocalBackupType by settingsViewModel.stringState(SettingsKeys.LAST_LOCAL_BACKUP_TYPE)

    val lastCloudBackupType by settingsViewModel.stringState(SettingsKeys.LAST_CLOUD_BACKUP_TYPE)

    val selectedModelId by settingsViewModel.stringState(SettingsKeys.SELECTED_MODEL_ID)

    val aiCacheEnabled by settingsViewModel.booleanState(SettingsKeys.AI_CACHE_ENABLED)

    val aiCacheDays by settingsViewModel.intState(SettingsKeys.AI_CACHE_DAYS)

    val autoScaleUI by settingsViewModel.booleanState(SettingsKeys.AUTO_SCALE_UI)

    val screenDensityMultiplier by settingsViewModel.floatState(SettingsKeys.SCREEN_DENSITY_MULTIPLIER)

    val fontSizeMultiplier by settingsViewModel.floatState(SettingsKeys.FONT_SIZE_MULTIPLIER)

    val fontFamily by settingsViewModel.intState(SettingsKeys.FONT_FAMILY)

    val autoBackupEnabled by settingsViewModel.booleanState(SettingsKeys.AUTO_BACKUP_ENABLED)
    val autoBackupFolderName by settingsViewModel.stringState(SettingsKeys.AUTO_BACKUP_FOLDER_NAME)
    val autoBackupTimeHour by settingsViewModel.intState(SettingsKeys.AUTO_BACKUP_TIME_HOUR)
    val autoBackupTimeMinute by settingsViewModel.intState(SettingsKeys.AUTO_BACKUP_TIME_MINUTE)
    val autoBackupFrequency by settingsViewModel.intState(SettingsKeys.AUTO_BACKUP_FREQUENCY)
    val lastAutoBackupLocalSuccessTime by settingsViewModel.stringState(SettingsKeys.LAST_AUTO_BACKUP_LOCAL_SUCCESS_TIME)
    val lastAutoBackupLocalError by settingsViewModel.stringState(SettingsKeys.LAST_AUTO_BACKUP_LOCAL_ERROR)
    val lastAutoBackupCloudSuccessTime by settingsViewModel.stringState(SettingsKeys.LAST_AUTO_BACKUP_CLOUD_SUCCESS_TIME)
    val lastAutoBackupCloudError by settingsViewModel.stringState(SettingsKeys.LAST_AUTO_BACKUP_CLOUD_ERROR)

    val scaledDensity = remember(
        autoScaleUI,
        screenDensityMultiplier,
        fontSizeMultiplier,
        baseDensity,
        configuration.smallestScreenWidthDp
    ) {
        val densityMultiplier = if (autoScaleUI) {
            val developmentScreenWidth = 406f
            val targetScreenWidth = configuration.smallestScreenWidthDp.toFloat()

            val differenceRatio =
                (developmentScreenWidth - targetScreenWidth) / developmentScreenWidth

            val normalizedDifference = abs(differenceRatio)
                .coerceIn(0f, 1f)

            val compensationFactor = 0.4f + 0.25f * normalizedDifference

            val compensation =
                differenceRatio * normalizedDifference * compensationFactor

            (targetScreenWidth / developmentScreenWidth) + compensation
        } else {
            screenDensityMultiplier
        }

        val fontMultiplier = if (autoScaleUI) 1.0f else fontSizeMultiplier

        Density(
            density = baseDensity.density * densityMultiplier,
            fontScale = baseDensity.fontScale * densityMultiplier * fontMultiplier
        )
    }

    val state =
        remember(
            autoUpdate,
            themeMode,
            seedColor,
            paletteStyle,
            isDynamicColor,
            isHighContrastDarkMode,
            isHapticEnabled,
            githubReleaseType,
            savedVersionCode,
            enableDirectDownload,
            localAdbMode,
            smoothScrolling,
            clearOutputConfirmation,
            overrideBookmarksLimit,
            disableSoftKeyboard,
            outputSaveDirectory,
            saveWholeOutput,
            lastSavedFileUri,
            isFirstLaunch,
            bookmarkSortType,
            commandSortType,
            terminalFontStyle,
            isNewCommandsAvailable,
            lastLocalBackupTime,
            lastCloudBackupTime,
            lastLocalBackupType,
            lastCloudBackupType,
            selectedModelId,
            aiCacheEnabled,
            aiCacheDays,
            autoScaleUI,
            screenDensityMultiplier,
            fontSizeMultiplier,
            fontFamily,
            autoBackupEnabled,
            autoBackupFolderName,
            autoBackupTimeHour,
            autoBackupTimeMinute,
            autoBackupFrequency,
            lastAutoBackupLocalSuccessTime,
            lastAutoBackupLocalError,
            lastAutoBackupCloudSuccessTime,
            lastAutoBackupCloudError
        ) {
            SettingsState(
                isAutoUpdate = autoUpdate,
                themeMode = themeMode,
                isHighContrastDarkMode = isHighContrastDarkMode,
                seedColor = seedColor,
                paletteStyle = paletteStyle,
                isDynamicColor = isDynamicColor,
                isHapticEnabled = isHapticEnabled,
                githubReleaseType = githubReleaseType,
                savedVersionCode = savedVersionCode,
                enableDirectDownload = enableDirectDownload,
                localAdbMode = localAdbMode,
                smoothScrolling = smoothScrolling,
                clearOutputConfirmation = clearOutputConfirmation,
                overrideBookmarksLimit = overrideBookmarksLimit,
                disableSoftKeyboard = disableSoftKeyboard,
                outputSaveDirectory = outputSaveDirectory,
                saveWholeOutput = saveWholeOutput,
                lastSavedFileUri = lastSavedFileUri,
                isFirstLaunch = isFirstLaunch,
                bookmarkSortType = bookmarkSortType,
                commandsSortType = commandSortType,
                terminalFontStyle = terminalFontStyle,
                isNewCommandsAvailable = isNewCommandsAvailable,
                lastLocalBackupTime = lastLocalBackupTime,
                lastCloudBackupTime = lastCloudBackupTime,
                lastLocalBackupType = lastLocalBackupType,
                lastCloudBackupType = lastCloudBackupType,
                selectedModelId = selectedModelId,
                aiCacheEnabled = aiCacheEnabled,
                aiCacheDays = aiCacheDays,
                autoUiScale = autoScaleUI,
                screenDensityMultiplier = screenDensityMultiplier,
                fontSizeMultiplier = fontSizeMultiplier,
                fontFamily = fontFamily,
                autoBackupEnabled = autoBackupEnabled,
                autoBackupFolderName = autoBackupFolderName,
                autoBackupTimeHour = autoBackupTimeHour,
                autoBackupTimeMinute = autoBackupTimeMinute,
                autoBackupFrequency = autoBackupFrequency,
                lastAutoBackupLocalSuccessTime = lastAutoBackupLocalSuccessTime,
                lastAutoBackupLocalError = lastAutoBackupLocalError,
                lastAutoBackupCloudSuccessTime = lastAutoBackupCloudSuccessTime,
                lastAutoBackupCloudError = lastAutoBackupCloudError
            )
        }

    val isDarkTheme = when (themeMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }

    val tonalPalette = listOf(
        AppSeedColors.Color01,
        AppSeedColors.Color02,
        AppSeedColors.Color03,
        AppSeedColors.Color04,
        AppSeedColors.Color05,
        AppSeedColors.Color06,
        AppSeedColors.Color07,
        AppSeedColors.Color08,
        AppSeedColors.Color09,
        AppSeedColors.Color10,
        AppSeedColors.Color11,
        AppSeedColors.Color12,
        AppSeedColors.Color13,
        AppSeedColors.Color14,
        AppSeedColors.Color15,
        AppSeedColors.Color16,
        AppSeedColors.Color17,
        AppSeedColors.Color18,
        AppSeedColors.Color19,
        AppSeedColors.Color20
    )

    val weakHaptic = remember(isHapticEnabled, view) {
        {
            if (isHapticEnabled) {
                view.weakHaptic()
            }
        }
    }

    val strongHaptic = remember(isHapticEnabled, view) {
        {
            if (isHapticEnabled) {
                view.strongHaptic()
            }
        }
    }

    CompositionLocalProvider(
        LocalSettings provides state,
        LocalDensity provides scaledDensity,
        LocalWeakHaptic provides weakHaptic,
        LocalStrongHaptic provides strongHaptic,
        LocalSeedColor provides seedColor,
        LocalDarkMode provides isDarkTheme,
        LocalTonalPalette provides tonalPalette,
        LocalPaletteStyle provides paletteStyle,
        LocalDialogManager provides dialogViewModel
    ) {
        content()
    }
}

@Composable
private fun SettingsViewModel.booleanState(key: SettingsKeys<Boolean>): State<Boolean> {
    return getBoolean(key).collectAsState(initial = key.default)
}

@Composable
private fun SettingsViewModel.intState(key: SettingsKeys<Int>): State<Int> {
    return getInt(key).collectAsState(initial = key.default)
}

@Composable
private fun SettingsViewModel.floatState(key: SettingsKeys<Float>): State<Float> {
    return getFloat(key).collectAsState(initial = key.default)
}

@Composable
private fun SettingsViewModel.stringState(key: SettingsKeys<String>): State<String> {
    return getString(key).collectAsState(initial = key.default)
}
