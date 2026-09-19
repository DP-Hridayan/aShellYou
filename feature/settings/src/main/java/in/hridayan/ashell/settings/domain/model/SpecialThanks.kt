package `in`.hridayan.ashell.settings.domain.model

import androidx.annotation.StringRes

data class SpecialThanks(
    val name: String,
    @StringRes val descriptionRes: Int,
    val url: String,
    val avatarAssetPath: String
)
