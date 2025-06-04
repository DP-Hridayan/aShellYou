package `in`.hridayan.ashell.settings.data.local

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.rounded.ChangeHistory
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.Vibration
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.utils.MiUiCheck
import `in`.hridayan.ashell.settings.data.local.model.PreferenceGroup
import `in`.hridayan.ashell.settings.domain.model.SettingsType
import `in`.hridayan.ashell.settings.domain.model.boolPreferenceItem
import `in`.hridayan.ashell.settings.domain.model.categorizedItems
import `in`.hridayan.ashell.settings.domain.model.customComposable
import `in`.hridayan.ashell.settings.domain.model.horizontalDivider
import `in`.hridayan.ashell.settings.domain.model.intPreferenceItem
import `in`.hridayan.ashell.settings.domain.model.nullPreferenceItem
import `in`.hridayan.ashell.settings.domain.model.uncategorizedItems

val isMiUi = MiUiCheck.isMiui
val isSdkLowerThan13 = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
val isSdkLowerThan12 = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

object SettingsProvider {
    val settingsPageList: List<PreferenceGroup> = listOf(
        uncategorizedItems(
            nullPreferenceItem(
                key = SettingsKeys.LOOK_AND_FEEL,
                titleResId = R.string.look_and_feel,
                descriptionResId = R.string.des_look_and_feel,
                iconVector = Icons.Outlined.Palette
            ),
            nullPreferenceItem(
                key = SettingsKeys.BEHAVIOR,
                titleResId = R.string.behavior,
                descriptionResId = R.string.des_behavior,
                iconVector = Icons.Rounded.SentimentNeutral
            ),
            nullPreferenceItem(
                key = SettingsKeys.AUTO_UPDATE,
                titleResId = R.string.auto_update,
                descriptionResId = R.string.des_auto_update,
                iconVector = Icons.Rounded.Update
            ),
            nullPreferenceItem(
                key = SettingsKeys.BACKUP_AND_RESTORE,
                titleResId = R.string.backup_and_restore,
                descriptionResId = R.string.des_backup_and_restore,
                iconVector = Icons.Rounded.SettingsBackupRestore
            ),
            nullPreferenceItem(
                key = SettingsKeys.ABOUT,
                titleResId = R.string.about,
                descriptionResId = R.string.des_about,
                iconResId = R.drawable.ic_info
            )
        )
    )

    val darkThemePageList: List<PreferenceGroup> = listOf(
        uncategorizedItems(
            intPreferenceItem(
                key = SettingsKeys.THEME_MODE,
                type = SettingsType.RadioGroup,
                radioOptions = RadioGroupOptionsProvider.darkModeOptions
            )
        ),
        categorizedItems(
            categoryNameResId = R.string.additional_settings,
            boolPreferenceItem(
                key = SettingsKeys.HIGH_CONTRAST_DARK_MODE,
                titleResId = R.string.high_contrast_dark_mode,
                descriptionResId = R.string.des_high_contrast_dark_mode,
                iconVector = Icons.Rounded.Contrast,
            )
        )
    )

    val lookAndFeelPageList: List<PreferenceGroup> = listOf(
        uncategorizedItems(
            boolPreferenceItem(
                key = SettingsKeys.DYNAMIC_COLORS,
                titleResId = R.string.dynamic_colors,
                descriptionResId = R.string.des_dynamic_colors,
                iconVector = Icons.Rounded.Colorize,
                isLayoutVisible = !isSdkLowerThan12
            )
        ),
        categorizedItems(
            categoryNameResId = R.string.additional_settings,
            nullPreferenceItem(
                key = SettingsKeys.DARK_THEME,
                titleResId = R.string.dark_theme,
                descriptionResId = R.string.system,
                iconVector = Icons.Outlined.DarkMode,
            ),
            boolPreferenceItem(
                key = SettingsKeys.HAPTICS_AND_VIBRATION,
                titleResId = R.string.haptics_and_vibration,
                descriptionResId = R.string.des_haptics_and_vibration,
                iconVector = Icons.Rounded.Vibration,
            ),
            nullPreferenceItem(
                key = SettingsKeys.LANGUAGE,
                isLayoutVisible = !isMiUi && !isSdkLowerThan13,
                titleResId = R.string.default_language,
                descriptionResId = R.string.des_default_language,
                iconVector = Icons.Rounded.Language,
            )
        )
    )

    val autoUpdatePageList: List<PreferenceGroup> = listOf(
        uncategorizedItems(
            boolPreferenceItem(
                key = SettingsKeys.AUTO_UPDATE,
                titleResId = R.string.enable_auto_update,
                type = SettingsType.SwitchBanner
            )
        ),
        categorizedItems(
            categoryNameResId = R.string.update_channel,
            intPreferenceItem(
                key = SettingsKeys.GITHUB_RELEASE_TYPE,
                type = SettingsType.RadioGroup,
                radioOptions = RadioGroupOptionsProvider.updateChannelOptions
            )
        ),
        customComposable(label = "check_update_button"),

        horizontalDivider(),

        uncategorizedItems(
            boolPreferenceItem(
                key = SettingsKeys.ENABLE_DIRECT_DOWNLOAD,
                titleResId = R.string.enable_direct_download,
                descriptionResId = R.string.des_enable_direct_download,
                iconVector = Icons.Rounded.Downloading
            )
        ),

        horizontalDivider()
    )

    val aboutPageList: List<PreferenceGroup> = listOf(
        categorizedItems(
            categoryNameResId = R.string.app,
            nullPreferenceItem(
                key = SettingsKeys.VERSION,
                titleResId = R.string.version,
                descriptionString = BuildConfig.VERSION_NAME,
                iconResId = R.drawable.ic_version,
            ),
            nullPreferenceItem(
                key = SettingsKeys.CHANGELOGS,
                titleResId = R.string.changelogs,
                descriptionResId = R.string.des_changelogs,
                iconVector = Icons.Rounded.ChangeHistory,
            ),
            nullPreferenceItem(
                key = SettingsKeys.REPORT,
                titleResId = R.string.report_issue,
                descriptionResId = R.string.des_report_issue,
                iconResId = R.drawable.ic_report
            ),
            nullPreferenceItem(
                key = SettingsKeys.FEATURE_REQUEST,
                titleResId = R.string.feature_request,
                descriptionResId = R.string.des_feature_request,
                iconResId = R.drawable.ic_add_comment
            ),
            nullPreferenceItem(
                key = SettingsKeys.GITHUB,
                titleResId = R.string.github,
                descriptionResId = R.string.des_github,
                iconResId = R.drawable.ic_github,
            ),
            nullPreferenceItem(
                key = SettingsKeys.LICENSE,
                titleResId = R.string.license,
                descriptionResId = R.string.des_license,
                iconResId = R.drawable.ic_license,
            )
        )
    )

    val behaviorPageList: List<PreferenceGroup> = listOf(

    )

    val backupPageList: List<PreferenceGroup> = listOf(
        categorizedItems(
            categoryNameResId = R.string.backup,
            nullPreferenceItem(
                key = SettingsKeys.BACKUP_APP_SETTINGS,
                titleResId = R.string.backup_settings,
                descriptionResId = R.string.des_backup_settings,
                iconResId = R.drawable.ic_handyman
            ),
            nullPreferenceItem(
                key = SettingsKeys.BACKUP_APP_DATABASE,
                titleResId = R.string.backup_app_database,
                descriptionResId = R.string.des_backup_app_database,
                iconResId = R.drawable.ic_database
            ),
            nullPreferenceItem(
                key = SettingsKeys.BACKUP_APP_DATA,
                titleResId = R.string.backup_all_data,
                descriptionResId = R.string.des_backup_all_data,
                iconResId = R.drawable.ic_upload_file
            )
        ),

        customComposable("last_backup_time"),

        categorizedItems(
            categoryNameResId = R.string.restore,
            nullPreferenceItem(
                key = SettingsKeys.RESTORE_APP_DATA,
                titleResId = R.string.restore_app_data,
                descriptionResId = R.string.des_restore_app_data,
                iconResId = R.drawable.ic_restore_page
            )
        ),
        categorizedItems(
            categoryNameResId = R.string.reset,
            nullPreferenceItem(
                key = SettingsKeys.RESET_APP_SETTINGS,
                titleResId = R.string.reset_app_settings,
                descriptionResId = R.string.des_reset_app_settings,
                iconResId = R.drawable.ic_reset_settings
            )
        )
    )

}