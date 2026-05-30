package `in`.hridayan.ashell.settings.presentation.provider

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.UnfoldMoreDouble
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.presentation.components.floaters.FloatingIconsBackground
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.data.SettingsKeys
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
                key = SettingsKeys.LOOK_AND_FEEL,
                title = R.string.look_and_feel,
                description = R.string.des_look_and_feel,
                icon = R.drawable.ic_pallete
            ),
            clickableItem(
                key = SettingsKeys.BEHAVIOR,
                title = R.string.behavior,
                description = R.string.des_behavior,
                icon = R.drawable.ic_sentiment_neutral
            ),
            clickableItem(
                key = SettingsKeys.QUICK_SETTINGS_TILES,
                title = R.string.qs_tiles,
                description = R.string.des_qs_tiles,
                icon = R.drawable.ic_dashboard
            ),
            clickableItem(
                key = SettingsKeys.AI_MODEL_MANAGER,
                title = R.string.ai_models,
                description = R.string.des_ai_models,
                iconVector = Icons.Rounded.AutoAwesome
            ),
            clickableItem(
                key = SettingsKeys.AUTO_UPDATE,
                title = R.string.auto_update,
                description = R.string.des_auto_update,
                icon = R.drawable.ic_auto_update
            ),
            clickableItem(
                key = SettingsKeys.BACKUP_AND_RESTORE,
                title = R.string.backup_and_restore,
                description = R.string.des_backup_and_restore,
                icon = R.drawable.ic_settings_backup_restore
            ),
            clickableItem(
                key = SettingsKeys.ABOUT,
                title = R.string.about,
                description = R.string.des_about,
                icon = R.drawable.ic_info
            ),
        )
    )

    val lookAndFeelPage = settingsPage(
        screenTitle = R.string.look_and_feel,
        screenId = "look_and_feel",
        group(
            switchItem(
                key = SettingsKeys.DYNAMIC_COLORS,
                title = R.string.dynamic_colors,
                description = R.string.des_dynamic_colors,
                icon = R.drawable.ic_dynamic_color,
                visible = !isSdkLowerThan31
            ),
            clickableItem(
                key = SettingsKeys.PALETTE_STYLE,
                title = R.string.palette_style,
                description = R.string.palette_tonal_spot,
                icon = R.drawable.ic_pallete
            ),
            clickableItem(
                key = SettingsKeys.DARK_THEME,
                title = R.string.dark_theme,
                description = R.string.system,
                iconVector = Icons.Outlined.DarkMode
            ),
        ),
        category(
            title = R.string.font_family,
            clickableItem(
                key = SettingsKeys.FONT_FAMILY,
                title = R.string.font_family,
                description = R.string.des_font_family,
                iconVector = Icons.Rounded.TextFields
            )
        ),
        category(
            title = R.string.ui_scale,
            clickableItem(
                key = SettingsKeys.UI_SCALE,
                title = R.string.ui_scale,
                description = R.string.des_ui_scale,
                icon = R.drawable.ic_high_density
            )
        ),
        category(
            title = R.string.additional_settings,
            switchItem(
                key = SettingsKeys.HAPTICS_AND_VIBRATION,
                title = R.string.haptics_and_vibration,
                description = R.string.des_haptics_and_vibration,
                icon = R.drawable.ic_vibration
            ),
            clickableItem(
                key = SettingsKeys.LANGUAGE,
                title = R.string.default_language,
                description = R.string.des_default_language,
                icon = R.drawable.ic_language
            ),
        ),
    )

    val darkThemePage = settingsPage(
        screenTitle = R.string.dark_theme,
        screenId = "dark_theme",
        group(
            radioGroupItem(
                key = SettingsKeys.THEME_MODE,
                options = RadioGroupOptionsProvider.darkModeOptions
            )
        ),
        category(
            title = R.string.additional_settings,
            switchItem(
                key = SettingsKeys.HIGH_CONTRAST_DARK_MODE,
                title = R.string.high_contrast_dark_mode,
                description = R.string.des_high_contrast_dark_mode,
                icon = R.drawable.ic_amoled_theme
            ),
        ),
    )

    val autoUpdatePage = settingsPage(
        screenTitle = R.string.auto_update,
        screenId = "auto_update",
        group(
            switchBannerItem(key = SettingsKeys.AUTO_UPDATE, title = R.string.enable_auto_update),
        ),
        category(
            title = R.string.update_channel,
            radioGroupItem(
                key = SettingsKeys.GITHUB_RELEASE_TYPE,
                options = RadioGroupOptionsProvider.updateChannelOptions
            ),
        ),
        category(
            title = R.string.additional_settings,
            switchItem(
                key = SettingsKeys.ENABLE_DIRECT_DOWNLOAD,
                title = R.string.enable_direct_download,
                description = R.string.des_enable_direct_download,
                iconVector = Icons.Rounded.Downloading
            ),
        ),
    )

    val behaviorPage = settingsPage(
        screenTitle = R.string.behavior,
        screenId = "behavior",
        category(
            title = R.string.local_adb_shell,
            radioGroupItem(
                key = SettingsKeys.LOCAL_ADB_WORKING_MODE,
                options = RadioGroupOptionsProvider.localAdbShellModeOptions
            ),
        ),
        category(
            title = R.string.terminal,
            switchItem(
                key = SettingsKeys.SMOOTH_SCROLLING,
                title = R.string.smooth_scrolling,
                description = R.string.des_smooth_scroll,
                iconVector = Icons.Rounded.UnfoldMoreDouble
            ),
            switchItem(
                key = SettingsKeys.CLEAR_OUTPUT_CONFIRMATION,
                title = R.string.clear_output_confirmation,
                description = R.string.des_clear_output_confirmation,
                icon = R.drawable.ic_clear
            ),
            switchItem(
                key = SettingsKeys.OVERRIDE_MAXIMUM_BOOKMARKS_LIMIT,
                title = R.string.override_bookmarks_limit,
                description = R.string.des_override_bookmarks,
                icon = R.drawable.ic_bookmarks
            ),
            switchItem(
                key = SettingsKeys.DISABLE_SOFT_KEYBOARD,
                title = R.string.disable_softkey,
                description = R.string.des_disable_softkey,
                icon = R.drawable.ic_disable_keyboard
            ),
        ),
        category(
            title = R.string.terminal_font_style,
            buttonGroupItem(
                key = SettingsKeys.TERMINAL_FONT_STYLE,
                options = listOf(
                    ButtonGroupOption(TerminalFontStyle.MONOSPACE, R.string.monospace),
                    ButtonGroupOption(TerminalFontStyle.SYSTEM_FONT, R.string.system_font),
                )
            ),
        ),
        category(
            title = R.string.file_actions,
            clickableItem(
                key = SettingsKeys.OUTPUT_SAVE_DIRECTORY,
                title = R.string.configure_save_directory,
                description = R.string.des_configure_save_directory,
                icon = R.drawable.ic_directory
            ),
            switchItem(
                key = SettingsKeys.SAVE_WHOLE_OUTPUT,
                title = R.string.save_whole_output,
                description = R.string.des_save_whole_output,
                icon = R.drawable.ic_save_as
            ),
        ),
    )

    val aboutPage = settingsPage(
        screenTitle = R.string.about,
        screenId = "about",
        category(
            title = R.string.contributors,
            clickableItem(
                key = SettingsKeys.CONTRIBUTORS,
                title = R.string.contributors,
                description = R.string.des_contributors,
                icon = R.drawable.ic_crowdsource
            ),
            clickableItem(
                key = SettingsKeys.TRANSLATORS,
                title = R.string.translators,
                description = R.string.des_translators,
                icon = R.drawable.ic_translate
            ),
        ),
        category(
            title = R.string.app,
            clickableItem(
                key = SettingsKeys.CHANGELOGS,
                title = R.string.changelogs,
                description = R.string.des_changelogs,
                icon = R.drawable.ic_changelog
            ),
            clickableItem(
                key = SettingsKeys.LICENSES,
                title = R.string.licenses,
                description = R.string.des_licenses,
                icon = R.drawable.ic_license
            ),
            clickableItem(
                key = SettingsKeys.CRASH_HISTORY,
                title = R.string.crash_history,
                description = R.string.des_crash_history,
                icon = R.drawable.ic_bug
            ),
            clickableItem(
                key = SettingsKeys.REPORT,
                title = R.string.report_issue,
                description = R.string.des_report_issue,
                icon = R.drawable.ic_report
            ),
            clickableItem(
                key = SettingsKeys.FEATURE_REQUEST,
                title = R.string.feature_request,
                description = R.string.des_feature_request,
                icon = R.drawable.ic_add_comment
            ),
        ),
    )

    val backupPage = settingsPage(
        screenTitle = R.string.backup_and_restore,
        screenId = "backup_restore",
        customSlot(BackupScreenCustomSlots.GoogleSignIn),
        category(
            title = R.string.backup,
            clickableItem(
                key = SettingsKeys.BACKUP_APP_SETTINGS,
                title = R.string.backup_settings,
                description = R.string.des_backup_settings,
                icon = R.drawable.ic_handyman
            ),
            clickableItem(
                key = SettingsKeys.BACKUP_APP_DATABASE,
                title = R.string.backup_app_database,
                description = R.string.des_backup_app_database,
                icon = R.drawable.ic_database
            ),
            clickableItem(
                key = SettingsKeys.BACKUP_APP_DATA,
                title = R.string.backup_all_data,
                description = R.string.des_backup_all_data,
                icon = R.drawable.ic_upload_file
            ),
        ),
        customSlot(BackupScreenCustomSlots.LastBackupTime),
        category(
            title = R.string.restore,
            clickableItem(
                key = SettingsKeys.RESTORE_APP_DATA,
                title = R.string.restore_app_data,
                description = R.string.des_restore_app_data,
                icon = R.drawable.ic_restore_page
            ),
        ),
        category(
            title = R.string.reset,
            clickableItem(
                key = SettingsKeys.RESET_APP_SETTINGS,
                title = R.string.reset_app_settings,
                description = R.string.des_reset_app_settings,
                icon = R.drawable.ic_reset_settings
            ),
        ),
    )

    val aiModelsPage = settingsPage(
        screenTitle = R.string.ai_models,
        screenId = "ai_models",
        category(
            title = R.string.models,
            clickableItem(
                key = SettingsKeys.AI_MODELS,
                title = R.string.models,
                description = R.string.des_models,
                iconVector = Icons.Rounded.Memory
            ),
        ),
        category(
            title = R.string.cache_settings,
            switchItem(
                key = SettingsKeys.AI_CACHE_ENABLED,
                title = R.string.ai_cache_enabled,
                description = R.string.des_ai_cache_enabled,
                iconVector = Icons.Rounded.Cached
            ),
            clickableItem(
                key = SettingsKeys.AI_CACHE_DAYS,
                title = R.string.ai_cache_days,
                description = R.string.des_ai_cache_days,
                icon = R.drawable.ic_schedule
            ),
            clickableItem(
                key = SettingsKeys.AI_CACHE_CLEAR,
                title = R.string.clear_analysis_cache,
                description = R.string.cache_size,
                icon = R.drawable.ic_delete_sweep
            )
        ),
    )

    /** All searchable pages — single source of truth for the search engine. */
    val allSearchablePages: List<SettingsPage> = listOf(
        settingsPage, lookAndFeelPage, darkThemePage,
        behaviorPage, autoUpdatePage, aboutPage, backupPage,
        aiModelsPage,
    )

    /**
     * Maps each page's [SettingsPage.screenId] to its [NavRoutes] factory.
     * Defined here — next to the pages — so adding a new page automatically
     * wires up search navigation. No hardcoded strings elsewhere.
     */
    private val navRouteMapping: Map<String, (String?) -> NavRoutes> = mapOf(
        lookAndFeelPage.screenId!! to { NavRoutes.LookAndFeelScreen(it) },
        darkThemePage.screenId!! to { NavRoutes.DarkThemeScreen(it) },
        behaviorPage.screenId!! to { NavRoutes.BehaviorScreen(it) },
        autoUpdatePage.screenId!! to { NavRoutes.AutoUpdateScreen(it) },
        aboutPage.screenId!! to { NavRoutes.AboutScreen(it) },
        backupPage.screenId!! to { NavRoutes.BackupAndRestoreScreen(it) },
        aiModelsPage.screenId!! to { NavRoutes.AiModelManagerScreen(it) },
    )

    /** Resolves a [screenId] to the correct [NavRoutes] destination. */
    fun resolveNavRoute(screenId: String, highlightKey: String? = null): NavRoutes =
        navRouteMapping[screenId]?.invoke(highlightKey) ?: NavRoutes.SettingsScreen
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
)