package `in`.hridayan.settingsdsl.dsl

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.model.CustomSlot
import `in`.hridayan.settingsdsl.model.GroupSpec
import `in`.hridayan.settingsdsl.model.ItemSpec
import `in`.hridayan.settingsdsl.model.RadioButtonOption
import `in`.hridayan.settingsdsl.model.SettingsGroup
import `in`.hridayan.settingsdsl.model.SettingsItemSpec
import `in`.hridayan.settingsdsl.model.SettingsKey
import `in`.hridayan.settingsdsl.model.SettingsPage

/**
 * Creates a settings item with a toggle switch.
 *
 * @param key Unique identifier for this setting.
 * @param titleResId String resource for the display title.
 * @param title Plain string for the display title (used if [titleResId] is null).
 * @param descriptionResId Optional string resource for the subtitle/description.
 * @param description Optional plain string for the subtitle/description.
 * @param icon Optional drawable resource for the leading icon.
 * @param iconVector Optional [ImageVector] for the leading icon.
 * @param visible Whether this item appears in the list.
 * @param enabled Whether the user can interact with this item.
 * @param enableExperimentalFlag Whether to show an experimental badge.
 * @param experimentalFlagTextResId String resource for the experimental badge text.
 * @param experimentalFlagText Plain string for the experimental badge text.
 */
fun switchItem(
    key: SettingsKey<*>,
    @StringRes titleResId: Int? = null,
    title: String = "",
    @StringRes descriptionResId: Int? = null,
    description: String = "",
    @DrawableRes icon: Int? = null,
    iconVector: ImageVector? = null,
    visible: Boolean = true,
    enabled: Boolean = true,
    enableExperimentalFlag: Boolean = false,
    @StringRes experimentalFlagTextResId: Int? = null,
    experimentalFlagText: String = "Experimental"
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.SwitchSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = title,
        descriptionResId = descriptionResId,
        descriptionString = description,
        iconResId = icon,
        iconVector = iconVector,
        enableExperimentalFlag = enableExperimentalFlag,
        experimentalFlagTextResId = experimentalFlagTextResId,
        experimentalFlagText = experimentalFlagText
    )
)

/**
 * Creates a full-width switch banner item.
 *
 * @param key Unique identifier for this setting.
 * @param titleResId String resource for the banner title.
 * @param visible Whether this banner appears in the list.
 * @param enabled Whether the user can interact with this banner.
 */
fun switchBannerItem(
    key: SettingsKey<*>,
    @StringRes titleResId: Int? = null,
    visible: Boolean = true,
    enabled: Boolean = true
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.SwitchBannerSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = ""
    )
)

/**
 * Creates a full-width switch banner item.
 *
 * @param key Unique identifier for this setting.
 * @param title Plain string for the banner title.
 * @param visible Whether this banner appears in the list.
 * @param enabled Whether the user can interact with this banner.
 */
fun switchBannerItem(
    key: SettingsKey<*>,
    title: String = "",
    visible: Boolean = true,
    enabled: Boolean = true
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.SwitchBannerSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        titleResId = null,
        titleString = title
    )
)

/**
 * Creates a tappable settings item that navigates or opens a dialog.
 *
 * @param key Unique identifier for this setting.
 * @param titleResId String resource for the display title.
 * @param title Plain string for the display title.
 * @param descriptionResId Optional string resource for the subtitle/description.
 * @param description Optional plain string for the subtitle/description.
 * @param icon Optional drawable resource for the leading icon.
 * @param iconVector Optional [ImageVector] for the leading icon.
 * @param visible Whether this item appears in the list.
 * @param enabled Whether the user can interact with this item.
 * @param enableExperimentalFlag Whether to show an experimental badge.
 * @param experimentalFlagTextResId String resource for the experimental badge text.
 * @param experimentalFlagText Plain string for the experimental badge text.
 */
fun clickableItem(
    key: SettingsKey<*>,
    @StringRes titleResId: Int? = null,
    title: String = "",
    @StringRes descriptionResId: Int? = null,
    description: String = "",
    @DrawableRes icon: Int? = null,
    iconVector: ImageVector? = null,
    visible: Boolean = true,
    enabled: Boolean = true,
    enableExperimentalFlag: Boolean = false,
    @StringRes experimentalFlagTextResId: Int? = null,
    experimentalFlagText: String = "Experimental"
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.ClickableSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = title,
        descriptionResId = descriptionResId,
        descriptionString = description,
        iconResId = icon,
        iconVector = iconVector,
        enableExperimentalFlag = enableExperimentalFlag,
        experimentalFlagTextResId = experimentalFlagTextResId,
        experimentalFlagText = experimentalFlagText
    )
)

/**
 * Creates a settings item that renders a group of mutually exclusive radio options.
 *
 * @param key Unique identifier for this setting.
 * @param options List of [RadioButtonOption] to display.
 * @param visible Whether this item appears in the list.
 * @param enabled Whether the user can interact with this item.
 */
fun radioGroupItem(
    key: SettingsKey<*>,
    options: List<RadioButtonOption>,
    visible: Boolean = true,
    enabled: Boolean = true
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.RadioGroupSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        options = options
    )
)

/**
 * Creates a settings item that renders a segmented/button group selector.
 *
 * @param key Unique identifier for this setting.
 * @param options List of [ButtonGroupOption] to display.
 * @param visible Whether this item appears in the list.
 * @param enabled Whether the user can interact with this item.
 */
fun buttonGroupItem(
    key: SettingsKey<*>,
    options: List<ButtonGroupOption>,
    visible: Boolean = true,
    enabled: Boolean = true
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.ButtonGroupSpec(
        key = key,
        isVisible = visible,
        enabled = enabled,
        options = options
    )
)

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
        items = items.map { it.spec })
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
        items = items.map { it.spec })
)

/** @see category */
fun category(
    @StringRes titleResId: Int,
    items: List<SettingsItemSpec>,
): SettingsGroup = SettingsGroup(
    GroupSpec.Category(
        titleResId = titleResId,
        title = "",
        items = items.map { it.spec })
)

/** @see category */
fun category(
    title: String,
    items: List<SettingsItemSpec>,
): SettingsGroup = SettingsGroup(
    GroupSpec.Category(
        titleResId = null,
        title = title,
        items = items.map { it.spec })
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
