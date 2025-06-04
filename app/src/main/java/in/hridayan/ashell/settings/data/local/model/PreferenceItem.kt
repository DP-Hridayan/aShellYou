package `in`.hridayan.ashell.settings.data.local.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.SettingsType

sealed class PreferenceItem(
    open val key: SettingsKeys,
    open val isLayoutVisible: Boolean = true,
    open val titleString: String = "",
    @StringRes open val titleResId: Int? = null,
    open val descriptionString: String = "",
    @StringRes open val descriptionResId: Int? = null,
    @DrawableRes open val iconResId: Int? = null,
    open val iconVector: ImageVector? = null,
    open val type: SettingsType = SettingsType.None,
) {
    data class IntPreferenceItem(
        val radioOptions: List<RadioButtonOptions>,
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @StringRes override val descriptionResId: Int?,
        @DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)

    data class BoolPreferenceItem(
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @StringRes override val descriptionResId: Int?,
        @DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)

    data class StringPreferenceItem(
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @StringRes override val descriptionResId: Int?,
        @DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)

    data class FloatPreferenceItem(
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @StringRes override val descriptionResId: Int?,
        @DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)

    data class NullPreferenceItem(
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @StringRes override val descriptionResId: Int?,
        @DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)
}