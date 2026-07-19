package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.core.presentation.theme.color.blend

/**
 * Semantic colors for each [LogLevel].
 *
 * - [barColor]: pure, fully opaque — used for the 4dp left bar
 * - [rowBackground]: very low alpha — used for the row background tint
 * - [textColor]: desaturated/darkened depending on [isDark] mode
 *   so text passes WCAG contrast requirements in both themes
 */
object LogLevelColors {

    // Base hues — intentionally distinct across the spectrum
    val Verbose = Color(0xFF9E9E9E)  // neutral grey
    val Debug   = Color(0xFF4FC3F7)  // light blue
    val Info    = Color(0xFF81C784)  // green
    val Warning = Color(0xFFFFB74D)  // amber
    val Error   = Color(0xFFE57373)  // red
    val Fatal   = Color(0xFFCE93D8)  // purple
    val Unknown = Color(0xFF78909C)  // blue-grey

    fun baseColor(level: LogLevel): Color = when (level) {
        LogLevel.VERBOSE -> Verbose
        LogLevel.DEBUG   -> Debug
        LogLevel.INFO    -> Info
        LogLevel.WARNING -> Warning
        LogLevel.ERROR   -> Error
        LogLevel.FATAL   -> Fatal
        LogLevel.SILENT  -> Unknown
        LogLevel.UNKNOWN -> Unknown
    }

    @Composable
    fun barColor(level: LogLevel): Color = baseColor(level)

    @Composable
    fun rowBackground(level: LogLevel): Color = baseColor(level).copy(alpha = 0.1f)

    /**
     * Text color:
     * - Dark mode: base pastel is already light enough; optionally slightly darkened
     * - Light mode: blend with black at 45% to reach adequate contrast on white/light surfaces
     */
    fun textColor(level: LogLevel, isDark: Boolean): Color {
        val base = baseColor(level)
        return if (isDark) {
            // In dark mode the base pastels read well on dark surfaces
            base
        } else {
            // Blend toward black for light mode contrast
            base.blend(Color.Black, 0.45f)
        }
    }
}
