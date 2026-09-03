package `in`.hridayan.ashell.core.presentation.provider

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.UnfoldMoreDouble
import `in`.hridayan.ashell.core.common.FeatureConfig
import `in`.hridayan.ashell.core.common.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.floaters.FloatingIconsBackground
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.settingsdsl.dsl.settingsPage
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.model.SettingsPage

private val isSdkLowerThan31 = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

object SettingsProvider {

    val settingsPage = settingsPage("settings") {
        title(R.string.settings)

        group {
            clickableItem(SettingsKeys.LookAndFeel) {
                title(R.string.look_and_feel)
                description(R.string.des_look_and_feel)
                icon(R.drawable.ic_pallete)
            }

            clickableItem(SettingsKeys.Behavior) {
                title(R.string.behavior)
                description(R.string.des_behavior)
                icon(R.drawable.ic_sentiment_neutral)
            }

            clickableItem(SettingsKeys.QuickSettingsTiles) {
                title(R.string.qs_tiles)
                description(R.string.des_qs_tiles)
                icon(R.drawable.ic_dashboard)
            }

            clickableItem(SettingsKeys.CloudModels) {
                title(R.string.ai_models)
                description(R.string.des_ai_models)
                icon(Icons.Outlined.AutoAwesome)
                visible = FeatureConfig.isAiEnabled
            }

            clickableItem(SettingsKeys.AutoUpdate) {
                title(R.string.auto_update)
                description(R.string.des_auto_update)
                icon(R.drawable.ic_auto_update)
            }

            clickableItem(SettingsKeys.BackupAndRestore) {
                title(R.string.backup_and_restore)
                description(R.string.des_backup_and_restore)
                icon(R.drawable.ic_settings_backup_restore)
            }

            clickableItem(SettingsKeys.About) {
                title(R.string.about)
                description(R.string.des_about)
                icon(R.drawable.ic_info)
            }
        }
    }

    val lookAndFeelPage = settingsPage("look_and_feel") {
        title(R.string.look_and_feel)

        group {
            switchItem(SettingsKeys.DynamicColors) {
                title(R.string.dynamic_colors)
                description(R.string.des_dynamic_colors)
                icon(R.drawable.ic_dynamic_color)
                visible = !isSdkLowerThan31
            }

            clickableItem(SettingsKeys.PaletteStyle) {
                title(R.string.palette_style)
                description(R.string.palette_tonal_spot)
                icon(R.drawable.ic_styles)
            }

            clickableItem(SettingsKeys.DarkTheme) {
                title(R.string.dark_theme)
                description(R.string.system)
                icon(Icons.Outlined.DarkMode)
            }
        }

        group(R.string.font_family) {
            clickableItem(SettingsKeys.FontFamily) {
                title(R.string.font_family)
                description(R.string.des_font_family)
                icon(Icons.Rounded.TextFields)
            }
        }

        group(R.string.ui_scale) {
            switchItem(SettingsKeys.AutoScaleUi) {
                title(R.string.auto_scale_ui)
                description(R.string.des_auto_scale_ui)
                icon(R.drawable.ic_transform)
                experimentalFlagText(R.string.experimental)
            }

            clickableItem(SettingsKeys.CustomUiScale) {
                title(R.string.custom_ui_scale)
                description(R.string.des_ui_scale)
                icon(R.drawable.ic_high_density)
            }
        }

        group(R.string.additional_settings) {
            switchItem(SettingsKeys.HapticsAndVibration) {
                title(R.string.haptics_and_vibration)
                description(R.string.des_haptics_and_vibration)
                icon(R.drawable.ic_vibration)
            }

            clickableItem(SettingsKeys.Language) {
                title(R.string.default_language)
                description(R.string.des_default_language)
                icon(R.drawable.ic_language)
            }
        }
    }

    val darkThemePage = settingsPage("dark_theme") {
        title(R.string.dark_theme)

        group(R.string.preference) {
            radioGroupItem(SettingsKeys.ThemeMode) {
                options(RadioGroupOptionsProvider.darkModeOptions)
            }
        }

        group(R.string.battery_saver) {
            switchItem(SettingsKeys.AutoDarkModeOnBatterySaver) {
                title(R.string.auto_dark_mode)
                description(R.string.des_auto_dark_mode)
                icon(R.drawable.ic_night_sight_auto)
            }
        }

        group(R.string.additional_settings) {
            switchItem(SettingsKeys.HighContrastDarkMode) {
                title(R.string.high_contrast_dark_mode)
                description(R.string.des_high_contrast_dark_mode)
                icon(R.drawable.ic_amoled_theme)
            }
        }
    }

    val autoUpdatePage = settingsPage("auto_update") {
        title(R.string.auto_update)

        group {
            switchBannerItem(SettingsKeys.AutoUpdate) {
                title(R.string.enable_auto_update)
            }
        }

        group(R.string.update_channel) {
            radioGroupItem(SettingsKeys.GithubReleaseType) {
                options(RadioGroupOptionsProvider.updateChannelOptions)
            }
        }

        group(R.string.additional_settings) {
            switchItem(SettingsKeys.EnableDirectDownload) {
                title(R.string.enable_direct_download)
                description(R.string.des_enable_direct_download)
                icon(Icons.Rounded.Downloading)
            }
        }
    }

    val behaviorPage = settingsPage("behavior") {
        title(R.string.behavior)

        group(R.string.local_adb_shell) {
            radioGroupItem(SettingsKeys.LocalAdbWorkingMode) {
                options(RadioGroupOptionsProvider.localAdbShellModeOptions)
            }
        }

        group(R.string.launch) {
            switchItem(SettingsKeys.DefaultLaunchIsLocalAdb) {
                title(R.string.set_local_adb_as_default_launch)
                description(R.string.des_set_local_adb_as_default_launch)
                icon(R.drawable.ic_rocket_launch)
            }
        }

        group(R.string.terminal) {
            switchItem(SettingsKeys.SmoothScrolling) {
                title(R.string.smooth_scrolling)
                description(R.string.des_smooth_scroll)
                icon(Icons.Rounded.UnfoldMoreDouble)
            }

            switchItem(SettingsKeys.ClearOutputConfirmation) {
                title(R.string.clear_output_confirmation)
                description(R.string.des_clear_output_confirmation)
                icon(R.drawable.ic_clear)
            }

            switchItem(SettingsKeys.OverrideMaximumBookmarksLimit) {
                title(R.string.override_bookmarks_limit)
                description(R.string.des_override_bookmarks)
                icon(R.drawable.ic_bookmarks)
            }

            switchItem(SettingsKeys.DisableSoftKeyboard) {
                title(R.string.disable_softkey)
                description(R.string.des_disable_softkey)
                icon(R.drawable.ic_disable_keyboard)
            }
        }

        group(R.string.terminal_font_style) {
            buttonGroupItem(SettingsKeys.TerminalFontStyle) {
                options(
                    ButtonGroupOption(TerminalFontStyle.MONOSPACE, R.string.monospace),
                    ButtonGroupOption(TerminalFontStyle.SYSTEM_FONT, R.string.system_font),
                )
            }
        }

        group(R.string.file_actions) {
            clickableItem(SettingsKeys.OutputSaveDirectory) {
                title(R.string.configure_save_directory)
                description(R.string.des_configure_save_directory)
                icon(R.drawable.ic_directory)
            }

            switchItem(SettingsKeys.SaveWholeOutput) {
                title(R.string.save_whole_output)
                description(R.string.des_save_whole_output)
                icon(R.drawable.ic_save_as)
            }
        }
    }

    val aboutPage = settingsPage("about") {
        title(R.string.about)

        group(R.string.contributors) {
            clickableItem(SettingsKeys.Contributors) {
                title(R.string.contributors)
                description(R.string.des_contributors)
                icon(R.drawable.ic_crowdsource)
            }

            clickableItem(SettingsKeys.Translators) {
                title(R.string.translators)
                description(R.string.des_translators)
                icon(R.drawable.ic_translate)
            }
        }

        group(R.string.app) {
            clickableItem(SettingsKeys.Changelogs) {
                title(R.string.changelogs)
                description(R.string.des_changelogs)
                icon(R.drawable.ic_changelog)
            }

            clickableItem(SettingsKeys.Report) {
                title(R.string.report_issue)
                description(R.string.des_report_issue)
                icon(R.drawable.ic_report)
            }

            clickableItem(SettingsKeys.FeatureRequest) {
                title(R.string.feature_request)
                description(R.string.des_feature_request)
                icon(R.drawable.ic_add_comment)
            }

            clickableItem(SettingsKeys.CrashHistory) {
                title(R.string.crash_history)
                description(R.string.des_crash_history)
                icon(R.drawable.ic_bug)
            }

            clickableItem(SettingsKeys.Licenses) {
                title(R.string.libraries_and_licenses)
                description(R.string.des_libraries_and_licenses)
                icon(R.drawable.ic_license)
            }

            clickableItem(SettingsKeys.PrivacyPolicy) {
                title(R.string.privacy_policy)
                description(R.string.des_privacy_policy)
                icon(R.drawable.ic_privacy_tip)
            }
        }
    }

    val backupPage = settingsPage("backup_restore") {
        title(R.string.backup_and_restore)

        customSlot(BackupScreenCustomSlots.GoogleSignIn)

        group(R.string.backup) {
            clickableItem(SettingsKeys.BackupAppSettings) {
                title(R.string.backup_settings)
                description(R.string.des_backup_settings)
                icon(R.drawable.ic_handyman)
            }

            clickableItem(SettingsKeys.BackupAppDatabase) {
                title(R.string.backup_app_database)
                description(R.string.des_backup_app_database)
                icon(R.drawable.ic_database)
            }

            clickableItem(SettingsKeys.BackupAppData) {
                title(R.string.backup_all_data)
                description(R.string.des_backup_all_data)
                icon(R.drawable.ic_upload_file)
            }
        }

        customSlot(BackupScreenCustomSlots.LastBackupTime)

        group(R.string.auto_backup) {
            clickableItem(SettingsKeys.BackupScheduler) {
                title(R.string.backup_scheduler)
                description(R.string.des_backup_scheduler)
                icon(R.drawable.ic_schedule)
            }
        }

        group(R.string.restore) {
            clickableItem(SettingsKeys.RestoreAppData) {
                title(R.string.restore_app_data)
                description(R.string.des_restore_app_data)
                icon(R.drawable.ic_restore_page)
            }
        }

        group(R.string.reset) {
            clickableItem(SettingsKeys.ResetAppSettings) {
                title(R.string.reset_app_settings)
                description(R.string.des_reset_app_settings)
                icon(R.drawable.ic_reset_settings)
            }
        }
    }

    val backupSchedulerPage = settingsPage("backup_scheduler") {
        title(R.string.backup_scheduler)

        group {
            switchBannerItem(SettingsKeys.AutoBackupEnabled) {
                title(R.string.enable_auto_backup)
            }
        }

        customSlot(BackupScreenCustomSlots.SchedulerStatus)

        group(R.string.schedule) {
            clickableItem(SettingsKeys.AutoBackupTime) {
                title(R.string.backup_time)
                description(R.string.des_auto_backup_time)
                icon(R.drawable.ic_schedule)
            }
        }

        group(R.string.frequency) {
            radioGroupItem(SettingsKeys.AutoBackupFrequency) {
                options(RadioGroupOptionsProvider.backupFrequencyOptions)
            }
        }

        group(R.string.auto_backup_content_type) {
            radioGroupItem(SettingsKeys.AutoBackupType) {
                options(RadioGroupOptionsProvider.autoBackupTypeOptions)
            }
        }

        group(R.string.local_backup) {
            clickableItem(SettingsKeys.AutoBackupFolder) {
                title(R.string.auto_backup_folder)
                description(R.string.des_auto_backup_folder)
                icon(R.drawable.ic_directory)
            }

            switchItem(SettingsKeys.AutoBackupDeleteExisting) {
                title(R.string.auto_delete_existing_backups)
                description(R.string.des_auto_delete_existing_backups)
                icon(R.drawable.ic_delete_sweep)
            }
        }
    }

    val aiSettingsPage = settingsPage("ai_settings") {
        title(R.string.ai_models)

        group(R.string.models) {
            clickableItem(SettingsKeys.AiCloudProvider) {
                title(R.string.cloud_models)
                description(R.string.des_cloud_models)
                icon(R.drawable.ic_cloud_model)
            }
        }

        group(R.string.agent_skills) {
            switchItem(SettingsKeys.AiSkillCommandExecution) {
                title(R.string.command_execution)
                description(R.string.des_command_execution)
                icon(R.drawable.ic_terminal)
            }

            switchItem(SettingsKeys.AiSkillQuickSettings) {
                title(R.string.quick_settings_tiles)
                description(R.string.des_quick_settings_tiles)
                icon(R.drawable.ic_dashboard)
            }

            switchItem(SettingsKeys.AiSkillPackages) {
                title(R.string.packages)
                description(R.string.des_packages)
                icon(R.drawable.ic_package)
            }

            switchItem(SettingsKeys.AiSkillDatabase) {
                title(R.string.database_modification)
                description(R.string.des_database_modification)
                icon(R.drawable.ic_database)
            }
        }

        group(R.string.cache_settings) {
            switchItem(SettingsKeys.AiCacheEnabled) {
                title(R.string.ai_cache_enabled)
                description(R.string.des_ai_cache_enabled)
                icon(Icons.Rounded.Cached)
            }

            clickableItem(SettingsKeys.AiCacheDays) {
                title(R.string.ai_cache_days)
                description(R.string.des_ai_cache_days)
                icon(R.drawable.ic_schedule)
            }

            clickableItem(SettingsKeys.AiCacheClear) {
                title(R.string.clear_analysis_cache)
                description(R.string.cache_size)
                icon(R.drawable.ic_delete_sweep)
            }
        }
    }

    /** All searchable pages — single source of truth for the search engine. */
    val allSearchablePages: List<SettingsPage> = listOf(
        settingsPage, lookAndFeelPage, darkThemePage,
        behaviorPage, autoUpdatePage, aboutPage, backupPage,
        aiSettingsPage, backupSchedulerPage,
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
        backupPage.screenId!! to { NavRoutes.BackupAndRestoreScreen(it) },
        aboutPage.screenId!! to { NavRoutes.AboutScreen(it) },
        aiSettingsPage.screenId!! to { NavRoutes.AiModelsScreen },
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
    R.drawable.ic_format_size,
    R.drawable.ic_adb
)






