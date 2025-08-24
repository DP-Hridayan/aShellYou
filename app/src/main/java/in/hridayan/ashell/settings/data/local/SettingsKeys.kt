package `in`.hridayan.ashell.settings.data.local

import android.os.Environment
import androidx.appcompat.app.AppCompatDelegate
import `in`.hridayan.ashell.core.common.SeedColorProvider
import `in`.hridayan.ashell.core.common.constants.GithubReleaseType
import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.domain.model.SortType

enum class SettingsKeys(val default: Any?) {
    LOOK_AND_FEEL(null),
    AUTO_UPDATE(false),
    ABOUT(null),
    BEHAVIOR(null),
    LANGUAGE(null),
    THEME_MODE(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    DARK_THEME(null),
    HIGH_CONTRAST_DARK_MODE(false),
    SEED_COLOR(SeedColorProvider.seedColor),
    DYNAMIC_COLORS(true),
    HAPTICS_AND_VIBRATION(true),
    VERSION(null),
    CHANGELOGS(null),
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
    SAVE_WHOLE_OUTPUT(true),
    SMOOTH_SCROLLING(true),
    COMMANDS(null),
    TELEGRAM(null),
    FIRST_LAUNCH(true),
    BOOKMARK_SORT_TYPE(SortType.AZ)
}