package `in`.hridayan.settingsdsl.dsl

import androidx.annotation.StringRes
import `in`.hridayan.settingsdsl.model.CustomSlot
import `in`.hridayan.settingsdsl.model.GroupSpec
import `in`.hridayan.settingsdsl.model.SettingsGroup
import `in`.hridayan.settingsdsl.model.SettingsItemSpec
import `in`.hridayan.settingsdsl.model.SettingsKey
import `in`.hridayan.settingsdsl.model.SettingsPage


/**
 * Creates an uncategorized group of items.
 *
 * @param items The items to include in this group.
 */
fun group(vararg items: SettingsItemSpec): SettingsGroup =
    SettingsGroup(GroupSpec.Items(items.map { it.spec }))

/** @see group */
fun group(items: List<SettingsItemSpec>): SettingsGroup =
    SettingsGroup(GroupSpec.Items(items.map { it.spec }))

/**
 * Creates a categorized group of items with a header label.
 *
 * @param titleResId String resource for the category title.
 * @param items The items to include in this category.
 */
fun category(
    @StringRes titleResId: Int,
    vararg items: SettingsItemSpec,
): SettingsGroup = SettingsGroup(
    GroupSpec.Category(
        titleResId = titleResId,
        title = "",
        items = items.map { it.spec }
    )
)

/**
 * Creates a categorized group of items with a header label.
 *
 * @param title Plain string for the category title.
 * @param items The items to include in this category.
 */
fun category(
    title: String,
    vararg items: SettingsItemSpec,
): SettingsGroup = SettingsGroup(
    GroupSpec.Category(
        titleResId = null,
        title = title,
        items = items.map { it.spec }
    )
)

/** @see category */
fun category(
    @StringRes titleResId: Int,
    items: List<SettingsItemSpec>,
): SettingsGroup = SettingsGroup(
    GroupSpec.Category(
        titleResId = titleResId,
        title = "",
        items = items.map { it.spec }
    )
)

/** @see category */
fun category(
    title: String,
    items: List<SettingsItemSpec>,
): SettingsGroup = SettingsGroup(
    GroupSpec.Category(
        titleResId = null,
        title = title,
        items = items.map { it.spec }
    )
)

/**
 * Inserts a custom composable slot identified by the given [CustomSlot].
 */
fun customSlot(slot: CustomSlot): SettingsGroup =
    SettingsGroup(GroupSpec.Custom(slot))

/**
 * Inserts a horizontal visual divider between groups.
 */
fun divider(): SettingsGroup = SettingsGroup(GroupSpec.Divider)

/**
 * Creates a [SettingsPage] — the top-level container for a settings screen.
 *
 * @param groups The groups that make up this page.
 */
fun settingsPage(vararg groups: SettingsGroup): SettingsPage =
    SettingsPage(groups.map { it.spec })

/** @see settingsPage */
fun settingsPage(groups: List<SettingsGroup>): SettingsPage =
    SettingsPage(groups.map { it.spec })

/**
 * Creates a [SettingsPage] with screen metadata for search auto-registration.
 *
 * When pages carry their own [screenId] and [screenTitle], the search engine can
 * build an index directly from the pages — no manual string maps needed.
 *
 * @param screenTitle String resource for the screen's display title.
 * @param screenId    Stable app-defined identifier for this screen (e.g. `"look_and_feel"`).
 * @param groups      The groups that make up this page.
 */
fun settingsPage(
    @StringRes screenTitle: Int,
    screenId: String,
    vararg groups: SettingsGroup,
): SettingsPage = SettingsPage(
    groups = groups.map { it.spec },
    screenId = screenId,
    screenTitleResId = screenTitle,
)

/**
 * Creates a settings item with a toggle switch using a builder block.
 *
 * @param key Unique identifier for this setting.
 * @param block Builder block for configuring the item.
 */
fun switchItem(key: SettingsKey<*>, block: SwitchItemBuilder.() -> Unit): SettingsItemSpec {
    return SwitchItemBuilder(key).apply(block).build()
}

/**
 * Creates a full-width switch banner item using a builder block.
 *
 * @param key Unique identifier for this setting.
 * @param block Builder block for configuring the item.
 */
fun switchBannerItem(
    key: SettingsKey<*>,
    block: SwitchBannerItemBuilder.() -> Unit
): SettingsItemSpec {
    return SwitchBannerItemBuilder(key).apply(block).build()
}

/**
 * Creates a tappable settings item that navigates or opens a dialog using a builder block.
 *
 * @param key Unique identifier for this setting.
 * @param block Builder block for configuring the item.
 */
fun clickableItem(key: SettingsKey<*>, block: ClickableItemBuilder.() -> Unit): SettingsItemSpec {
    return ClickableItemBuilder(key).apply(block).build()
}

/**
 * Creates a settings item that renders a group of mutually exclusive radio options using a builder block.
 *
 * @param key Unique identifier for this setting.
 * @param block Builder block for configuring the item.
 */
fun radioGroupItem(key: SettingsKey<*>, block: RadioGroupItemBuilder.() -> Unit): SettingsItemSpec {
    return RadioGroupItemBuilder(key).apply(block).build()
}

/**
 * Creates a settings item that renders a segmented/button group selector using a builder block.
 *
 * @param key Unique identifier for this setting.
 * @param block Builder block for configuring the item.
 */
fun buttonGroupItem(
    key: SettingsKey<*>,
    block: ButtonGroupItemBuilder.() -> Unit
): SettingsItemSpec {
    return ButtonGroupItemBuilder(key).apply(block).build()
}
