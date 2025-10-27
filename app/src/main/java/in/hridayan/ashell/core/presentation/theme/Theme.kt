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
import `in`.hridayan.ashell.core.common.LocalSettings

@Composable
fun AshellYouTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val darkTheme = LocalDarkMode.current
    val dynamicColor = LocalSettings.current.isDynamicColor
    val isHighContrastDarkTheme = LocalSettings.current.isHighContrastDarkMode

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
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme && isHighContrastDarkTheme) highContrastDynamicDarkColorScheme(context)
            else if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> {
            if (isHighContrastDarkTheme) highContrastDarkColorSchemeFromSeed()
            else darkColorSchemeFromSeed()
        }

        else -> lightColorSchemeFromSeed()
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}