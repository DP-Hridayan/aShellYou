package `in`.hridayan.ashell.settings.presentation.model

import `in`.hridayan.ashell.core.common.domain.model.AppFont
import `in`.hridayan.ashell.settings.domain.model.CustomFontEntity

sealed class FontOption {

    /** Unique ID used as the selection value stored in preferences. */
    abstract val listId: Int

    data class Predefined(val appFont: AppFont, val labelResId: Int) : FontOption() {
        override val listId: Int get() = appFont.id
    }

    /** [listId] is the raw DB id, which is always >= [AppFont.CUSTOM_FONT_ID_OFFSET] due to sequence seeding. */
    data class Custom(val entity: CustomFontEntity) : FontOption() {
        override val listId: Int get() = entity.id
    }
}
