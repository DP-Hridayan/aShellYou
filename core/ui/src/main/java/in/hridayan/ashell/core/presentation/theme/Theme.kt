@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalFontFamily
import `in`.hridayan.ashell.core.common.LocalUserGeneratedColorScheme
import `in`.hridayan.ashell.core.common.domain.model.AppFont
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.theme.color.darkColorSchemeFromSeed
import `in`.hridayan.ashell.core.presentation.theme.color.highContrastDarkColorSchemeFromSeed
import `in`.hridayan.ashell.core.presentation.theme.color.highContrastDynamicDarkColorScheme
import `in`.hridayan.ashell.core.presentation.theme.color.lightColorSchemeFromSeed
import `in`.hridayan.ashell.core.presentation.theme.color.toColorScheme

@Composable
fun AshellYouTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val darkTheme = LocalDarkMode.current
    val generatedColorScheme = LocalUserGeneratedColorScheme.current
    val settings = LocalSettings.current
    val dynamicColor = settings[SettingsKeys.DynamicColors]
    val isHighContrastDarkTheme = settings[SettingsKeys.HighContrastDarkMode]
    val isUserGeneratedColorSchemeApplied = settings[SettingsKeys.UserGeneratedColorSchemeApplied]

    LaunchedEffect(darkTheme) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            view.windowInsetsController?.setSystemBarsAppearance(
                if (darkTheme) 0 else APPEARANCE_LIGHT_STATUS_BARS,
                APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
            val controller = WindowCompat.getInsetsController(window, view)

            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme

            @Suppress("DEPRECATION")
            window.statusBarColor = Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    val colorScheme = when {
        isUserGeneratedColorSchemeApplied && generatedColorScheme != null && !dynamicColor -> generatedColorScheme.toColorScheme()

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme && isHighContrastDarkTheme) {
                highContrastDynamicDarkColorScheme(context)
            } else if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> {
            if (isHighContrastDarkTheme) {
                highContrastDarkColorSchemeFromSeed()
            } else {
                darkColorSchemeFromSeed()
            }
        }

        else -> lightColorSchemeFromSeed()
    }

    val fontFamilyId = settings[SettingsKeys.FontFamily]
    val customFontFamily = LocalFontFamily.current
    val resolvedFontFamily = if (fontFamilyId >= AppFont.CUSTOM_FONT_ID_OFFSET) {
        customFontFamily ?: AppFont.SYSTEM.fontFamily
    } else {
        AppFont.fromId(fontFamilyId).fontFamily
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = appTypography(resolvedFontFamily),
        content = content
    )
}
