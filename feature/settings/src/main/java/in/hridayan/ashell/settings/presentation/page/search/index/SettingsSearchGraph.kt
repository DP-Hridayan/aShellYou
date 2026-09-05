package `in`.hridayan.ashell.settings.presentation.page.search.index

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.FeatureConfig
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.settingsdsl.search.SearchGraph
import `in`.hridayan.settingsdsl.search.SearchScreenScope
import `in`.hridayan.settingsdsl.search.searchGraph

private const val SCREEN_ID_SETTINGS = "settings"
private const val SCREEN_ID_LOOK_AND_FEEL = "look_and_feel"
private const val SCREEN_ID_DARK_THEME = "dark_theme"
private const val SCREEN_ID_BEHAVIOR = "behavior"
private const val SCREEN_ID_AUTO_UPDATE = "auto_update"
private const val SCREEN_ID_ABOUT = "about"
private const val SCREEN_ID_BACKUP_AND_RESTORE = "backup_and_restore"
private const val SCREEN_ID_BACKUP_SCHEDULER = "backup_scheduler"
private const val SCREEN_ID_AI_MODELS = "ai_models"

/**
 * Builds the settings search index.
 *
 * Availability gates are read here, in composition, and passed to [remember] as keys, so the graph
 * is rebuilt whenever one changes. A setting that is currently unreachable is therefore absent
 * from the index rather than filtered out of results later.
 *
 * @param navController Captured by each screen's navigation lambda.
 */
@Composable
fun rememberSettingsSearchGraph(navController: NavController): SearchGraph {
    val settings = LocalSettings.current
    val aiEnabled = FeatureConfig.isAiEnabled
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorEnabled = settings[SettingsKeys.DynamicColors]
    val customSchemeApplied = settings[SettingsKeys.UserGeneratedColorSchemeApplied]

    return remember(
        navController,
        aiEnabled,
        supportsDynamicColor,
        dynamicColorEnabled,
        customSchemeApplied,
    ) {
        searchGraph {
            screen(
                id = SCREEN_ID_SETTINGS,
                title = R.string.settings,
                navigate = { navController.navigate(NavRoutes.SettingsScreen) { launchSingleTop = true } },
            ) {
                rootEntries(aiEnabled)
                lookAndFeelScreen(
                    navController = navController,
                    supportsDynamicColor = supportsDynamicColor,
                    dynamicColorEnabled = dynamicColorEnabled,
                    customSchemeApplied = customSchemeApplied,
                )
                behaviorScreen(navController)
                autoUpdateScreen(navController)
                aboutScreen(navController)
                backupAndRestoreScreen(navController)
                aiModelsScreen(navController, aiEnabled)
            }
        }
    }
}

private fun SearchScreenScope.rootEntries(aiEnabled: Boolean) {
    entry(
        key = SettingsKeys.LookAndFeel,
        title = R.string.look_and_feel,
        description = R.string.des_look_and_feel,
        icon = R.drawable.ic_pallete,
    )

    entry(
        key = SettingsKeys.Behavior,
        title = R.string.behavior,
        description = R.string.des_behavior,
        icon = R.drawable.ic_sentiment_neutral,
    )

    entry(
        key = SettingsKeys.QuickSettingsTiles,
        title = R.string.qs_tiles,
        description = R.string.des_qs_tiles,
        icon = R.drawable.ic_dashboard,
    )

    entry(
        key = SettingsKeys.AiModels,
        title = R.string.ai_models,
        description = R.string.des_ai_models,
        icon = R.drawable.ic_cloud_model,
        availableWhen = aiEnabled,
    )

    entry(
        key = SettingsKeys.AutoUpdate,
        title = R.string.auto_update,
        description = R.string.des_auto_update,
        icon = R.drawable.ic_auto_update,
    )

    entry(
        key = SettingsKeys.BackupAndRestore,
        title = R.string.backup_and_restore,
        description = R.string.des_backup_and_restore,
        icon = R.drawable.ic_settings_backup_restore,
    )

    entry(
        key = SettingsKeys.About,
        title = R.string.about,
        description = R.string.des_about,
        icon = R.drawable.ic_info,
    )
}

private fun SearchScreenScope.lookAndFeelScreen(
    navController: NavController,
    supportsDynamicColor: Boolean,
    dynamicColorEnabled: Boolean,
    customSchemeApplied: Boolean,
) {
    screen(
        id = SCREEN_ID_LOOK_AND_FEEL,
        title = R.string.look_and_feel,
        navigate = { navController.navigate(NavRoutes.LookAndFeelScreen) { launchSingleTop = true } },
    ) {
        entry(
            key = SettingsKeys.DynamicColors,
            title = R.string.dynamic_colors,
            description = R.string.des_dynamic_colors,
            icon = R.drawable.ic_dynamic_color,
            availableWhen = supportsDynamicColor,
        )
        entry(
            key = SettingsKeys.PaletteStyle,
            title = R.string.palette_style,
            description = R.string.des_palette_style,
            icon = R.drawable.ic_styles,
            availableWhen = !(dynamicColorEnabled || customSchemeApplied),
        )
        entry(
            key = SettingsKeys.DarkTheme,
            title = R.string.dark_theme,
            description = R.string.des_dark_theme,
            availableWhen = !customSchemeApplied || dynamicColorEnabled,
        )
        entry(
            key = SettingsKeys.FontFamily,
            title = R.string.font_family,
            description = R.string.des_font_family,
        )
        entry(
            key = SettingsKeys.AutoScaleUi,
            title = R.string.auto_scale_ui,
            description = R.string.des_auto_scale_ui,
            icon = R.drawable.ic_transform,
        )
        entry(
            key = SettingsKeys.CustomUiScale,
            title = R.string.custom_ui_scale,
            description = R.string.des_ui_scale,
            icon = R.drawable.ic_high_density,
        )
        entry(
            key = SettingsKeys.HapticsAndVibration,
            title = R.string.haptics_and_vibration,
            description = R.string.des_haptics_and_vibration,
            icon = R.drawable.ic_vibration,
        )
        entry(
            key = SettingsKeys.Language,
            title = R.string.default_language,
            description = R.string.des_default_language,
            icon = R.drawable.ic_language,
        )

        darkThemeScreen(navController)
    }
}

private fun SearchScreenScope.darkThemeScreen(navController: NavController) {
    screen(
        id = SCREEN_ID_DARK_THEME,
        title = R.string.dark_theme,
        navigate = { navController.navigate(NavRoutes.DarkThemeScreen) { launchSingleTop = true } },
    ) {
        entry(
            key = SettingsKeys.ThemeMode,
            title = R.string.theme_mode,
            description = R.string.des_dark_theme,
            keywords = listOf(R.string.system, R.string.on, R.string.off),
        )
        entry(
            key = SettingsKeys.AutoDarkModeOnBatterySaver,
            title = R.string.auto_dark_mode,
            description = R.string.des_auto_dark_mode,
            icon = R.drawable.ic_night_sight_auto,
        )
        entry(
            key = SettingsKeys.HighContrastDarkMode,
            title = R.string.high_contrast_dark_mode,
            description = R.string.des_high_contrast_dark_mode,
            icon = R.drawable.ic_amoled_theme,
        )
    }
}

private fun SearchScreenScope.behaviorScreen(navController: NavController) {
    screen(
        id = SCREEN_ID_BEHAVIOR,
        title = R.string.behavior,
        navigate = { navController.navigate(NavRoutes.BehaviorScreen) { launchSingleTop = true } },
    ) {
        entry(
            key = SettingsKeys.LocalAdbWorkingMode,
            title = R.string.local_adb_shell,
            keywords = listOf(
                R.string.basic_shell,
                R.string.shizuku,
                R.string.root,
                R.string.tcpip_mode,
            ),
        )
        entry(
            key = SettingsKeys.DefaultLaunchIsLocalAdb,
            title = R.string.set_local_adb_as_default_launch,
            description = R.string.des_set_local_adb_as_default_launch,
            icon = R.drawable.ic_rocket_launch,
        )
        entry(
            key = SettingsKeys.SmoothScrolling,
            title = R.string.smooth_scrolling,
            description = R.string.des_smooth_scroll,
        )
        entry(
            key = SettingsKeys.ClearOutputConfirmation,
            title = R.string.clear_output_confirmation,
            description = R.string.des_clear_output_confirmation,
            icon = R.drawable.ic_clear,
        )
        entry(
            key = SettingsKeys.OverrideMaximumBookmarksLimit,
            title = R.string.override_bookmarks_limit,
            description = R.string.des_override_bookmarks,
            icon = R.drawable.ic_bookmarks,
        )
        entry(
            key = SettingsKeys.DisableSoftKeyboard,
            title = R.string.disable_softkey,
            description = R.string.des_disable_softkey,
            icon = R.drawable.ic_disable_keyboard,
        )
        entry(
            key = SettingsKeys.TerminalFontStyle,
            title = R.string.terminal_font_style,
            keywords = listOf(R.string.monospace, R.string.system_font),
        )
        entry(
            key = SettingsKeys.OutputSaveDirectory,
            title = R.string.configure_save_directory,
            description = R.string.des_configure_save_directory,
            icon = R.drawable.ic_directory,
        )
        entry(
            key = SettingsKeys.SaveWholeOutput,
            title = R.string.save_whole_output,
            description = R.string.des_save_whole_output,
            icon = R.drawable.ic_save_as,
        )
    }
}

private fun SearchScreenScope.autoUpdateScreen(navController: NavController) {
    screen(
        id = SCREEN_ID_AUTO_UPDATE,
        title = R.string.auto_update,
        navigate = { navController.navigate(NavRoutes.AutoUpdateScreen) { launchSingleTop = true } },
    ) {
        entry(
            key = SettingsKeys.AutoUpdate,
            title = R.string.enable_auto_update,
            description = R.string.des_auto_update,
            icon = R.drawable.ic_auto_update,
        )
        entry(
            key = SettingsKeys.GithubReleaseType,
            title = R.string.update_channel,
            description = R.string.des_update_channel,
            keywords = listOf(
                R.string.stable_fdroid,
                R.string.stable_github,
                R.string.pre_release_github,
            ),
        )
        entry(
            key = SettingsKeys.EnableDirectDownload,
            title = R.string.enable_direct_download,
            description = R.string.des_enable_direct_download,
        )
    }
}

private fun SearchScreenScope.aboutScreen(navController: NavController) {
    screen(
        id = SCREEN_ID_ABOUT,
        title = R.string.about,
        navigate = { navController.navigate(NavRoutes.AboutScreen) { launchSingleTop = true } },
    ) {
        entry(
            key = SettingsKeys.Contributors,
            title = R.string.contributors,
            description = R.string.des_contributors,
            icon = R.drawable.ic_crowdsource,
        )
        entry(
            key = SettingsKeys.Translators,
            title = R.string.translators,
            description = R.string.des_translators,
            icon = R.drawable.ic_translate,
        )
        entry(
            key = SettingsKeys.Changelogs,
            title = R.string.changelogs,
            description = R.string.des_changelogs,
            icon = R.drawable.ic_changelog,
        )
        entry(
            key = SettingsKeys.Report,
            title = R.string.report_issue,
            description = R.string.des_report_issue,
            icon = R.drawable.ic_report,
        )
        entry(
            key = SettingsKeys.FeatureRequest,
            title = R.string.feature_request,
            description = R.string.des_feature_request,
            icon = R.drawable.ic_add_comment,
        )
        entry(
            key = SettingsKeys.CrashHistory,
            title = R.string.crash_history,
            description = R.string.des_crash_history,
            icon = R.drawable.ic_bug,
        )
        entry(
            key = SettingsKeys.Licenses,
            title = R.string.libraries_and_licenses,
            description = R.string.des_libraries_and_licenses,
            icon = R.drawable.ic_license,
        )
        entry(
            key = SettingsKeys.PrivacyPolicy,
            title = R.string.privacy_policy,
            description = R.string.des_privacy_policy,
            icon = R.drawable.ic_privacy_tip,
        )
    }
}

private fun SearchScreenScope.backupAndRestoreScreen(navController: NavController) {
    screen(
        id = SCREEN_ID_BACKUP_AND_RESTORE,
        title = R.string.backup_and_restore,
        navigate = { navController.navigate(NavRoutes.BackupAndRestoreScreen) { launchSingleTop = true } },
    ) {
        entry(
            key = SettingsKeys.BackupAppSettings,
            title = R.string.backup_settings,
            description = R.string.des_backup_settings,
            icon = R.drawable.ic_handyman,
        )
        entry(
            key = SettingsKeys.BackupAppDatabase,
            title = R.string.backup_app_database,
            description = R.string.des_backup_app_database,
            icon = R.drawable.ic_database,
        )
        entry(
            key = SettingsKeys.BackupAppData,
            title = R.string.backup_all_data,
            description = R.string.des_backup_all_data,
            icon = R.drawable.ic_upload_file,
        )
        entry(
            key = SettingsKeys.BackupScheduler,
            title = R.string.backup_scheduler,
            description = R.string.des_backup_scheduler,
            icon = R.drawable.ic_schedule,
        )
        entry(
            key = SettingsKeys.RestoreAppData,
            title = R.string.restore_app_data,
            description = R.string.des_restore_app_data,
            icon = R.drawable.ic_restore_page,
        )
        entry(
            key = SettingsKeys.ResetAppSettings,
            title = R.string.reset_app_settings,
            description = R.string.des_reset_app_settings,
            icon = R.drawable.ic_reset_settings,
        )

        backupSchedulerScreen(navController)
    }
}

private fun SearchScreenScope.backupSchedulerScreen(navController: NavController) {
    screen(
        id = SCREEN_ID_BACKUP_SCHEDULER,
        title = R.string.backup_scheduler,
        navigate = { navController.navigate(NavRoutes.BackupSchedulerScreen) { launchSingleTop = true } },
    ) {
        entry(
            key = SettingsKeys.AutoBackupEnabled,
            title = R.string.enable_auto_backup,
        )
        entry(
            key = SettingsKeys.AutoBackupTime,
            title = R.string.backup_time,
            description = R.string.des_auto_backup_time,
            icon = R.drawable.ic_schedule,
        )
        entry(
            key = SettingsKeys.AutoBackupFrequency,
            title = R.string.frequency,
            keywords = listOf(R.string.daily, R.string.weekly, R.string.monthly),
        )
        entry(
            key = SettingsKeys.AutoBackupType,
            title = R.string.auto_backup_content_type,
            keywords = listOf(
                R.string.all_data,
                R.string.settings_only,
                R.string.databases_only,
            ),
        )
        entry(
            key = SettingsKeys.AutoBackupFolder,
            title = R.string.auto_backup_folder,
            description = R.string.des_auto_backup_folder,
            icon = R.drawable.ic_directory,
        )
        entry(
            key = SettingsKeys.AutoBackupDeleteExisting,
            title = R.string.auto_delete_existing_backups,
            description = R.string.des_auto_delete_existing_backups,
            icon = R.drawable.ic_delete_sweep,
        )
    }
}

private fun SearchScreenScope.aiModelsScreen(navController: NavController, aiEnabled: Boolean) {
    screen(
        id = SCREEN_ID_AI_MODELS,
        title = R.string.ai_models,
        navigate = { navController.navigate(NavRoutes.AiModelsScreen) { launchSingleTop = true } },
    ) {
        availableWhen(aiEnabled)

        entry(
            key = SettingsKeys.CloudModels,
            title = R.string.cloud_models,
            description = R.string.des_cloud_models,
            icon = R.drawable.ic_cloud_model,
        )
        entry(
            key = SettingsKeys.AiSkillCommandExecution,
            title = R.string.command_execution,
            description = R.string.des_command_execution,
            icon = R.drawable.ic_terminal,
        )
        entry(
            key = SettingsKeys.AiSkillQuickSettings,
            title = R.string.quick_settings_tiles,
            description = R.string.des_quick_settings_tiles,
            icon = R.drawable.ic_dashboard,
        )
        entry(
            key = SettingsKeys.AiSkillPackages,
            title = R.string.packages,
            description = R.string.des_packages,
            icon = R.drawable.ic_package,
        )
        entry(
            key = SettingsKeys.AiSkillDatabase,
            title = R.string.database_modification,
            description = R.string.des_database_modification,
            icon = R.drawable.ic_database,
        )
        entry(
            key = SettingsKeys.AiCacheEnabled,
            title = R.string.ai_cache_enabled,
            description = R.string.des_ai_cache_enabled,
        )
        entry(
            key = SettingsKeys.AiCacheDays,
            title = R.string.ai_cache_days,
            description = R.string.des_ai_cache_days,
            icon = R.drawable.ic_schedule,
        )
        entry(
            key = SettingsKeys.AiCacheClear,
            title = R.string.clear_analysis_cache,
            description = R.string.des_clear_analysis_cache,
            icon = R.drawable.ic_delete_sweep,
        )
    }
}
