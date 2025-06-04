package `in`.hridayan.ashell.settings.domain.model

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.data.local.model.PreferenceGroup
import `in`.hridayan.ashell.settings.data.local.model.PreferenceItem
import `in`.hridayan.ashell.settings.data.local.model.RadioButtonOptions

fun categorizedItems(
    categoryNameResId: Int,
    vararg items: PreferenceItem
): PreferenceGroup.Category {
    return PreferenceGroup.Category(categoryNameResId, items.toList())
}

fun uncategorizedItems(vararg items: PreferenceItem): PreferenceGroup.Items {
    return PreferenceGroup.Items(items.toList())
}

fun customComposable(label: String = ""): PreferenceGroup.CustomComposable {
    return PreferenceGroup.CustomComposable(label)
}

fun horizontalDivider() = PreferenceGroup.HorizontalDivider

fun intPreferenceItem(
    key: SettingsKeys,
    radioOptions: List<RadioButtonOptions> = emptyList(),
    isLayoutVisible: Boolean = true,
    titleString: String = "",
    titleResId: Int? = null,
    descriptionString: String = "",
    descriptionResId: Int? = null,
    iconResId: Int? = null,
    iconVector: ImageVector? = null,
    type: SettingsType = SettingsType.None
) = PreferenceItem.IntPreferenceItem(
    key = key,
    isLayoutVisible = isLayoutVisible,
    radioOptions = radioOptions,
    titleString = titleString,
    titleResId = titleResId,
    descriptionString = descriptionString,
    descriptionResId = descriptionResId,
    iconResId = iconResId,
    iconVector = iconVector,
    type = type
)

fun boolPreferenceItem(
    key: SettingsKeys,
    isLayoutVisible: Boolean = true,
    titleString: String = "",
    titleResId: Int? = null,
    descriptionString: String = "",
    descriptionResId: Int? = null,
    iconResId: Int? = null,
    iconVector: ImageVector? = null,
    type: SettingsType = SettingsType.Switch
) = PreferenceItem.BoolPreferenceItem(
    key = key,
    isLayoutVisible = isLayoutVisible,
    titleString = titleString,
    titleResId = titleResId,
    descriptionString = descriptionString,
    descriptionResId = descriptionResId,
    iconResId = iconResId,
    iconVector = iconVector,
    type = type,
)

fun stringPreferenceItem(
    key: SettingsKeys,
    isLayoutVisible: Boolean = true,
    titleString: String = "",
    titleResId: Int? = null,
    descriptionString: String = "",
    descriptionResId: Int? = null,
    iconResId: Int? = null,
    iconVector: ImageVector? = null,
    type: SettingsType = SettingsType.None
) = PreferenceItem.StringPreferenceItem(
    key = key,
    isLayoutVisible = isLayoutVisible,
    titleString = titleString,
    titleResId = titleResId,
    descriptionString = descriptionString,
    descriptionResId = descriptionResId,
    iconResId = iconResId,
    iconVector = iconVector,
    type = type
)

fun floatPreferenceItem(
    key: SettingsKeys,
    isLayoutVisible: Boolean = true,
    titleString: String = "",
    titleResId: Int? = null,
    descriptionString: String = "",
    descriptionResId: Int? = null,
    iconResId: Int? = null,
    iconVector: ImageVector? = null,
    type: SettingsType = SettingsType.None
) = PreferenceItem.FloatPreferenceItem(
    key = key,
    isLayoutVisible = isLayoutVisible,
    titleString = titleString,
    titleResId = titleResId,
    descriptionString = descriptionString,
    descriptionResId = descriptionResId,
    iconResId = iconResId,
    iconVector = iconVector,
    type = type
)

fun nullPreferenceItem(
    key: SettingsKeys,
    isLayoutVisible: Boolean = true,
    titleString: String = "",
    titleResId: Int? = null,
    descriptionString: String = "",
    descriptionResId: Int? = null,
    iconResId: Int? = null,
    iconVector: ImageVector? = null,
    type: SettingsType = SettingsType.None
) = PreferenceItem.NullPreferenceItem(
    key = key,
    isLayoutVisible = isLayoutVisible,
    titleString = titleString,
    titleResId = titleResId,
    descriptionString = descriptionString,
    descriptionResId = descriptionResId,
    iconResId = iconResId,
    iconVector = iconVector,
    type = type
)


@Composable
fun PreferenceItem.getResolvedTitle(): String {
    return titleResId?.let {
        runCatching { stringResource(it) }.getOrElse { titleString.ifBlank { "" } }
    } ?: titleString.ifBlank { "" }
}


@Composable
fun PreferenceItem.getResolvedIcon(): ImageVector? {

    val darkMode = LocalDarkMode.current

    return if (key == SettingsKeys.DARK_THEME) {
        if (darkMode) Icons.Outlined.DarkMode
        else Icons.Rounded.LightMode
    } else {
        iconVector ?: iconResId?.let {
            runCatching { ImageVector.vectorResource(id = it) }.getOrNull()
        }
    }
}

@Composable
fun PreferenceItem.getResolvedDescription(): String {
    val themeMode = LocalSettings.current.themeMode

    return if (key == SettingsKeys.DARK_THEME) {
        when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> stringResource(R.string.on)
            AppCompatDelegate.MODE_NIGHT_NO -> stringResource(R.string.off)
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> stringResource(R.string.system)
            else -> ""
        }
    } else {
        descriptionResId?.let {
            runCatching { stringResource(it) }.getOrElse {
                descriptionString.takeIf { it.isNotBlank() } ?: ""
            }
        } ?: descriptionString.takeIf { it.isNotBlank() } ?: ""
    }
}


