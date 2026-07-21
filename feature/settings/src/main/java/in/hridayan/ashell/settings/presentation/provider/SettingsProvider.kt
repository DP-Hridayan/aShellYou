package `in`.hridayan.ashell.settings.presentation.provider

import `in`.hridayan.ashell.core.resources.R


import `in`.hridayan.ashell.core.common.SettingsKeys

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.UnfoldMoreDouble
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.presentation.components.floaters.FloatingIconsBackground
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.ui.provider.RadioGroupOptionsProvider
import `in`.hridayan.settingsdsl.dsl.buttonGroupItem
import `in`.hridayan.settingsdsl.dsl.category
import `in`.hridayan.settingsdsl.dsl.clickableItem
import `in`.hridayan.settingsdsl.dsl.customSlot
import `in`.hridayan.settingsdsl.dsl.group
import `in`.hridayan.settingsdsl.dsl.radioGroupItem
import `in`.hridayan.settingsdsl.dsl.settingsPage
import `in`.hridayan.settingsdsl.dsl.switchBannerItem
import `in`.hridayan.settingsdsl.dsl.switchItem
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.model.SettingsPage

private val isSdkLowerThan31 = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

object SettingsProvider {

    val settingsPage = settingsPage(
        screenTitle = R.string.settings,
        screenId = "settings",
        group(
            clickableItem(
                key = SettingsKeys.LookAndFeel,
                titleResId = R.string.look_and_feel,
                descriptionResId = R.string.des_look_and_feel,
                icon = R.drawable.ic_pallete
            ),
            clickableItem(
                key = SettingsKeys.Behavior,
                titleResId = R.string.behavior,
                descriptionResId = R.string.des_behavior,
                icon = R.drawable.ic_sentiment_neutral
            ),
            clickableItem(
                key = SettingsKeys.QuickSettingsTiles,
                titleResId = R.string.qs_tiles,
                descriptionResId = R.string.des_qs_tiles,
                icon = R.drawable.ic_dashboard
            ),
            clickableItem(
                key = SettingsKeys.AiModelManager,
                titleResId = R.string.ai_models,
                descriptionResId = R.string.des_ai_models,
                iconVector = Icons.Rounded.AutoAwesome
            ),
            clickableItem(
                key = SettingsKeys.AutoUpdate,
                titleResId = R.string.auto_update,
                descriptionResId = R.string.des_auto_update,
                icon = R.drawable.ic_auto_update
            ),
            clickableItem(
                key = SettingsKeys.BackupAndRestore,
                titleResId = R.string.backup_and_restore,
                descriptionResId = R.string.des_backup_and_restore,
                icon = R.drawable.ic_settings_backup_restore
            ),
            clickableItem(
                key = SettingsKeys.About,
                titleResId = R.string.about,
                descriptionResId = R.string.des_about,
                icon = R.drawable.ic_info
            ),
        )
    )

    val lookAndFeelPage = settingsPage(
        screenTitle = R.string.look_and_feel,
        screenId = "look_and_feel",
        group(
            switchItem(
                key = SettingsKeys.DynamicColors,
                titleResId = R.string.dynamic_colors,
                descriptionResId = R.string.des_dynamic_colors,
                icon = R.drawable.ic_dynamic_color,
                visible = !isSdkLowerThan31
            ),
            clickableItem(
                key = SettingsKeys.PaletteStyle,
                titleResId = R.string.palette_style,
                descriptionResId = R.string.palette_tonal_spot,
                icon = R.drawable.ic_styles
            ),
            clickableItem(
                key = SettingsKeys.DarkTheme,
                titleResId = R.string.dark_theme,
                descriptionResId = R.string.system,
                iconVector = Icons.Outlined.DarkMode
            ),
        ),
        category(
            titleResId = R.string.font_family,
            clickableItem(
                key = SettingsKeys.FontFamily,
                titleResId = R.string.font_family,
                descriptionResId = R.string.des_font_family,
                iconVector = Icons.Rounded.TextFields
            )
        ),
        category(
            titleResId = R.string.ui_scale,
            switchItem(
                key = SettingsKeys.AutoScaleUi,
                titleResId = R.string.auto_scale_ui,
                descriptionResId = R.string.des_auto_scale_ui,
                icon = R.drawable.ic_transform,
                enableExperimentalFlag = true,
                experimentalFlagTextResId = R.string.experimental
            ),
            clickableItem(
                key = SettingsKeys.CustomUiScale,
                titleResId = R.string.custom_ui_scale,
                descriptionResId = R.string.des_ui_scale,
                icon = R.drawable.ic_high_density
            )
        ),
        category(
            titleResId = R.string.additional_settings,
            switchItem(
                key = SettingsKeys.HapticsAndVibration,
                titleResId = R.string.haptics_and_vibration,
                descriptionResId = R.string.des_haptics_and_vibration,
                icon = R.drawable.ic_vibration
            ),
            clickableItem(
                key = SettingsKeys.Language,
                titleResId = R.string.default_language,
                descriptionResId = R.string.des_default_language,
                icon = R.drawable.ic_language
            ),
        ),
    )

    val darkThemePage = settingsPage(
        screenTitle = R.string.dark_theme,
        screenId = "dark_theme",
        category(
            titleResId = R.string.preference,
            radioGroupItem(
                key = SettingsKeys.ThemeMode,
                options = RadioGroupOptionsProvider.darkModeOptions
            )
        ),
        category(
            titleResId = R.string.battery_saver,
            switchItem(
                key = SettingsKeys.AutoDarkModeOnBatterySaver,
                titleResId = R.string.auto_dark_mode,
                descriptionResId = R.string.des_auto_dark_mode,
                icon = R.drawable.ic_night_sight_auto
            )
        ),
        category(
            titleResId = R.string.additional_settings,
            switchItem(
                key = SettingsKeys.HighContrastDarkMode,
                titleResId = R.string.high_contrast_dark_mode,
                descriptionResId = R.string.des_high_contrast_dark_mode,
                icon = R.drawable.ic_amoled_theme
            ),
        ),
    )

    val autoUpdatePage = settingsPage(
        screenTitle = R.string.auto_update,
        screenId = "auto_update",
        group(
            switchBannerItem(
                key = SettingsKeys.AutoUpdate,
                titleResId = R.string.enable_auto_update
            ),
        ),
        category(
            titleResId = R.string.update_channel,
            radioGroupItem(
                key = SettingsKeys.GithubReleaseType,
                options = RadioGroupOptionsProvider.updateChannelOptions
            ),
        ),
        category(
            titleResId = R.string.additional_settings,
            switchItem(
                key = SettingsKeys.EnableDirectDownload,
                titleResId = R.string.enable_direct_download,
                descriptionResId = R.string.des_enable_direct_download,
                iconVector = Icons.Rounded.Downloading
            ),
        ),
    )

    val behaviorPage = settingsPage(
        screenTitle = R.string.behavior,
        screenId = "behavior",
        category(
            titleResId = R.string.local_adb_shell,
            radioGroupItem(
                key = SettingsKeys.LocalAdbWorkingMode,
                options = RadioGroupOptionsProvider.localAdbShellModeOptions
            ),
        ),
        category(
            titleResId = R.string.terminal,
            switchItem(
                key = SettingsKeys.SmoothScrolling,
                titleResId = R.string.smooth_scrolling,
                descriptionResId = R.string.des_smooth_scroll,
                iconVector = Icons.Rounded.UnfoldMoreDouble
            ),
            switchItem(
                key = SettingsKeys.ClearOutputConfirmation,
                titleResId = R.string.clear_output_confirmation,
                descriptionResId = R.string.des_clear_output_confirmation,
                icon = R.drawable.ic_clear
            ),
            switchItem(
                key = SettingsKeys.OverrideMaximumBookmarksLimit,
                titleResId = R.string.override_bookmarks_limit,
                descriptionResId = R.string.des_override_bookmarks,
                icon = R.drawable.ic_bookmarks
            ),
            switchItem(
                key = SettingsKeys.DisableSoftKeyboard,
                titleResId = R.string.disable_softkey,
                descriptionResId = R.string.des_disable_softkey,
                icon = R.drawable.ic_disable_keyboard
            ),
        ),
        category(
            titleResId = R.string.terminal_font_style,
            buttonGroupItem(
                key = SettingsKeys.TerminalFontStyle,
                options = listOf(
                    ButtonGroupOption(TerminalFontStyle.MONOSPACE, R.string.monospace),
                    ButtonGroupOption(TerminalFontStyle.SYSTEM_FONT, R.string.system_font),
                )
            ),
        ),
        category(
            titleResId = R.string.file_actions,
            clickableItem(
                key = SettingsKeys.OutputSaveDirectory,
                titleResId = R.string.configure_save_directory,
                descriptionResId = R.string.des_configure_save_directory,
                icon = R.drawable.ic_directory
            ),
            switchItem(
                key = SettingsKeys.SaveWholeOutput,
                titleResId = R.string.save_whole_output,
                descriptionResId = R.string.des_save_whole_output,
                icon = R.drawable.ic_save_as
            ),
        ),
    )

    val aboutPage = settingsPage(
        screenTitle = R.string.about,
        screenId = "about",
        category(
            titleResId = R.string.contributors,
            clickableItem(
                key = SettingsKeys.Contributors,
                titleResId = R.string.contributors,
                descriptionResId = R.string.des_contributors,
                icon = R.drawable.ic_crowdsource
            ),
            clickableItem(
                key = SettingsKeys.Translators,
                titleResId = R.string.translators,
                descriptionResId = R.string.des_translators,
                icon = R.drawable.ic_translate
            ),
        ),
        category(
            titleResId = R.string.app,
            clickableItem(
                key = SettingsKeys.Changelogs,
                titleResId = R.string.changelogs,
                descriptionResId = R.string.des_changelogs,
                icon = R.drawable.ic_changelog
            ),
            clickableItem(
                key = SettingsKeys.Licenses,
                titleResId = R.string.licenses,
                descriptionResId = R.string.des_licenses,
                icon = R.drawable.ic_license
            ),
            clickableItem(
                key = SettingsKeys.CrashHistory,
                titleResId = R.string.crash_history,
                descriptionResId = R.string.des_crash_history,
                icon = R.drawable.ic_bug
            ),
            clickableItem(
                key = SettingsKeys.Report,
                titleResId = R.string.report_issue,
                descriptionResId = R.string.des_report_issue,
                icon = R.drawable.ic_report
            ),
            clickableItem(
                key = SettingsKeys.FeatureRequest,
                titleResId = R.string.feature_request,
                descriptionResId = R.string.des_feature_request,
                icon = R.drawable.ic_add_comment
            ),
        ),
    )

    val backupPage = settingsPage(
        screenTitle = R.string.backup_and_restore,
        screenId = "backup_restore",
        customSlot(BackupScreenCustomSlots.GoogleSignIn),
        category(
            titleResId = R.string.backup,
            clickableItem(
                key = SettingsKeys.BackupAppSettings,
                titleResId = R.string.backup_settings,
                descriptionResId = R.string.des_backup_settings,
                icon = R.drawable.ic_handyman
            ),
            clickableItem(
                key = SettingsKeys.BackupAppDatabase,
                titleResId = R.string.backup_app_database,
                descriptionResId = R.string.des_backup_app_database,
                icon = R.drawable.ic_database
            ),
            clickableItem(
                key = SettingsKeys.BackupAppData,
                titleResId = R.string.backup_all_data,
                descriptionResId = R.string.des_backup_all_data,
                icon = R.drawable.ic_upload_file
            ),
        ),
        customSlot(BackupScreenCustomSlots.LastBackupTime),
        category(
            titleResId = R.string.auto_backup,
            clickableItem(
                key = SettingsKeys.BackupScheduler,
                titleResId = R.string.backup_scheduler,
                descriptionResId = R.string.des_backup_scheduler,
                icon = R.drawable.ic_schedule
            ),
        ),
        category(
            titleResId = R.string.restore,
            clickableItem(
                key = SettingsKeys.RestoreAppData,
                titleResId = R.string.restore_app_data,
                descriptionResId = R.string.des_restore_app_data,
                icon = R.drawable.ic_restore_page
            ),
        ),
        category(
            titleResId = R.string.reset,
            clickableItem(
                key = SettingsKeys.ResetAppSettings,
                titleResId = R.string.reset_app_settings,
                descriptionResId = R.string.des_reset_app_settings,
                icon = R.drawable.ic_reset_settings
            ),
        ),
    )

    val backupSchedulerPage = settingsPage(
        screenTitle = R.string.backup_scheduler,
        screenId = "backup_scheduler",
        group(
            switchBannerItem(
                key = SettingsKeys.AutoBackupEnabled,
                titleResId = R.string.enable_auto_backup
            ),
        ),
        customSlot(BackupScreenCustomSlots.SchedulerStatus),
        category(
            titleResId = R.string.schedule,
            clickableItem(
                key = SettingsKeys.AutoBackupTime,
                titleResId = R.string.backup_time,
                descriptionResId = R.string.des_auto_backup_time,
                icon = R.drawable.ic_schedule
            )
        ),
        category(
            titleResId = R.string.frequency,
            radioGroupItem(
                key = SettingsKeys.AutoBackupFrequency,
                options = RadioGroupOptionsProvider.backupFrequencyOptions,
            ),
        ),
        category(
            titleResId = R.string.auto_backup_content_type,
            radioGroupItem(
                key = SettingsKeys.AutoBackupType,
                options = RadioGroupOptionsProvider.autoBackupTypeOptions,
            ),
        ),
        category(
            titleResId = R.string.local_backup,
            clickableItem(
                key = SettingsKeys.AutoBackupFolder,
                titleResId = R.string.auto_backup_folder,
                descriptionResId = R.string.des_auto_backup_folder,
                icon = R.drawable.ic_directory
            ),
            switchItem(
                key = SettingsKeys.AutoBackupDeleteExisting,
                titleResId = R.string.auto_delete_existing_backups,
                descriptionResId = R.string.des_auto_delete_existing_backups,
                icon = R.drawable.ic_delete_sweep
            ),
        )
    )

    val aiModelsPage = settingsPage(
        screenTitle = R.string.ai_models,
        screenId = "ai_models",
        category(
            titleResId = R.string.models,
            clickableItem(
                key = SettingsKeys.AiModels,
                titleResId = R.string.models,
                descriptionResId = R.string.des_models,
                iconVector = Icons.Rounded.Memory
            ),
        ),
        category(
            titleResId = R.string.cache_settings,
            switchItem(
                key = SettingsKeys.AiCacheEnabled,
                titleResId = R.string.ai_cache_enabled,
                descriptionResId = R.string.des_ai_cache_enabled,
                iconVector = Icons.Rounded.Cached
            ),
            clickableItem(
                key = SettingsKeys.AiCacheDays,
                titleResId = R.string.ai_cache_days,
                descriptionResId = R.string.des_ai_cache_days,
                icon = R.drawable.ic_schedule
            ),
            clickableItem(
                key = SettingsKeys.AiCacheClear,
                titleResId = R.string.clear_analysis_cache,
                descriptionResId = R.string.cache_size,
                icon = R.drawable.ic_delete_sweep
            )
        ),
    )

    /** All searchable pages — single source of truth for the search engine. */
    val allSearchablePages: List<SettingsPage> = listOf(
        settingsPage, lookAndFeelPage, darkThemePage,
        behaviorPage, autoUpdatePage, aboutPage, backupPage,
        aiModelsPage, backupSchedulerPage,
    )

    /**
     * Maps each page's [SettingsPage.screenId] to its [NavRoutes] factory.
     * Defined here — next to the pages — so adding a new page automatically
     * wires up search navigation. No hardcoded strings elsewhere.
     */
    private val navRouteMapping: Map<String, (String?) -> NavRoutes> = mapOf(
        settingsPage.screenId!! to { NavRoutes.SettingsScreen(it) },
        lookAndFeelPage.screenId!! to { NavRoutes.LookAndFeelScreen(it) },
        darkThemePage.screenId!! to { NavRoutes.DarkThemeScreen(it) },
        behaviorPage.screenId!! to { NavRoutes.BehaviorScreen(it) },
        autoUpdatePage.screenId!! to { NavRoutes.AutoUpdateScreen(it) },
        aboutPage.screenId!! to { NavRoutes.AboutScreen(it) },
        backupPage.screenId!! to { NavRoutes.BackupAndRestoreScreen(it) },
        aiModelsPage.screenId!! to { NavRoutes.AiModelManagerScreen(it) },
        backupSchedulerPage.screenId!! to { NavRoutes.BackupSchedulerScreen },
    )

    /** Resolves a [screenId] to the correct [NavRoutes] destination. */
    fun resolveNavRoute(screenId: String, highlightKey: String? = null): NavRoutes =
        navRouteMapping[screenId]?.invoke(highlightKey) ?: NavRoutes.SettingsScreen()
}

/**
 * Returns all drawable resource IDs used across every settings screen.
 * Used by [FloatingIconsBackground] on the main settings screen.
 */
fun getAllSettingsIcons(): List<Int> = listOf(
    R.drawable.ic_pallete,
    R.drawable.ic_sentiment_neutral,
    R.drawable.ic_dashboard,
    R.drawable.ic_auto_update,
    R.drawable.ic_settings_backup_restore,
    R.drawable.ic_info,
    R.drawable.ic_dynamic_color,
    R.drawable.ic_vibration,
    R.drawable.ic_language,
    R.drawable.ic_amoled_theme,
    R.drawable.ic_clear,
    R.drawable.ic_bookmarks,
    R.drawable.ic_disable_keyboard,
    R.drawable.ic_directory,
    R.drawable.ic_save_as,
    R.drawable.ic_crowdsource,
    R.drawable.ic_translate,
    R.drawable.ic_changelog,
    R.drawable.ic_license,
    R.drawable.ic_bug,
    R.drawable.ic_report,
    R.drawable.ic_add_comment,
    R.drawable.ic_handyman,
    R.drawable.ic_database,
    R.drawable.ic_upload_file,
    R.drawable.ic_restore_page,
    R.drawable.ic_reset_settings,
    R.drawable.ic_styles,
    R.drawable.ic_format_size
)
