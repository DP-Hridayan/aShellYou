package `in`.hridayan.ashell.guides.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class GuideItem(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val url: String,
    val category: GuideCategory
)
