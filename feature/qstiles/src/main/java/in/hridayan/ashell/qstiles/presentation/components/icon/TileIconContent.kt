package `in`.hridayan.ashell.qstiles.presentation.components.icon

import android.graphics.Typeface
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.model.FontLoadState

private const val DEFAULT_GLYPH_FONT_SIZE = 24

@Composable
fun TileIconContent(
    iconId: String,
    fontLoadState: FontLoadState,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    glyphFontSize: TextUnit = DEFAULT_GLYPH_FONT_SIZE.sp,
) {
    val bundledIcon = TileIconProvider.iconById[iconId]

    if (bundledIcon != null) {
        Icon(
            modifier = modifier,
            painter = painterResource(bundledIcon.resId),
            contentDescription = iconId,
            tint = tint,
        )
        return
    }

    val ready = fontLoadState as? FontLoadState.Ready
    if (ready != null && !TileIconProvider.isBundledIcon(iconId)) {
        val iconName = TileIconProvider.extractCloudIconName(iconId)
        val entry = ready.icons.firstOrNull { it.name == iconName }

        if (entry != null) {
            MaterialIconGlyph(
                typeface = ready.typeface,
                codepoint = entry.codepoint,
                color = tint,
                fontSize = glyphFontSize,
            )
            return
        }
    }

    Icon(
        modifier = modifier,
        painter = painterResource(R.drawable.ic_adb),
        contentDescription = iconId,
        tint = tint,
    )
}

@Composable
fun MaterialIconGlyph(
    typeface: Typeface,
    codepoint: Int,
    color: Color = LocalContentColor.current,
    fontSize: TextUnit = DEFAULT_GLYPH_FONT_SIZE.sp,
) {
    val fontFamily = remember(typeface) {
        FontFamily(androidx.compose.ui.text.font.Typeface(typeface))
    }

    Text(
        text = String(Character.toChars(codepoint)),
        fontFamily = fontFamily,
        fontSize = fontSize,
        color = color,
    )
}
