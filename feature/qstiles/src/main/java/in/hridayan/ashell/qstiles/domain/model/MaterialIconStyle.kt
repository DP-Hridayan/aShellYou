package `in`.hridayan.ashell.qstiles.domain.model

import androidx.annotation.StringRes
import `in`.hridayan.ashell.core.resources.R

enum class MaterialIconStyle(
    val fontAssetName: String,
    val codepointsAssetName: String,
    @StringRes val titleResId: Int
) {
    OUTLINED(
        fontAssetName = "MaterialIconsOutlined-Regular.otf",
        codepointsAssetName = "MaterialIconsOutlined-Regular.codepoints",
        titleResId = R.string.style_outlined
    ),
    FILLED(
        fontAssetName = "MaterialIcons-Regular.ttf",
        codepointsAssetName = "MaterialIcons-Regular.codepoints",
        titleResId = R.string.style_filled
    ),
    ROUNDED(
        fontAssetName = "MaterialIconsRound-Regular.otf",
        codepointsAssetName = "MaterialIconsRound-Regular.codepoints",
        titleResId = R.string.style_rounded
    ),
    SHARP(
        fontAssetName = "MaterialIconsSharp-Regular.otf",
        codepointsAssetName = "MaterialIconsSharp-Regular.codepoints",
        titleResId = R.string.style_sharp
    ),
    TWO_TONE(
        fontAssetName = "MaterialIconsTwoTone-Regular.otf",
        codepointsAssetName = "MaterialIconsTwoTone-Regular.codepoints",
        titleResId = R.string.style_two_tone
    )
}
