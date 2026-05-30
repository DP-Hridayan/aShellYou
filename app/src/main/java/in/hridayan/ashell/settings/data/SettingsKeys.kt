package `in`.hridayan.ashell.settings.data

import android.os.Environment
import androidx.appcompat.app.AppCompatDelegate
import `in`.hridayan.ashell.ai.data.local.model.ModelRegistry
import `in`.hridayan.ashell.core.domain.model.GithubReleaseType
import `in`.hridayan.ashell.core.domain.model.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.domain.model.PaletteStyle
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.settings.data.SettingsKeys.Companion.entries
import `in`.hridayan.ashell.settings.data.SettingsKeys.Companion.valueOf
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
sealed class SettingsKeys<out T>(
    override val name: String,
    override val defaultValue: T,
) : SettingsKey<T> {

    /** Backward-compatible alias for [defaultValue]. */
    val default: T get() = defaultValue

    // Action only keys

    data object LOOK_AND_FEEL : SettingsKeys<Nothing?>("LOOK_AND_FEEL", null)
    data object ABOUT : SettingsKeys<Nothing?>("ABOUT", null)
    data object BEHAVIOR : SettingsKeys<Nothing?>("BEHAVIOR", null)
    data object LANGUAGE : SettingsKeys<Nothing?>("LANGUAGE", null)
    data object DARK_THEME : SettingsKeys<Nothing?>("DARK_THEME", null)
    data object UI_SCALE : SettingsKeys<Nothing?>("UI_SCALE", null)
    data object VERSION : SettingsKeys<Nothing?>("VERSION", null)
    data object CHANGELOGS : SettingsKeys<Nothing?>("CHANGELOGS", null)
    data object CRASH_HISTORY : SettingsKeys<Nothing?>("CRASH_HISTORY", null)
    data object REPORT : SettingsKeys<Nothing?>("REPORT", null)
    data object FEATURE_REQUEST : SettingsKeys<Nothing?>("FEATURE_REQUEST", null)
    data object GITHUB : SettingsKeys<Nothing?>("GITHUB", null)
    data object LICENSES : SettingsKeys<Nothing?>("LICENSES", null)
    data object COMMANDS : SettingsKeys<Nothing?>("COMMANDS", null)
    data object TELEGRAM : SettingsKeys<Nothing?>("TELEGRAM", null)
    data object BACKUP_AND_RESTORE : SettingsKeys<Nothing?>("BACKUP_AND_RESTORE", null)
    data object BACKUP_APP_SETTINGS : SettingsKeys<Nothing?>("BACKUP_APP_SETTINGS", null)
    data object BACKUP_APP_DATABASE : SettingsKeys<Nothing?>("BACKUP_APP_DATABASE", null)
    data object BACKUP_APP_DATA : SettingsKeys<Nothing?>("BACKUP_APP_DATA", null)
    data object RESTORE_APP_DATA : SettingsKeys<Nothing?>("RESTORE_APP_DATA", null)
    data object RESET_APP_SETTINGS : SettingsKeys<Nothing?>("RESET_APP_SETTINGS", null)
    data object BACKUP_SCHEDULER : SettingsKeys<Nothing?>("BACKUP_SCHEDULER", null)
    data object QUICK_SETTINGS_TILES : SettingsKeys<Nothing?>("QUICK_SETTINGS_TILES", null)
    data object TRANSLATORS : SettingsKeys<Nothing?>("TRANSLATORS", null)
    data object CONTRIBUTORS : SettingsKeys<Nothing?>("CONTRIBUTORS", null)
    data object AI_MODEL_MANAGER : SettingsKeys<Nothing?>("AI_MODEL_MANAGER", null)
    data object AI_MODELS : SettingsKeys<Nothing?>("AI_MODELS", null)
    data object AI_CACHE_CLEAR : SettingsKeys<Nothing?>("AI_CACHE_CLEAR", null)

    // Boolean Keys

    data object AUTO_UPDATE : SettingsKeys<Boolean>("AUTO_UPDATE", false)
    data object HIGH_CONTRAST_DARK_MODE : SettingsKeys<Boolean>("HIGH_CONTRAST_DARK_MODE", false)
    data object DYNAMIC_COLORS : SettingsKeys<Boolean>("DYNAMIC_COLORS", true)
    data object HAPTICS_AND_VIBRATION : SettingsKeys<Boolean>("HAPTICS_AND_VIBRATION", true)
    data object ENABLE_DIRECT_DOWNLOAD : SettingsKeys<Boolean>("ENABLE_DIRECT_DOWNLOAD", true)
    data object CLEAR_OUTPUT_CONFIRMATION : SettingsKeys<Boolean>("CLEAR_OUTPUT_CONFIRMATION", true)
    data object DISABLE_SOFT_KEYBOARD : SettingsKeys<Boolean>("DISABLE_SOFT_KEYBOARD", false)
    data object OVERRIDE_MAXIMUM_BOOKMARKS_LIMIT :
        SettingsKeys<Boolean>("OVERRIDE_MAXIMUM_BOOKMARKS_LIMIT", false)

    data object SAVE_WHOLE_OUTPUT : SettingsKeys<Boolean>("SAVE_WHOLE_OUTPUT", true)
    data object SMOOTH_SCROLLING : SettingsKeys<Boolean>("SMOOTH_SCROLLING", true)
    data object FIRST_LAUNCH : SettingsKeys<Boolean>("FIRST_LAUNCH", true)
    data object NEW_COMMANDS_AVAILABLE : SettingsKeys<Boolean>("NEW_COMMANDS_AVAILABLE", true)
    data object AI_CACHE_ENABLED : SettingsKeys<Boolean>("AI_CACHE_ENABLED", true)


    // Float keys

    data object SCREEN_DENSITY_MULTIPLIER : SettingsKeys<Float>("SCREEN_DENSITY_MULTIPLIER", 1f)
    data object FONT_SIZE_MULTIPLIER : SettingsKeys<Float>("FONT_SIZE_MULTIPLIER", 1f)


    // Int Keys

    data object THEME_MODE :
        SettingsKeys<Int>("THEME_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    data object PRIMARY_SEED : SettingsKeys<Int>("PRIMARY_SEED", SeedColorProvider.primary)
    data object PALETTE_STYLE : SettingsKeys<Int>("PALETTE_STYLE", PaletteStyle.TONAL_SPOT.ordinal)
    data object GITHUB_RELEASE_TYPE :
        SettingsKeys<Int>("GITHUB_RELEASE_TYPE", GithubReleaseType.STABLE_GITHUB)

    data object SAVED_VERSION_CODE : SettingsKeys<Int>("SAVED_VERSION_CODE", 0)
    data object LOCAL_ADB_WORKING_MODE :
        SettingsKeys<Int>("LOCAL_ADB_WORKING_MODE", LocalAdbWorkingMode.BASIC)

    data object TERMINAL_FONT_STYLE :
        SettingsKeys<Int>("TERMINAL_FONT_STYLE", TerminalFontStyle.MONOSPACE)

    data object BOOKMARK_SORT_TYPE : SettingsKeys<Int>("BOOKMARK_SORT_TYPE", SortType.AZ)
    data object COMMAND_SORT_TYPE : SettingsKeys<Int>("COMMAND_SORT_TYPE", SortType.AZ)
    data object AI_CACHE_DAYS : SettingsKeys<Int>("AI_CACHE_DAYS", 30)
    data object FONT_FAMILY : SettingsKeys<Int>("FONT_FAMILY", 0)

    // String Keys

    data object LAST_LOCAL_BACKUP_TIME : SettingsKeys<String>("LAST_LOCAL_BACKUP_TIME", "")
    data object OUTPUT_SAVE_DIRECTORY : SettingsKeys<String>(
        "OUTPUT_SAVE_DIRECTORY",
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    )

    data object LAST_SAVED_FILE_URI : SettingsKeys<String>("LAST_SAVED_FILE_URI", "")
    data object GOOGLE_ACCOUNT_EMAIL : SettingsKeys<String>("GOOGLE_ACCOUNT_EMAIL", "")
    data object GOOGLE_ACCOUNT_PHOTO_URL : SettingsKeys<String>("GOOGLE_ACCOUNT_PHOTO_URL", "")
    data object LAST_CLOUD_BACKUP_TIME : SettingsKeys<String>("LAST_CLOUD_BACKUP_TIME", "")
    data object LAST_CLOUD_BACKUP_TYPE : SettingsKeys<String>("LAST_CLOUD_BACKUP_TYPE", "None")
    data object LAST_LOCAL_BACKUP_TYPE : SettingsKeys<String>("LAST_LOCAL_BACKUP_TYPE", "None")
    data object RECENT_SEARCH_KEYS : SettingsKeys<String>("RECENT_SEARCH_KEYS", "")
    data object SELECTED_MODEL_ID :
        SettingsKeys<String>("SELECTED_MODEL_ID", ModelRegistry.defaultModel.id)

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