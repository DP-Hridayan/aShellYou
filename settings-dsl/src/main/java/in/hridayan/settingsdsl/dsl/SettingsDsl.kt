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
 * @param title String resource for the display title.
 * @param description Optional string resource for the subtitle/description.
 * @param icon Optional drawable resource for the leading icon.
 * @param iconVector Optional [ImageVector] for the leading icon.
 * @param visible Whether this item appears in the list. Defaults to `true`.
 */
fun switchItem(
    key: SettingsKey<*>,
    @StringRes title: Int,
    @StringRes description: Int? = null,
    @DrawableRes icon: Int? = null,
    iconVector: ImageVector? = null,
    visible: Boolean = true,
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.SwitchSpec(
        key = key,
        isVisible = visible,
        titleResId = title,
        titleString = "",
        descriptionResId = description,
        descriptionString = "",
        iconResId = icon,
        iconVector = iconVector,
    )
)

/**
 * Creates a full-width switch banner item (e.g. a prominent enable/disable toggle at the top of a page).
 */
fun switchBannerItem(
    key: SettingsKey<*>,
    @StringRes title: Int,
    visible: Boolean = true,
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.SwitchBannerSpec(
        key = key,
        isVisible = visible,
        titleResId = title,
        titleString = ""
    )
)

/**
 * Creates a tappable settings item that navigates or opens a dialog.
 */
fun clickableItem(
    key: SettingsKey<*>,
    @StringRes title: Int,
    @StringRes description: Int? = null,
    @DrawableRes icon: Int? = null,
    iconVector: ImageVector? = null,
    visible: Boolean = true,
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.ClickableSpec(
        key = key,
        isVisible = visible,
        titleResId = title,
        titleString = "",
        descriptionResId = description,
        descriptionString = "",
        iconResId = icon,
        iconVector = iconVector,
    )
)

/**
 * Creates a settings item that renders a group of mutually exclusive radio options.
 */
fun radioGroupItem(
    key: SettingsKey<*>,
    options: List<RadioButtonOption>,
    visible: Boolean = true,
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.RadioGroupSpec(
        key = key,
        isVisible = visible,
        options = options
    )
)

/**
 * Creates a settings item that renders a segmented/button group selector.
 */
fun buttonGroupItem(
    key: SettingsKey<*>,
    options: List<ButtonGroupOption>,
    visible: Boolean = true,
): SettingsItemSpec = SettingsItemSpec(
    ItemSpec.ButtonGroupSpec(
        key = key,
        isVisible = visible,
        options = options
    )
)

/**
 * Creates an uncategorized group of items (no header label).
 */
fun group(vararg items: SettingsItemSpec): SettingsGroup =
    SettingsGroup(GroupSpec.Items(items.map { it.spec }))

/** @see group */
fun group(items: List<SettingsItemSpec>): SettingsGroup =
    SettingsGroup(GroupSpec.Items(items.map { it.spec }))

/**
 * Creates a categorized group of items with a string-resource header label.
 */
fun category(
    @StringRes title: Int,
    vararg items: SettingsItemSpec,
): SettingsGroup = SettingsGroup(GroupSpec.Category(title, items.map { it.spec }))

/** @see category */
fun category(
    @StringRes title: Int,
    items: List<SettingsItemSpec>,
): SettingsGroup = SettingsGroup(GroupSpec.Category(title, items.map { it.spec }))

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
 * Creates a [SettingsPage] — the top-level container for a single settings screen.
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
