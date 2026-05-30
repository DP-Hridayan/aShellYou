@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import `in`.hridayan.ashell.settings.domain.model.AppFont

/**
 * Builds a [Typography] with every text style (baseline + emphasized)
 * overridden to use the selected [AppFont]'s [FontFamily].
 *
 * Memoised via [remember] so a new object is created only when
 * [fontFamilyId] actually changes, not on every recomposition.
 */
@Composable
fun appTypography(fontFamilyId: Int): Typography {
    return remember(fontFamilyId) {
        val font = AppFont.fromId(fontFamilyId).fontFamily
        val base = Typography()
        base.copy(
            // Baseline styles
            displayLarge = base.displayLarge.withFont(font),
            displayMedium = base.displayMedium.withFont(font),
            displaySmall = base.displaySmall.withFont(font),
            headlineLarge = base.headlineLarge.withFont(font),
            headlineMedium = base.headlineMedium.withFont(font),
            headlineSmall = base.headlineSmall.withFont(font),
            titleLarge = base.titleLarge.withFont(font),
            titleMedium = base.titleMedium.withFont(font),
            titleSmall = base.titleSmall.withFont(font),
            bodyLarge = base.bodyLarge.withFont(font),
            bodyMedium = base.bodyMedium.withFont(font),
            bodySmall = base.bodySmall.withFont(font),
            labelLarge = base.labelLarge.withFont(font),
            labelMedium = base.labelMedium.withFont(font),
            labelSmall = base.labelSmall.withFont(font),

            // Emphasized styles
            displayLargeEmphasized = base.displayLargeEmphasized.withFont(font),
            displayMediumEmphasized = base.displayMediumEmphasized.withFont(font),
            displaySmallEmphasized = base.displaySmallEmphasized.withFont(font),
            headlineLargeEmphasized = base.headlineLargeEmphasized.withFont(font),
            headlineMediumEmphasized = base.headlineMediumEmphasized.withFont(font),
            headlineSmallEmphasized = base.headlineSmallEmphasized.withFont(font),
            titleLargeEmphasized = base.titleLargeEmphasized.withFont(font),
            titleMediumEmphasized = base.titleMediumEmphasized.withFont(font),
            titleSmallEmphasized = base.titleSmallEmphasized.withFont(font),
            bodyLargeEmphasized = base.bodyLargeEmphasized.withFont(font),
            bodyMediumEmphasized = base.bodyMediumEmphasized.withFont(font),
            bodySmallEmphasized = base.bodySmallEmphasized.withFont(font),
            labelLargeEmphasized = base.labelLargeEmphasized.withFont(font),
            labelMediumEmphasized = base.labelMediumEmphasized.withFont(font),
            labelSmallEmphasized = base.labelSmallEmphasized.withFont(font),
        )
    }
}

/** Shorthand to override only the [FontFamily] on a [TextStyle]. */
private fun TextStyle.withFont(font: FontFamily): TextStyle = copy(fontFamily = font)