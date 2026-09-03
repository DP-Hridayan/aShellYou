package `in`.hridayan.settingsdsl.dsl

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.model.CustomSlot
import `in`.hridayan.settingsdsl.model.GroupSpec
import `in`.hridayan.settingsdsl.model.ItemSpec
import `in`.hridayan.settingsdsl.model.RadioButtonOption
import `in`.hridayan.settingsdsl.model.SettingsKey
import `in`.hridayan.settingsdsl.model.SettingsPage

/**
 * DSL marker annotation to restrict scope in settings DSL blocks.
 */
@DslMarker
annotation class SettingsDslMarker

/**
 * Base builder for a settings item with a title and visibility state.
 */
@SettingsDslMarker
abstract class BaseItemBuilder {
    var visible: Boolean = true
    var enabled: Boolean = true

    internal var titleResId: Int? = null
    internal var titleString: String = ""

    /** Sets the title using a string resource ID. */
    fun title(@StringRes resId: Int) {
        titleResId = resId
        titleString = ""
    }

    /** Sets the title using a plain string. */
    fun title(text: String) {
        titleString = text
        titleResId = null
    }
}

/**
 * Base builder for a settings item that also has a description.
 */
@SettingsDslMarker
abstract class DescribedItemBuilder : BaseItemBuilder() {
    internal var descriptionResId: Int? = null
    internal var descriptionString: String = ""

    /** Sets the description using a string resource ID. */
    fun description(@StringRes resId: Int) {
        descriptionResId = resId
        descriptionString = ""
    }

    /** Sets the description using a plain string. */
    fun description(text: String) {
        descriptionString = text
        descriptionResId = null
    }
}

/**
 * Base builder for a settings item that also has an icon.
 */
@SettingsDslMarker
abstract class IconItemBuilder : DescribedItemBuilder() {
    internal var iconResId: Int? = null
    internal var iconVector: ImageVector? = null

    /** Sets the icon using a drawable resource ID. */
    fun icon(@DrawableRes resId: Int) {
        iconResId = resId
        iconVector = null
    }

    /** Sets the icon using an ImageVector. */
    fun icon(vector: ImageVector) {
        iconVector = vector
        iconResId = null
    }
}

/**
 * Base builder for a settings item that supports an experimental flag badge.
 */
@SettingsDslMarker
abstract class BadgeItemBuilder : IconItemBuilder() {
    internal var experimentalFlagTextResId: Int? = null
    internal var experimentalFlagTextString: String = ""

    /** Sets the experimental flag text using a string resource ID. */
    fun experimentalFlagText(@StringRes resId: Int) {
        experimentalFlagTextResId = resId
        experimentalFlagTextString = ""
    }

    /** Sets the experimental flag text using a plain string. */
    fun experimentalFlagText(text: String) {
        experimentalFlagTextString = text
        experimentalFlagTextResId = null
    }
}

/**
 * Builder for a clickable settings item.
 */
@SettingsDslMarker
class ClickableItemBuilder internal constructor(private val key: SettingsKey<*>) :
    BadgeItemBuilder() {
    internal fun build(): ItemSpec = ItemSpec.ClickableSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = titleString,
        descriptionResId = descriptionResId,
        descriptionString = descriptionString,
        iconResId = iconResId,
        iconVector = iconVector,
        experimentalFlagTextResId = experimentalFlagTextResId,
        experimentalFlagText = experimentalFlagTextString
    )
}

/**
 * Builder for a switch settings item.
 */
@SettingsDslMarker
class SwitchItemBuilder internal constructor(private val key: SettingsKey<*>) :
    BadgeItemBuilder() {
    internal fun build(): ItemSpec = ItemSpec.SwitchSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = titleString,
        descriptionResId = descriptionResId,
        descriptionString = descriptionString,
        iconResId = iconResId,
        iconVector = iconVector,
        experimentalFlagTextResId = experimentalFlagTextResId,
        experimentalFlagText = experimentalFlagTextString
    )
}

/**
 * Builder for a switch banner item.
 */
@SettingsDslMarker
class SwitchBannerItemBuilder internal constructor(private val key: SettingsKey<*>) :
    BaseItemBuilder() {
    internal fun build(): ItemSpec = ItemSpec.SwitchBannerSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = titleString
    )
}

/**
 * Builder for a radio group settings item.
 */
@SettingsDslMarker
class RadioGroupItemBuilder internal constructor(private val key: SettingsKey<*>) {
    var visible: Boolean = true
    var enabled: Boolean = true
    internal var options: List<RadioButtonOption> = emptyList()

    /** Sets the options for the radio group. */
    fun options(vararg optionsList: RadioButtonOption) {
        options = optionsList.toList()
    }

    /** Sets the options for the radio group. */
    fun options(optionsList: List<RadioButtonOption>) {
        options = optionsList
    }

    internal fun build(): ItemSpec = ItemSpec.RadioGroupSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        options = options
    )
}

/**
 * Builder for a button group settings item.
 */
@SettingsDslMarker
class ButtonGroupItemBuilder internal constructor(private val key: SettingsKey<*>) {
    var visible: Boolean = true
    var enabled: Boolean = true
    internal var options: List<ButtonGroupOption> = emptyList()

    /** Sets the options for the button group. */
    fun options(vararg optionsList: ButtonGroupOption) {
        options = optionsList.toList()
    }

    /** Sets the options for the button group. */
    fun options(optionsList: List<ButtonGroupOption>) {
        options = optionsList
    }

    internal fun build(): ItemSpec = ItemSpec.ButtonGroupSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        options = options
    )
}

/**
 * Scope for adding items to a settings group.
 * You can optionally set a title to render this group as a categorized section.
 */
@SettingsDslMarker
class GroupScope internal constructor() {
    internal var titleResId: Int? = null
    internal var titleString: String = ""
    internal val items = mutableListOf<ItemSpec>()

    /** Sets the group title using a string resource ID. */
    fun title(@StringRes resId: Int) {
        titleResId = resId
        titleString = ""
    }

    /** Sets the group title using a plain string. */
    fun title(text: String) {
        titleString = text
        titleResId = null
    }

    /**
     * Creates a settings item with a toggle switch using a builder block.
     *
     * @param key Unique identifier for this setting.
     * @param block Builder block for configuring the item.
     */
    fun switchItem(key: SettingsKey<*>, block: SwitchItemBuilder.() -> Unit) {
        items.add(SwitchItemBuilder(key).apply(block).build())
    }

    /**
     * Creates a full-width switch banner item using a builder block.
     *
     * @param key Unique identifier for this setting.
     * @param block Builder block for configuring the item.
     */
    fun switchBannerItem(key: SettingsKey<*>, block: SwitchBannerItemBuilder.() -> Unit) {
        items.add(SwitchBannerItemBuilder(key).apply(block).build())
    }

    /**
     * Creates a tappable settings item that navigates or opens a dialog using a builder block.
     *
     * @param key Unique identifier for this setting.
     * @param block Builder block for configuring the item.
     */
    fun clickableItem(key: SettingsKey<*>, block: ClickableItemBuilder.() -> Unit) {
        items.add(ClickableItemBuilder(key).apply(block).build())
    }

    /**
     * Creates a settings item that renders a group of mutually exclusive radio options using a builder block.
     *
     * @param key Unique identifier for this setting.
     * @param block Builder block for configuring the item.
     */
    fun radioGroupItem(key: SettingsKey<*>, block: RadioGroupItemBuilder.() -> Unit) {
        items.add(RadioGroupItemBuilder(key).apply(block).build())
    }

    /**
     * Creates a settings item that renders a segmented/button group selector using a builder block.
     *
     * @param key Unique identifier for this setting.
     * @param block Builder block for configuring the item.
     */
    fun buttonGroupItem(key: SettingsKey<*>, block: ButtonGroupItemBuilder.() -> Unit) {
        items.add(ButtonGroupItemBuilder(key).apply(block).build())
    }
}

/**
 * Scope for building a SettingsPage.
 */
@SettingsDslMarker
class SettingsPageBuilder internal constructor(
    private val screenId: String? = null
) {
    internal var screenTitleResId: Int? = null
    internal var screenTitleString: String = ""
    internal val groups = mutableListOf<GroupSpec>()

    /** Sets the screen title using a string resource ID. */
    fun title(@StringRes resId: Int) {
        screenTitleResId = resId
        screenTitleString = ""
    }

    /** Sets the screen title using a plain string. */
    fun title(text: String) {
        screenTitleString = text
        screenTitleResId = null
    }

    /**
     * Creates a group of items.
     *
     * If you call `title(...)` inside the builder block, the group will render as
     * a categorized section with a header. Otherwise, it will render as an uncategorized block.
     *
     * @param block Builder block for configuring the items in this group.
     */
    fun group(block: GroupScope.() -> Unit) {
        val scope = GroupScope().apply(block)
        groups.add(
            GroupSpec.Group(
                titleResId = scope.titleResId,
                title = scope.titleString,
                items = scope.items
            )
        )
    }

    /**
     * Creates a categorized group of items with a string resource header label.
     *
     * @param titleResId String resource for the group title.
     * @param block Builder block for configuring the items in this group.
     */
    fun group(@StringRes titleResId: Int, block: GroupScope.() -> Unit) {
        group {
            title(titleResId)
            block()
        }
    }

    /**
     * Creates a categorized group of items with a plain string header label.
     *
     * @param title Plain string for the group title.
     * @param block Builder block for configuring the items in this group.
     */
    fun group(title: String, block: GroupScope.() -> Unit) {
        group {
            title(title)
            block()
        }
    }

    /**
     * Inserts a custom composable slot identified by the given [CustomSlot].
     */
    fun customSlot(slot: CustomSlot) {
        groups.add(GroupSpec.Custom(slot))
    }

    /**
     * Inserts a horizontal visual divider between groups.
     */
    fun divider() {
        groups.add(GroupSpec.Divider)
    }

    internal fun build(): SettingsPage =
        SettingsPage(
            groups = groups,
            screenId = screenId,
            screenTitleResId = screenTitleResId
        )
}



