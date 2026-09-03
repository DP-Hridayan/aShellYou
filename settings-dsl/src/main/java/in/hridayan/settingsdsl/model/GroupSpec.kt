package `in`.hridayan.settingsdsl.model

import androidx.annotation.StringRes

/**
 * Internal blueprint for a group of settings items.
 * Created by [group], [category], [customSlot], [divider] DSL functions.
 */
internal sealed class GroupSpec {
    data class Items(val items: List<ItemSpec>) : GroupSpec()
    data class Category(
        @param:StringRes val titleResId: Int?,
        val title: String,
        val items: List<ItemSpec>
    ) : GroupSpec()

    data class Custom(val slot: CustomSlot) : GroupSpec()
    object Divider : GroupSpec()
}

