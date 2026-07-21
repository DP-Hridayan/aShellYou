@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.hridayan.ashell.core.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.data.local.provider.AppSeedColors
import `in`.hridayan.ashell.core.data.local.provider.SeedColor
import `in`.hridayan.ashell.core.domain.model.PaletteStyle
import `in`.hridayan.ashell.core.presentation.components.snackbar.SnackBarController
import `in`.hridayan.ashell.core.presentation.utils.HapticUtils.strongHaptic
import `in`.hridayan.ashell.core.presentation.utils.HapticUtils.weakHaptic
import `in`.hridayan.ashell.core.presentation.viewmodel.DialogViewModel
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

val LocalSnackBarController = staticCompositionLocalOf<SnackBarController> {
    error("No SnackBarController provided")
}

@Composable
fun CompositionLocals(
    settingsState: SettingsState,
    dialogViewModel: DialogViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val baseDensity = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Theme
    val themeMode = settingsState[SettingsKeys.ThemeMode]
    val primarySeed = settingsState[SettingsKeys.PrimarySeed]
    val seedColor = SeedColor(primarySeed)
    val paletteStyleOrdinal = settingsState[SettingsKeys.PaletteStyle]
    val paletteStyle = remember(paletteStyleOrdinal) {
        PaletteStyle.entries.getOrElse(paletteStyleOrdinal) { PaletteStyle.TONAL_SPOT }
    }

    // Haptics
    val isHapticEnabled = settingsState[SettingsKeys.HapticsAndVibration]

    // Density
    val autoScaleUI = settingsState[SettingsKeys.AutoScaleUi]
    val screenDensityMultiplier = settingsState[SettingsKeys.ScreenDensityMultiplier]
    val fontSizeMultiplier = settingsState[SettingsKeys.FontSizeMultiplier]

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

    // Battery saver
    val autoDarkModeOnBatterySaver = settingsState[SettingsKeys.AutoDarkModeOnBatterySaver]

    var isBatterySaverOn by remember {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mutableStateOf(pm.isPowerSaveMode)
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                isBatterySaverOn = pm.isPowerSaveMode
            }
        }
        val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ── Derived values ──
    val isDarkTheme = when {
        autoDarkModeOnBatterySaver && isBatterySaverOn -> true
        else -> when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> isSystemInDarkTheme()
        }
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

    val snackbarController = remember { SnackBarController() }

    CompositionLocalProvider(
        LocalSettings provides settingsState,
        LocalDensity provides scaledDensity,
        LocalWeakHaptic provides weakHaptic,
        LocalStrongHaptic provides strongHaptic,
        LocalSeedColor provides seedColor,
        LocalDarkMode provides isDarkTheme,
        LocalTonalPalette provides tonalPalette,
        LocalPaletteStyle provides paletteStyle,
        LocalDialogManager provides dialogViewModel,
        LocalSnackBarController provides snackbarController
    ) {
        content()
    }
}

