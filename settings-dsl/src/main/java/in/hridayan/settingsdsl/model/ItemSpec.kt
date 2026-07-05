package `in`.hridayan.settingsdsl.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Internal blueprint for a single settings item.
 * Created by DSL builder functions and stored in [SettingsGroup].
 * Never exposed to screen-level or view-level code directly.
 */
internal sealed class ItemSpec(
    val key: SettingsKey<*>,
    val isVisible: Boolean,
    val enabled: Boolean,
    @param:StringRes val titleResId: Int?,
    val titleString: String,
    @param:StringRes val descriptionResId: Int?,
    val descriptionString: String,
    @param:DrawableRes val iconResId: Int?,
    val iconVector: ImageVector?,
    val enableExperimentalFlag: Boolean,
    @param:StringRes val experimentalFlagTextResId: Int?,
    val experimentalFlagText: String
) {
    class SwitchSpec(
        key: SettingsKey<*>,
        isVisible: Boolean,
        enabled: Boolean,
        @StringRes titleResId: Int?,
        titleString: String,
        @StringRes descriptionResId: Int?,
        descriptionString: String,
        @DrawableRes iconResId: Int?,
        iconVector: ImageVector?,
        enableExperimentalFlag: Boolean,
        @StringRes experimentalFlagTextResId: Int?,
        experimentalFlagText: String
    ) : ItemSpec(
        key = key,
        isVisible = isVisible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = titleString,
        descriptionResId = descriptionResId,
        descriptionString = descriptionString,
        iconResId = iconResId,
        iconVector = iconVector,
        enableExperimentalFlag = enableExperimentalFlag,
        experimentalFlagTextResId = experimentalFlagTextResId,
        experimentalFlagText = experimentalFlagText
    )

    class SwitchBannerSpec(
        key: SettingsKey<*>,
        isVisible: Boolean,
        enabled: Boolean,
        @StringRes titleResId: Int?,
        titleString: String,
    ) : ItemSpec(
        key = key,
        isVisible = isVisible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = titleString,
        descriptionResId = null,
        descriptionString = "",
        iconResId = null,
        iconVector = null,
        enableExperimentalFlag = false,
        experimentalFlagTextResId = null,
        experimentalFlagText = ""
    )

    class ClickableSpec(
        key: SettingsKey<*>,
        isVisible: Boolean,
        enabled: Boolean,
        @StringRes titleResId: Int?,
        titleString: String,
        @StringRes descriptionResId: Int?,
        descriptionString: String,
        @DrawableRes iconResId: Int?,
        iconVector: ImageVector?,
        enableExperimentalFlag: Boolean,
        @StringRes experimentalFlagTextResId: Int?,
        experimentalFlagText: String
    ) : ItemSpec(
        key = key,
        isVisible = isVisible,
        enabled = enabled,
        titleResId = titleResId,
        titleString = titleString,
        descriptionResId = descriptionResId,
        descriptionString = descriptionString,
        iconResId = iconResId,
        iconVector = iconVector,
        enableExperimentalFlag = enableExperimentalFlag,
        experimentalFlagTextResId = experimentalFlagTextResId,
        experimentalFlagText = experimentalFlagText
    )

    class RadioGroupSpec(
        key: SettingsKey<*>,
        isVisible: Boolean,
        enabled: Boolean,
        val options: List<RadioButtonOption>,
    ) : ItemSpec(
        key = key,
        isVisible = isVisible,
        enabled = enabled,
        titleResId = null,
        titleString = "",
        descriptionResId = null,
        descriptionString = "",
        iconResId = null,
        iconVector = null,
        enableExperimentalFlag = false,
        experimentalFlagTextResId = null,
        experimentalFlagText = ""
    )

    class ButtonGroupSpec(
        key: SettingsKey<*>,
        isVisible: Boolean,
        enabled: Boolean,
        val options: List<ButtonGroupOption>,
    ) : ItemSpec(   key = key,
        isVisible = isVisible,
        enabled = enabled,
        titleResId = null,
        titleString = "",
        descriptionResId = null,
        descriptionString = "",
        iconResId = null,
        iconVector = null,
        enableExperimentalFlag = false,
        experimentalFlagTextResId = null,
        experimentalFlagText = "")
}

/**
 * Opaque wrapper around [ItemSpec] used as the parameter type for DSL group builders.
 *
 * The internal [spec] field is accessible only within the library module. Consumers
 * create instances via DSL builder functions ([switchItem], [clickableItem], etc.) and
 * pass them to group builders ([group], [category]). They cannot read or modify the spec directly.
 */
class SettingsItemSpec internal constructor(internal val spec: ItemSpec)

/**
 * A radio button option within a [radioGroupItem].
 *
 * @param value The integer value stored when this option is selected.
 * @param labelResId String resource for the display label.
 */
data class RadioButtonOption(
    val value: Int,
    @param:StringRes val labelResId: Int,
)

/**
 * A segmented button option within a [buttonGroupItem].
 *
 * @param value The integer value stored when this option is selected.
 * @param labelResId String resource for the display label.
 * @param iconResId Optional drawable for the button icon.
 */
data class ButtonGroupOption(
    val value: Int,
    @param:StringRes val labelResId: Int,
    @param:DrawableRes val iconResId: Int? = null,
)
