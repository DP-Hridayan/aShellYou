package `in`.hridayan.settingsdsl.model

import androidx.annotation.StringRes
import `in`.hridayan.settingsdsl.dsl.category
import `in`.hridayan.settingsdsl.dsl.customSlot
import `in`.hridayan.settingsdsl.dsl.divider
import `in`.hridayan.settingsdsl.dsl.group
import `in`.hridayan.settingsdsl.dsl.settingsPage

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

/**
 * Opaque wrapper around [GroupSpec] used as the parameter type for [SettingsPage] construction.
 *
 * Consumers create instances via DSL functions ([group], [category], [customSlot], [divider])
 * and pass them to [settingsPage]. The internal [spec] is not accessible outside the library.
 */
class SettingsGroup internal constructor(internal val spec: GroupSpec)
