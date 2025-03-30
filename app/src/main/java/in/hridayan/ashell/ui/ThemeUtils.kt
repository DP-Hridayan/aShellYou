package `in`.hridayan.ashell.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.utils.DeviceUtils.androidVersion

object ThemeUtils {
    private var isAmoledTheme = false
    private var isDynamicTheme = false
    private var themeMode = 0

    @JvmStatic
    fun updateTheme(activity: AppCompatActivity) {
        isAmoledTheme = Preferences.getAmoledTheme()
        isDynamicTheme = Preferences.getDynamicColors()
        themeMode = Preferences.getThemeMode()

        AppCompatDelegate.setDefaultNightMode(themeMode)

        if (isAmoledTheme && isNightMode(activity)) setHighContrastDarkTheme(activity)
        else setNormalTheme(activity)
    }

    private fun setHighContrastDarkTheme(activity: AppCompatActivity) {
        if (androidVersion() >= Build.VERSION_CODES.S) activity.setTheme(
            if (isDynamicTheme)
                R.style.aShellYou_AmoledTheme_DynamicColors
            else
                R.style.aShellYou_AmoledTheme
        )
        else activity.setTheme(R.style.ThemeOverlay_aShellYou_AmoledThemeBelowV31)
    }

    private fun setNormalTheme(activity: AppCompatActivity) {
        if (isDynamicTheme) activity.setTheme(R.style.aShellYou_DynamicColors)
        else activity.setTheme(R.style.aShellYou_AppTheme)
    }

    // Returns if device is in dark mode
    fun isNightMode(context: Context): Boolean {
        return ((context.resources
            .configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES)
    }

    // returns a color
    @JvmStatic
    fun getColor(color: Int, context: Context): Int {
        return ContextCompat.getColor(context, color)
    }

    @JvmStatic
    fun colorError(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorError, typedValue, true)
        val colorError = typedValue.data
        return colorError
    }
}
