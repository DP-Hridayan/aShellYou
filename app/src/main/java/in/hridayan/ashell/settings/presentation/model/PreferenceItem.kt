package `in`.hridayan.ashell.settings.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.ashell.settings.data.local.SettingsKeys

sealed class PreferenceItem(
    open val key: SettingsKeys,
    open val isLayoutVisible: Boolean = true,
    open val titleString: String = "",
    @param:StringRes open val titleResId: Int? = null,
    open val descriptionString: String = "",
    @param:StringRes open val descriptionResId: Int? = null,
    @param:DrawableRes open val iconResId: Int? = null,
    open val iconVector: ImageVector? = null,
    open val type: SettingsType = SettingsType.None,
) {
    data class IntPreferenceItem(
        val radioOptions: List<RadioButtonOptions>,
        val buttonGroupOptions: List<ButtonGroupOptions>,
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @param:StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @param:StringRes override val descriptionResId: Int?,
        @param:DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)

    data class BoolPreferenceItem(
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @param:StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @param:StringRes override val descriptionResId: Int?,
        @param:DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)

    data class StringPreferenceItem(
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @param:StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @param:StringRes override val descriptionResId: Int?,
        @param:DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)

    data class FloatPreferenceItem(
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @param:StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @param:StringRes override val descriptionResId: Int?,
        @param:DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)

    data class NullPreferenceItem(
        override val key: SettingsKeys,
        override val isLayoutVisible: Boolean,
        override val titleString: String,
        @param:StringRes override val titleResId: Int?,
        override val descriptionString: String,
        @param:StringRes override val descriptionResId: Int?,
        @param:DrawableRes override val iconResId: Int?,
        override val iconVector: ImageVector?,
        override val type: SettingsType
    ) : PreferenceItem(key)
}