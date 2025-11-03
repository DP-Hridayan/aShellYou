package `in`.hridayan.ashell.settings.data.local

import android.os.Environment
import androidx.appcompat.app.AppCompatDelegate
import `in`.hridayan.ashell.core.domain.model.GithubReleaseType
import `in`.hridayan.ashell.core.domain.model.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider

enum class SettingsKeys(val default: Any?) {
    LOOK_AND_FEEL(null),
    AUTO_UPDATE(false),
    ABOUT(null),
    BEHAVIOR(null),
    LANGUAGE(null),
    THEME_MODE(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    DARK_THEME(null),
    HIGH_CONTRAST_DARK_MODE(false),
    PRIMARY_SEED(SeedColorProvider.primary),
    SECONDARY_SEED(SeedColorProvider.secondary),
    TERTIARY_SEED(SeedColorProvider.tertiary),
    DYNAMIC_COLORS(true),
    HAPTICS_AND_VIBRATION(true),
    VERSION(null),
    CHANGELOGS(null),
    CRASH_HISTORY(null),
    REPORT(null),
    FEATURE_REQUEST(null),
    GITHUB(null),
    LICENSE(null),
    GITHUB_RELEASE_TYPE(GithubReleaseType.STABLE),
    SAVED_VERSION_CODE(0),
    ENABLE_DIRECT_DOWNLOAD(true),
    BACKUP_AND_RESTORE(null),
    BACKUP_APP_SETTINGS(null),
    BACKUP_APP_DATABASE(null),
    BACKUP_APP_DATA(null),
    RESTORE_APP_DATA(null),
    RESET_APP_SETTINGS(null),
    LAST_BACKUP_TIME(""),
    CLEAR_OUTPUT_CONFIRMATION(true),
    OUTPUT_SAVE_DIRECTORY(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).absolutePath
    ),
    LAST_SAVED_FILE_URI(""),
    LOCAL_ADB_WORKING_MODE(LocalAdbWorkingMode.BASIC),
    DISABLE_SOFT_KEYBOARD(false),
    OVERRIDE_MAXIMUM_BOOKMARKS_LIMIT(false),
    TERMINAL_FONT_STYLE(TerminalFontStyle.MONOSPACE),
    SAVE_WHOLE_OUTPUT(true),
    SMOOTH_SCROLLING(true),
    COMMANDS(null),
    TELEGRAM(null),
    FIRST_LAUNCH(true),
    BOOKMARK_SORT_TYPE(SortType.AZ),
    COMMAND_SORT_TYPE(SortType.AZ)
}