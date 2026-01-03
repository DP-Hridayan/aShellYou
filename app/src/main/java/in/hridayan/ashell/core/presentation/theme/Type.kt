@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import `in`.hridayan.ashell.R

@Composable
fun appTypography(): Typography {
    return Typography(
        bodySmall = MaterialTheme.typography.bodySmall.copy(lineHeight = MaterialTheme.typography.bodyMedium.lineHeight),
        bodySmallEmphasized = MaterialTheme.typography.bodySmallEmphasized.copy(lineHeight = MaterialTheme.typography.bodyMedium.lineHeight)
    )
}

object CustomFontFamily {
    val robotoFlex = FontFamily(Font(resId = R.font.roboto_flex_var))
}