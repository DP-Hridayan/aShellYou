package `in`.hridayan.settingsdsl.dsl

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.model.ItemBehavior
import `in`.hridayan.settingsdsl.model.RadioButtonOption
import `in`.hridayan.settingsdsl.model.SettingsGraph
import `in`.hridayan.settingsdsl.model.SettingsGraphGroup
import `in`.hridayan.settingsdsl.model.SettingsNode

@DslMarker
annotation class SettingsDslMarker

/**
 * Base builder shared by all settings item builder types.
 *
 * Provides [title], [description][DescribedItemBuilder], [visible], [enabled], and [onClick]
 * configuration.
 */
@SettingsDslMarker
abstract class BaseItemBuilder {
    internal var staticTitleRes: Int? = null
    internal var staticTitleString: String = ""
    internal var dynamicTitle: (@Composable () -> String)? = null
    internal var enabled: Boolean = true
    internal var visibleLambda: () -> Boolean = { true }
    internal var onClickOverride: ((Any) -> Unit)? = null

    /** Sets the title from a string resource. */
    fun title(@StringRes resId: Int) {
        staticTitleRes = resId
        staticTitleString = ""
        dynamicTitle = null
    }

    /** Sets the title from a plain string. */
    fun title(text: String) {
        staticTitleString = text
        staticTitleRes = null
        dynamicTitle = null
    }

    /**
     * Sets a dynamic title via a [Composable] lambda, executed on every recomposition and so
     * suitable for reading Compose state.
     *
     * @param block Composable lambda that returns the display title string.
     */
    fun title(block: @Composable () -> String) {
        dynamicTitle = block
        staticTitleRes = null
        staticTitleString = ""
    }

    /**
     * Controls whether this item is rendered.
     *
     * The lambda is evaluated in pure Kotlin context � do not call Compose APIs inside it.
     * Read pre-computed values from the enclosing composable scope instead. The containing
     * composable must recompose for visibility changes to take effect.
     *
     * @param block Pure-Kotlin lambda returning true if the item should be visible.
     */
    fun visible(block: () -> Boolean) {
        visibleLambda = block
    }

    /** Controls whether this item is interactable. */
    fun enabled(value: Boolean) {
        enabled = value
    }

    /**
     * Registers a per-item click or toggle handler.
     *
     * For [ItemBehavior.Clickable] items this is the tap action. For [ItemBehavior.Switch] and
     * [ItemBehavior.SwitchBanner] items this overrides the global
     * [in.hridayan.settingsdsl.ui.OnClickDefaults.onSwitchItem].
     *
     * @param block Lambda receiving the item key.
     */
    fun onClick(block: (Any) -> Unit) {
        onClickOverride = block
    }
}

/**
 * Extends [BaseItemBuilder] with description configuration.
 */
@SettingsDslMarker
abstract class DescribedItemBuilder : BaseItemBuilder() {
    internal var staticDescRes: Int? = null
    internal var staticDescString: String = ""
    internal var dynamicDescription: (@Composable () -> String)? = null

    /** Sets the description from a string resource. */
    fun description(@StringRes resId: Int) {
        staticDescRes = resId
        staticDescString = ""
        dynamicDescription = null
    }

    /** Sets the description from a plain string. */
    fun description(text: String) {
        staticDescString = text
        staticDescRes = null
        dynamicDescription = null
    }

    /**
     * Sets a dynamic description via a [Composable] lambda, executed on every recomposition and so
     * suitable for reading Compose state.
     *
     * @param block Composable lambda that returns the display description string.
     */
    fun description(block: @Composable () -> String) {
        dynamicDescription = block
        staticDescRes = null
        staticDescString = ""
    }
}

/**
 * Extends [DescribedItemBuilder] with icon configuration.
 */
@SettingsDslMarker
abstract class IconItemBuilder : DescribedItemBuilder() {
    internal var iconResId: Int? = null
    internal var iconVector: ImageVector? = null

    /** Sets the leading icon from a drawable resource. */
    fun icon(@DrawableRes resId: Int) {
        iconResId = resId
        iconVector = null
    }

    /** Sets the leading icon from an [ImageVector]. */
    fun icon(vector: ImageVector) {
        iconVector = vector
        iconResId = null
    }
}

/**
 * Extends [IconItemBuilder] with experimental badge configuration.
 */
@SettingsDslMarker
abstract class BadgeItemBuilder : IconItemBuilder() {
    internal var experimentalFlagTextRes: Int? = null
    internal var experimentalFlagTextString: String = ""

    /** Sets the experimental badge text from a string resource. */
    fun experimentalFlagText(@StringRes resId: Int) {
        experimentalFlagTextRes = resId
        experimentalFlagTextString = ""
    }

    /** Sets the experimental badge text from a plain string. */
    fun experimentalFlagText(text: String) {
        experimentalFlagTextString = text
        experimentalFlagTextRes = null
    }
}

/**
 * Builder for a clickable settings item.
 *
 * Use [onClick] (inherited from [BaseItemBuilder]) to define the tap action. There is no global
 * default for clickable items � each item must declare its own handler.
 *
 * @param key The developer-supplied key for this item.
 */
@SettingsDslMarker
class ClickableItemBuilder internal constructor(private val key: Any) : BadgeItemBuilder() {
    internal fun build(): SettingsNode = SettingsNode(
        key = key,
        keyName = key.toString(),
        isVisible = visibleLambda,
        dynamicTitle = dynamicTitle,
        staticTitleRes = staticTitleRes,
        staticTitleString = staticTitleString,
        dynamicDescription = dynamicDescription,
        staticDescRes = staticDescRes,
        staticDescString = staticDescString,
        iconResId = iconResId,
        iconVector = iconVector,
        experimentalFlagTextRes = experimentalFlagTextRes,
        experimentalFlagTextString = experimentalFlagTextString,
        enabled = enabled,
        behavior = ItemBehavior.Clickable,
        onClickOverride = onClickOverride,
    )
}

/**
 * Builder for a switch settings item.
 *
 * Use [onClick] to override the global [in.hridayan.settingsdsl.ui.OnClickDefaults.onSwitchItem]
 * for this item only. Use [isChecked] to override the global
 * [in.hridayan.settingsdsl.ui.OnClickDefaults.isChecked] for this item only.
 *
 * @param key The developer-supplied key for this item.
 */
@SettingsDslMarker
class SwitchItemBuilder internal constructor(private val key: Any) : BadgeItemBuilder() {
    internal var isCheckedOverride: ((Any) -> Boolean)? = null

    /**
     * Registers a per-item boolean state reader that overrides the global
     * [in.hridayan.settingsdsl.ui.OnClickDefaults.isChecked] for this item only.
     *
     * @param block Lambda receiving the item key and returning whether the switch is checked.
     */
    fun isChecked(block: (Any) -> Boolean) {
        isCheckedOverride = block
    }

    internal fun build(): SettingsNode = SettingsNode(
        key = key,
        keyName = key.toString(),
        isVisible = visibleLambda,
        dynamicTitle = dynamicTitle,
        staticTitleRes = staticTitleRes,
        staticTitleString = staticTitleString,
        dynamicDescription = dynamicDescription,
        staticDescRes = staticDescRes,
        staticDescString = staticDescString,
        iconResId = iconResId,
        iconVector = iconVector,
        experimentalFlagTextRes = experimentalFlagTextRes,
        experimentalFlagTextString = experimentalFlagTextString,
        enabled = enabled,
        behavior = ItemBehavior.Switch,
        onToggleOverride = onClickOverride,
        isCheckedOverride = isCheckedOverride,
    )
}

/**
 * Builder for a full-width switch banner item.
 *
 * Use [onClick] to override the global [in.hridayan.settingsdsl.ui.OnClickDefaults.onSwitchItem]
 * for this item only. Use [isChecked] to override the global
 * [in.hridayan.settingsdsl.ui.OnClickDefaults.isChecked] for this item only.
 *
 * @param key The developer-supplied key for this item.
 */
@SettingsDslMarker
class SwitchBannerItemBuilder internal constructor(private val key: Any) : BaseItemBuilder() {
    internal var isCheckedOverride: ((Any) -> Boolean)? = null

    /**
     * Registers a per-item boolean state reader that overrides the global
     * [in.hridayan.settingsdsl.ui.OnClickDefaults.isChecked] for this item only.
     *
     * @param block Lambda receiving the item key and returning whether the switch is checked.
     */
    fun isChecked(block: (Any) -> Boolean) {
        isCheckedOverride = block
    }

    internal fun build(): SettingsNode = SettingsNode(
        key = key,
        keyName = key.toString(),
        isVisible = visibleLambda,
        dynamicTitle = dynamicTitle,
        staticTitleRes = staticTitleRes,
        staticTitleString = staticTitleString,
        dynamicDescription = null,
        staticDescRes = null,
        staticDescString = "",
        iconResId = null,
        iconVector = null,
        experimentalFlagTextRes = null,
        experimentalFlagTextString = "",
        enabled = enabled,
        behavior = ItemBehavior.SwitchBanner,
        onToggleOverride = onClickOverride,
        isCheckedOverride = isCheckedOverride,
    )
}

/**
 * Builder for a radio group settings item.
 *
 * Use [onIntChanged] to register a per-item value-change handler (overrides the global default).
 * Use [selectedValue] to register a per-item state reader (overrides the global default).
 *
 * @param key The developer-supplied key for this item.
 */
@SettingsDslMarker
class RadioGroupItemBuilder internal constructor(private val key: Any) {
    internal var visibleLambda: () -> Boolean = { true }
    internal var enabledValue: Boolean = true
    internal var options: List<RadioButtonOption> = emptyList()
    internal var onIntChangedOverride: ((Any, Int) -> Unit)? = null
    internal var selectedValueOverride: ((Any) -> Int)? = null

    /**
     * Controls whether this item is visible and indexed by search.
     *
     * The lambda is evaluated in pure Kotlin context. Read pre-computed values from the enclosing
     * composable scope instead of calling Compose APIs.
     */
    fun visible(block: () -> Boolean) {
        visibleLambda = block
    }

    /** Controls whether this item is interactable. */
    fun enabled(value: Boolean) {
        enabledValue = value
    }

    /** Sets the radio options using varargs. */
    fun options(vararg optionsList: RadioButtonOption) {
        options = optionsList.toList()
    }

    /** Sets the radio options from a list. */
    fun options(optionsList: List<RadioButtonOption>) {
        options = optionsList
    }

    /**
     * Registers a per-item value-change handler that overrides the global
     * [in.hridayan.settingsdsl.ui.OnClickDefaults.onIntChanged] for this item only.
     *
     * @param block Lambda receiving the item key and the newly selected index.
     */
    fun onIntChanged(block: (Any, Int) -> Unit) {
        onIntChangedOverride = block
    }

    /**
     * Registers a per-item integer state reader that overrides the global
     * [in.hridayan.settingsdsl.ui.OnClickDefaults.selectedValue] for this item only.
     *
     * @param block Lambda receiving the item key and returning the currently selected index.
     */
    fun selectedValue(block: (Any) -> Int) {
        selectedValueOverride = block
    }

    internal fun build(): SettingsNode = SettingsNode(
        key = key,
        keyName = key.toString(),
        isVisible = visibleLambda,
        dynamicTitle = null,
        staticTitleRes = null,
        staticTitleString = "",
        dynamicDescription = null,
        staticDescRes = null,
        staticDescString = "",
        iconResId = null,
        iconVector = null,
        experimentalFlagTextRes = null,
        experimentalFlagTextString = "",
        enabled = enabledValue,
        behavior = ItemBehavior.RadioGroup(options),
        radioOptions = options,
        onIntChangedOverride = onIntChangedOverride,
        selectedValueOverride = selectedValueOverride,
    )
}

/**
 * Builder for a segmented button group settings item.
 *
 * Use [onIntChanged] to register a per-item value-change handler (overrides the global default).
 * Use [selectedValue] to register a per-item state reader (overrides the global default).
 *
 * @param key The developer-supplied key for this item.
 */
@SettingsDslMarker
class ButtonGroupItemBuilder internal constructor(private val key: Any) {
    internal var visibleLambda: () -> Boolean = { true }
    internal var enabledValue: Boolean = true
    internal var options: List<ButtonGroupOption> = emptyList()
    internal var onIntChangedOverride: ((Any, Int) -> Unit)? = null
    internal var selectedValueOverride: ((Any) -> Int)? = null

    /**
     * Controls whether this item is visible and indexed by search.
     *
     * The lambda is evaluated in pure Kotlin context. Read pre-computed values from the enclosing
     * composable scope instead of calling Compose APIs.
     */
    fun visible(block: () -> Boolean) {
        visibleLambda = block
    }

    /** Controls whether this item is interactable. */
    fun enabled(value: Boolean) {
        enabledValue = value
    }

    /** Sets the button group options using varargs. */
    fun options(vararg optionsList: ButtonGroupOption) {
        options = optionsList.toList()
    }

    /** Sets the button group options from a list. */
    fun options(optionsList: List<ButtonGroupOption>) {
        options = optionsList
    }

    /**
     * Registers a per-item value-change handler that overrides the global
     * [in.hridayan.settingsdsl.ui.OnClickDefaults.onIntChanged] for this item only.
     *
     * @param block Lambda receiving the item key and the newly selected index.
     */
    fun onIntChanged(block: (Any, Int) -> Unit) {
        onIntChangedOverride = block
    }

    /**
     * Registers a per-item integer state reader that overrides the global
     * [in.hridayan.settingsdsl.ui.OnClickDefaults.selectedValue] for this item only.
     *
     * @param block Lambda receiving the item key and returning the currently selected index.
     */
    fun selectedValue(block: (Any) -> Int) {
        selectedValueOverride = block
    }

    internal fun build(): SettingsNode = SettingsNode(
        key = key,
        keyName = key.toString(),
        isVisible = visibleLambda,
        dynamicTitle = null,
        staticTitleRes = null,
        staticTitleString = "",
        dynamicDescription = null,
        staticDescRes = null,
        staticDescString = "",
        iconResId = null,
        iconVector = null,
        experimentalFlagTextRes = null,
        experimentalFlagTextString = "",
        enabled = enabledValue,
        behavior = ItemBehavior.ButtonGroup(options),
        buttonOptions = options,
        onIntChangedOverride = onIntChangedOverride,
        selectedValueOverride = selectedValueOverride,
    )
}

/**
 * Scope for defining items inside a settings group.
 */
@SettingsDslMarker
class GraphGroupScope internal constructor() {
    internal var titleResId: Int? = null
    internal var titleString: String = ""
    internal val nodes = mutableListOf<SettingsNode>()

    /** Sets the group title from a string resource. */
    fun title(@StringRes resId: Int) {
        titleResId = resId
        titleString = ""
    }

    /** Sets the group title from a plain string. */
    fun title(text: String) {
        titleString = text
        titleResId = null
    }

    /**
     * Adds a switch item to this group.
     *
     * @param key The key identifying this setting. Can be any type.
     * @param block Builder block to configure the item.
     */
    fun switchItem(key: Any, block: SwitchItemBuilder.() -> Unit) {
        nodes.add(SwitchItemBuilder(key).apply(block).build())
    }

    /**
     * Adds a full-width switch banner item to this group.
     *
     * @param key The key identifying this setting.
     * @param block Builder block to configure the item.
     */
    fun switchBannerItem(key: Any, block: SwitchBannerItemBuilder.() -> Unit) {
        nodes.add(SwitchBannerItemBuilder(key).apply(block).build())
    }

    /**
     * Adds a clickable item to this group.
     *
     * @param key The key identifying this setting.
     * @param block Builder block to configure the item.
     */
    fun clickableItem(key: Any, block: ClickableItemBuilder.() -> Unit) {
        nodes.add(ClickableItemBuilder(key).apply(block).build())
    }

    /**
     * Adds a radio group item to this group.
     *
     * @param key The key identifying this setting.
     * @param block Builder block to configure the item.
     */
    fun radioGroupItem(key: Any, block: RadioGroupItemBuilder.() -> Unit) {
        nodes.add(RadioGroupItemBuilder(key).apply(block).build())
    }

    /**
     * Adds a segmented button group item to this group.
     *
     * @param key The key identifying this setting.
     * @param block Builder block to configure the item.
     */
    fun buttonGroupItem(key: Any, block: ButtonGroupItemBuilder.() -> Unit) {
        nodes.add(ButtonGroupItemBuilder(key).apply(block).build())
    }
}

/**
 * Top-level builder for the contents of a [in.hridayan.settingsdsl.ui.SettingsColumn].
 *
 * This is the receiver of the `SettingsColumn { … }` block; instances are created by the renderer.
 */
@SettingsDslMarker
class SettingsGraphBuilder internal constructor() {
    internal val groups = mutableListOf<SettingsGraphGroup>()

    /**
     * Adds an anonymous group of items (no header label).
     *
     * @param block Builder block for configuring the items in this group.
     */
    fun group(block: GraphGroupScope.() -> Unit) {
        val scope = GraphGroupScope().apply(block)
        groups.add(
            SettingsGraphGroup.Group(
                titleResId = scope.titleResId,
                titleString = scope.titleString,
                nodes = scope.nodes,
            )
        )
    }

    /**
     * Adds a group with a string resource header label.
     *
     * @param titleResId String resource for the group header.
     * @param block Builder block for configuring the items in this group.
     */
    fun group(@StringRes titleResId: Int, block: GraphGroupScope.() -> Unit) {
        group {
            title(titleResId)
            block()
        }
    }

    /**
     * Adds a group with a plain string header label.
     *
     * @param title Plain string for the group header.
     * @param block Builder block for configuring the items in this group.
     */
    fun group(title: String, block: GraphGroupScope.() -> Unit) {
        group {
            title(title)
            block()
        }
    }

    /** Inserts a horizontal visual divider at this position in the graph. */
    fun divider() {
        groups.add(SettingsGraphGroup.Divider)
    }

    /**
     * Inserts arbitrary composable content at this position in the graph.
     *
     * The [content] lambda is a `@Composable` function rendered inside a lazy list item.
     * Use this for custom headers, banners, spacers, or any bespoke row placed between groups.
     *
     * @param key Stable unique key for the lazy list item. Must be unique within the graph and
     *            must not depend on the item's position, otherwise the lazy list discards and
     *            recreates the item whenever a preceding entry appears or disappears.
     * @param content The composable content to render.
     */
    fun item(key: Any, content: @Composable () -> Unit) {
        groups.add(SettingsGraphGroup.RawItem(key = key, content = content))
    }

    internal fun build(): SettingsGraph = SettingsGraph(groups = groups)
}

