package `in`.hridayan.settingsdsl.dsl

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.model.ItemSpec
import `in`.hridayan.settingsdsl.model.RadioButtonOption
import `in`.hridayan.settingsdsl.model.SettingsItemSpec
import `in`.hridayan.settingsdsl.model.SettingsKey

/**
 * DSL marker annotation to restrict scope in settings DSL blocks.
 */
@DslMarker
annotation class SettingsDslMarker

/**
 * Base builder for a settings item with a title and visibility state.
 */
@SettingsDslMarker
abstract class BaseSettingsItemBuilder {
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
abstract class BaseSettingsItemWithDescriptionBuilder : BaseSettingsItemBuilder() {
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
abstract class BaseSettingsItemWithIconBuilder : BaseSettingsItemWithDescriptionBuilder() {
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
abstract class BaseSettingsItemWithExperimentalFlagBuilder : BaseSettingsItemWithIconBuilder() {
    var enableExperimentalFlag: Boolean = false
    internal var experimentalFlagTextResId: Int? = null
    internal var experimentalFlagTextString: String = "Experimental"

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
    BaseSettingsItemWithExperimentalFlagBuilder() {
    internal fun build(): SettingsItemSpec = SettingsItemSpec(
        ItemSpec.ClickableSpec(
            key = key,
            isVisible = visible,
            enabled = enabled,
            titleResId = titleResId,
            titleString = titleString,
            descriptionResId = descriptionResId,
            descriptionString = descriptionString,
            iconResId = iconResId,
            iconVector = iconVector,
            enableExperimentalFlag = enableExperimentalFlag,
            experimentalFlagTextResId = experimentalFlagTextResId,
            experimentalFlagText = experimentalFlagTextString
        )
    )
}

/**
 * Builder for a switch settings item.
 */
@SettingsDslMarker
class SwitchItemBuilder internal constructor(private val key: SettingsKey<*>) :
    BaseSettingsItemWithExperimentalFlagBuilder() {
    internal fun build(): SettingsItemSpec = SettingsItemSpec(
        ItemSpec.SwitchSpec(
            key = key,
            isVisible = visible,
            enabled = enabled,
            titleResId = titleResId,
            titleString = titleString,
            descriptionResId = descriptionResId,
            descriptionString = descriptionString,
            iconResId = iconResId,
            iconVector = iconVector,
            enableExperimentalFlag = enableExperimentalFlag,
            experimentalFlagTextResId = experimentalFlagTextResId,
            experimentalFlagText = experimentalFlagTextString
        )
    )
}

/**
 * Builder for a switch banner item.
 */
@SettingsDslMarker
class SwitchBannerItemBuilder internal constructor(private val key: SettingsKey<*>) :
    BaseSettingsItemBuilder() {
    internal fun build(): SettingsItemSpec = SettingsItemSpec(
        ItemSpec.SwitchBannerSpec(
            key = key,
            isVisible = visible,
            enabled = enabled,
            titleResId = titleResId,
            titleString = titleString
        )
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

    internal fun build(): SettingsItemSpec = SettingsItemSpec(
        ItemSpec.RadioGroupSpec(
            key = key,
            isVisible = visible,
            enabled = enabled,
            options = options
        )
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

    internal fun build(): SettingsItemSpec = SettingsItemSpec(
        ItemSpec.ButtonGroupSpec(
            key = key,
            isVisible = visible,
            enabled = enabled,
            options = options
        )
    )
}
