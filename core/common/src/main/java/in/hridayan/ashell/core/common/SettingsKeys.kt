package `in`.hridayan.ashell.core.common

import android.os.Environment
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Stable
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.core.common.SettingsKeys.Companion.entries
import `in`.hridayan.ashell.core.common.SettingsKeys.Companion.valueOf
import `in`.hridayan.settingsdsl.model.SettingsKey
import kotlin.reflect.KClass

/**
 * All settings keys used by the app.
 *
 * Each entry is a typed singleton carrying its own [defaultValue].
 * The [name] property is used as the DataStore preferences key.
 *
 * **Adding a new key**: just add a `data object` here — [entries] and the
 * typed sub-lists are auto-discovered via sealed-class reflection.
 */
@Stable
sealed class SettingsKeys<out T>(
    override val name: String,
    override val defaultValue: T,
) : SettingsKey<T> {

    /** Backward-compatible alias for [defaultValue]. */
    val default: T get() = defaultValue

    // Action only keys

    data object LookAndFeel : SettingsKeys<Nothing?>("LOOK_AND_FEEL", null)
    data object About : SettingsKeys<Nothing?>("ABOUT", null)
    data object Behavior : SettingsKeys<Nothing?>("BEHAVIOR", null)
    data object Language : SettingsKeys<Nothing?>("LANGUAGE", null)
    data object DarkTheme : SettingsKeys<Nothing?>("DARK_THEME", null)
    data object CustomUiScale : SettingsKeys<Nothing?>("CUSTOM_UI_SCALE", null)
    data object Version : SettingsKeys<Nothing?>("VERSION", null)
    data object Changelogs : SettingsKeys<Nothing?>("CHANGELOGS", null)
    data object CrashHistory : SettingsKeys<Nothing?>("CRASH_HISTORY", null)
    data object Report : SettingsKeys<Nothing?>("REPORT", null)
    data object FeatureRequest : SettingsKeys<Nothing?>("FEATURE_REQUEST", null)
    data object Github : SettingsKeys<Nothing?>("GITHUB", null)
    data object Licenses : SettingsKeys<Nothing?>("LICENSES", null)
    data object Commands : SettingsKeys<Nothing?>("COMMANDS", null)
    data object Telegram : SettingsKeys<Nothing?>("TELEGRAM", null)
    data object BackupAndRestore : SettingsKeys<Nothing?>("BACKUP_AND_RESTORE", null)
    data object BackupAppSettings : SettingsKeys<Nothing?>("BACKUP_APP_SETTINGS", null)
    data object BackupAppDatabase : SettingsKeys<Nothing?>("BACKUP_APP_DATABASE", null)
    data object BackupAppData : SettingsKeys<Nothing?>("BACKUP_APP_DATA", null)
    data object RestoreAppData : SettingsKeys<Nothing?>("RESTORE_APP_DATA", null)
    data object ResetAppSettings : SettingsKeys<Nothing?>("RESET_APP_SETTINGS", null)
    data object BackupScheduler : SettingsKeys<Nothing?>("BACKUP_SCHEDULER", null)
    data object AutoBackupTime : SettingsKeys<Nothing?>("AUTO_BACKUP_TIME", null)
    data object AutoBackupFolder : SettingsKeys<Nothing?>("AUTO_BACKUP_FOLDER", null)
    data object QuickSettingsTiles : SettingsKeys<Nothing?>("QUICK_SETTINGS_TILES", null)
    data object Translators : SettingsKeys<Nothing?>("TRANSLATORS", null)
    data object Contributors : SettingsKeys<Nothing?>("CONTRIBUTORS", null)
    data object AiModelManager : SettingsKeys<Nothing?>("AI_MODEL_MANAGER", null)
    data object AiModels : SettingsKeys<Nothing?>("AI_MODELS", null)
    data object AiCacheClear : SettingsKeys<Nothing?>("AI_CACHE_CLEAR", null)

    // Boolean Keys

    data object AutoUpdate : SettingsKeys<Boolean>("AUTO_UPDATE", false)
    data object AutoScaleUi : SettingsKeys<Boolean>("AUTO_SCALE_UI", false)
    data object HighContrastDarkMode : SettingsKeys<Boolean>("HIGH_CONTRAST_DARK_MODE", false)
    data object AutoDarkModeOnBatterySaver :
        SettingsKeys<Boolean>("AutoDarkModeOnBatterySaver", false)

    data object DynamicColors : SettingsKeys<Boolean>("DYNAMIC_COLORS", true)
    data object HapticsAndVibration : SettingsKeys<Boolean>("HAPTICS_AND_VIBRATION", true)
    data object EnableDirectDownload : SettingsKeys<Boolean>("ENABLE_DIRECT_DOWNLOAD", true)
    data object ClearOutputConfirmation : SettingsKeys<Boolean>("CLEAR_OUTPUT_CONFIRMATION", true)
    data object DisableSoftKeyboard : SettingsKeys<Boolean>("DISABLE_SOFT_KEYBOARD", false)
    data object OverrideMaximumBookmarksLimit :
        SettingsKeys<Boolean>("OVERRIDE_MAXIMUM_BOOKMARKS_LIMIT", false)

    data object SaveWholeOutput : SettingsKeys<Boolean>("SAVE_WHOLE_OUTPUT", true)
    data object SmoothScrolling : SettingsKeys<Boolean>("SMOOTH_SCROLLING", true)
    data object FirstLaunch : SettingsKeys<Boolean>("FIRST_LAUNCH", true)
    data object NewCommandsAvailable : SettingsKeys<Boolean>("NEW_COMMANDS_AVAILABLE", true)
    data object AiCacheEnabled : SettingsKeys<Boolean>("AI_CACHE_ENABLED", true)
    data object AutoBackupEnabled : SettingsKeys<Boolean>("AUTO_BACKUP_ENABLED", false)
    data object AutoBackupDeleteExisting :
        SettingsKeys<Boolean>("AUTO_BACKUP_DELETE_EXISTING", true)

    data object AutoBackupLocalEnabled : SettingsKeys<Boolean>("AUTO_BACKUP_LOCAL_ENABLED", true)
    data object AutoBackupCloudEnabled : SettingsKeys<Boolean>("AUTO_BACKUP_CLOUD_ENABLED", true)


    // Float keys

    data object ScreenDensityMultiplier : SettingsKeys<Float>("SCREEN_DENSITY_MULTIPLIER", 1f)
    data object FontSizeMultiplier : SettingsKeys<Float>("FONT_SIZE_MULTIPLIER", 1f)


    // Int Keys

    data object ThemeMode :
        SettingsKeys<Int>("THEME_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    data object PrimarySeed : SettingsKeys<Int>("PRIMARY_SEED", SeedColorProvider.primary)
    data object PaletteStyle : SettingsKeys<Int>(
        "PALETTE_STYLE",
        `in`.hridayan.ashell.core.domain.model.PaletteStyle.TONAL_SPOT.ordinal
    )

    data object GithubReleaseType :
        SettingsKeys<Int>(
            "GITHUB_RELEASE_TYPE",
            1
        )

    data object SavedVersionCode : SettingsKeys<Int>("SAVED_VERSION_CODE", 0)
    data object LocalAdbWorkingMode :
        SettingsKeys<Int>(
            "LOCAL_ADB_WORKING_MODE",
            `in`.hridayan.ashell.core.domain.model.LocalAdbWorkingMode.BASIC
        )

    data object TerminalFontStyle :
        SettingsKeys<Int>(
            "TERMINAL_FONT_STYLE",
            `in`.hridayan.ashell.core.domain.model.TerminalFontStyle.MONOSPACE
        )

    data object BookmarkSortType : SettingsKeys<Int>("BOOKMARK_SORT_TYPE", SortType.AZ)
    data object CommandSortType : SettingsKeys<Int>("COMMAND_SORT_TYPE", SortType.AZ)
    data object AiCacheDays : SettingsKeys<Int>("AI_CACHE_DAYS", 30)
    data object AutoBackupTimeHour : SettingsKeys<Int>("AUTO_BACKUP_TIME_HOUR", 2)
    data object AutoBackupTimeMinute : SettingsKeys<Int>("AUTO_BACKUP_TIME_MINUTE", 0)
    data object AutoBackupFrequency : SettingsKeys<Int>("AUTO_BACKUP_FREQUENCY", 0)
    data object AutoBackupType : SettingsKeys<Int>("AUTO_BACKUP_TYPE", 2)
    data object FontFamily : SettingsKeys<Int>("FONT_FAMILY", 0)

    // String Keys

    data object LastLocalBackupTime : SettingsKeys<String>("LAST_LOCAL_BACKUP_TIME", "")
    data object OutputSaveDirectory : SettingsKeys<String>(
        "OUTPUT_SAVE_DIRECTORY",
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    )

    data object LastSavedFileUri : SettingsKeys<String>("LAST_SAVED_FILE_URI", "")
    data object GoogleAccountEmail : SettingsKeys<String>("GOOGLE_ACCOUNT_EMAIL", "")
    data object GoogleAccountPhotoUrl : SettingsKeys<String>("GOOGLE_ACCOUNT_PHOTO_URL", "")
    data object LastCloudBackupTime : SettingsKeys<String>("LAST_CLOUD_BACKUP_TIME", "")
    data object LastCloudBackupType : SettingsKeys<String>("LAST_CLOUD_BACKUP_TYPE", "None")
    data object LastLocalBackupType : SettingsKeys<String>("LAST_LOCAL_BACKUP_TYPE", "None")
    data object RecentSearchKeys : SettingsKeys<String>("RECENT_SEARCH_KEYS", "")
    data object AutoBackupFolderUri : SettingsKeys<String>("AUTO_BACKUP_FOLDER_URI", "")
    data object AutoBackupFolderName : SettingsKeys<String>("AUTO_BACKUP_FOLDER_NAME", "")
    data object LastAutoBackupLocalSuccessTime :
        SettingsKeys<String>("LAST_AUTO_BACKUP_LOCAL_SUCCESS_TIME", "")

    data object LastAutoBackupLocalError : SettingsKeys<String>("LAST_AUTO_BACKUP_LOCAL_ERROR", "")
    data object LastAutoBackupCloudSuccessTime :
        SettingsKeys<String>("LAST_AUTO_BACKUP_CLOUD_SUCCESS_TIME", "")

    data object LastAutoBackupCloudError : SettingsKeys<String>("LAST_AUTO_BACKUP_CLOUD_ERROR", "")
    data object SelectedModelId :
        SettingsKeys<String>("SELECTED_MODEL_ID", "qwen2.5-0.5b-instruct-q4_k_m")

    companion object {
        /**
         * All key instances — auto-discovered from sealed subclasses.
         * Adding a new `data object` is all you need to do.
         */
        @Suppress("UNCHECKED_CAST")
        val entries: List<SettingsKeys<*>> by lazy {
            SettingsKeys::class.sealedSubclasses
                .mapNotNull { (it as KClass<SettingsKeys<*>>).objectInstance }
        }

        /** Boolean keys only — auto-derived from [entries]. */
        @Suppress("UNCHECKED_CAST")
        val booleanEntries: List<SettingsKeys<Boolean>> by lazy {
            entries.filter { it.default is Boolean } as List<SettingsKeys<Boolean>>
        }

        /** Int keys only — auto-derived from [entries]. */
        @Suppress("UNCHECKED_CAST")
        val intEntries: List<SettingsKeys<Int>> by lazy {
            entries.filter { it.default is Int } as List<SettingsKeys<Int>>
        }

        /** String keys only — auto-derived from [entries]. */
        @Suppress("UNCHECKED_CAST")
        val stringEntries: List<SettingsKeys<String>> by lazy {
            entries.filter { it.default is String } as List<SettingsKeys<String>>
        }

        /** Replaces enum `valueOf()`. Throws [NoSuchElementException] if not found. */
        fun valueOf(name: String): SettingsKeys<*> =
            entries.first { it.name == name }

        /** Safe version of [valueOf] — returns null if not found. */
        fun valueOfOrNull(name: String): SettingsKeys<*>? =
            entries.firstOrNull { it.name == name }
    }
}
