package `in`.hridayan.settingsdsl.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.settingsdsl.ui.card.CustomCardShape

/**
 * The fully-resolved, display-ready representation of a single settings item.
 *
 * This is the **only** type that views and screens should ever interact with.
 * Card corner [shape] is computed automatically based on list position.
 *
 * Do not construct this manually — it is produced by [SettingsPage.groups] or
 * [SettingsPage.resolveItem].
 */
data class SettingsItem(
    /** The key identifying this item. */
    val key: SettingsKey<*>,

    /** Resolved display title. */
    val title: String,

    /** Resolved display description. Empty string if none. */
    val description: String,

    /**
     * Resolved icon as [ImageVector]. Null if the icon was specified as a drawable res
     * (use [iconResId] instead) or no icon defined.
     */
    val icon: ImageVector?,

    /**
     * Drawable resource ID for the leading icon. Non-null only when the icon was specified
     * via [icon] param in the DSL builder and no [ImageVector] override was provided.
     * Use `painterResource(iconResId!!)` in views to render this.
     */
    @param:DrawableRes val iconResId: Int?,

    /**
     * Auto-computed card corner shape based on this item's position within its
     * visibility-filtered group. Do not override — it is always correct.
     */
    val shape: CustomCardShape,

    /** Describes how this item behaves when rendered and interacted with. */
    val behavior: ItemBehavior,

    /** True if this item should be visually highlighted (e.g. from a search result). */
    val isHighlighted: Boolean = false,
)

/**
 * Describes the interactive behavior of a [SettingsItem].
 */
sealed interface ItemBehavior {

    /** Item is tapped to navigate or open a dialog. */
    data object Clickable : ItemBehavior

    /** Item has a toggle switch. */
    data object Switch : ItemBehavior

    /** Item renders as a full-width switch banner. */
    data object SwitchBanner : ItemBehavior

    /** Item contains a list of mutually exclusive radio options. */
    data class RadioGroup(val options: List<RadioButtonOption>) : ItemBehavior

    /** Item contains a segmented button group. */
    data class ButtonGroup(val options: List<ButtonGroupOption>) : ItemBehavior
}
