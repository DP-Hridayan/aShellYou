package `in`.hridayan.ashell.config

import android.os.Environment
import `in`.hridayan.ashell.BuildConfig
import java.util.Collections

interface Const {
    /* <--------NAMES OF CONTRIBUTORS -------> */
    enum class Contributors(val contributorsName: String, val github: String) {
        // assigning names and their github profile links to each contributors
        HRIDAYAN("Hridayan", "https://github.com/DP-Hridayan"),
        KRISHNA("Krishna", "https://github.com/KrishnaSSH"),
        STARRY("Stɑrry Shivɑm", "https://github.com/starry-shivam"),
        DISAGREE("DrDisagree", "https://github.com/Mahmud0808"),
        RIKKA("RikkaApps", "https://github.com/RikkaApps/Shizuku"),
        SUNILPAULMATHEW("Sunilpaulmathew", "https://gitlab.com/sunilpaulmathew/ashell"),
        KHUN_HTETZ("Khun Htetz Naing", "https://github.com/KhunHtetzNaing/ADB-OTG"),
        MARCIOZOMB("marciozomb13", "https://github.com/marciozomb13"),
        WEIGUANGTWK("weiguangtwk", "https://github.com/WeiguangTWK"),
        WINZORT("WINZORT", "https://github.com/mikropsoft");

        fun getName(): String = contributorsName
        fun getGithubUrl(): String = github

    }

    enum class InfoCards {
        WARNING_USB_DEBUGGING
    }

    companion object {
        const val SHARED_PREFS: String = BuildConfig.APPLICATION_ID + "_preferences"
        const val DEV_EMAIL: String = "hridayanofficial@gmail.com"

        // preferences tags
        const val PREF_FIRST_LAUNCH: String = "first_launch"
        const val PREF_AMOLED_THEME: String = "id_amoled_theme"
        const val PREF_THEME_MODE: String = "id_theme_mode"
        const val PREF_COUNTER_PREFIX: String = "counter_"
        const val PREF_PINNED_PREFIX: String = "pinned"
        const val PREF_CLEAR: String = "id_clear"
        const val PREF_SHARE_AND_RUN: String = "id_share_and_run"
        const val PREF_DISABLE_SOFTKEY: String = "id_disable_softkey"
        const val PREF_DYNAMIC_COLORS: String = "id_dynamic_colors"
        const val PREF_OVERRIDE_BOOKMARKS: String = "id_override_bookmarks"
        const val PREF_SMOOTH_SCROLL: String = "id_smooth_scroll"
        const val PREF_SAVED_VERSION_CODE: String = "saved_version_code"
        const val PREF_SORTING_OPTION: String = "sorting_option"
        const val PREF_SORTING_EXAMPLES: String = "sorting_examples"
        const val PREF_CURRENT_FRAGMENT: String = "current_fragment"
        const val PREF_AUTO_UPDATE_CHECK: String = "id_auto_update_check"
        const val PREF_SAVE_PREFERENCE: String = "id_save_preference"
        const val PREF_LATEST_VERSION_NAME: String = "latest_version_name"
        const val PREF_LAST_SAVED_FILENAME: String = "last_saved_filename"
        const val PREF_HAPTICS_AND_VIBRATION: String = "id_vibration"
        const val PREF_LOCAL_ADB_MODE: String = "id_local_adb_mode"
        const val PREF_ACTIVITY_RECREATED: String = "activity_recreated"
        const val PREF_EXAMPLES_LAYOUT_STYLE: String = "id_examples_layout_style"
        const val PREF_OUTPUT_SAVE_DIRECTORY: String = "output_save_directory"
        const val PREF_UNKNOWN_SOURCE_PERM_ASKED: String = "unknown_source_permission"
        const val PREF_UPDATE_APK_FILE_NAME: String = "update_apk_file_name"
        const val PREF_DEFAULT_SAVE_DIRECTORY: String = "/storage/emulated/0/Download"
        const val ADB_IP: String = "adb_ip"
        const val ADB_PAIRING_PORT: String = "adb_pairing_port"
        const val ADB_CONNECTING_PORT: String = "adb_connecting_port"
        const val ADB_PAIRING_CODE: String = "adb_pairing_code"

        // tags for things like onclick listeners
        const val ID_LOOK_AND_FEEL: String = "id_look_and_feel"
        const val ID_DEF_LANGUAGE: String = "id_default_language"
        const val ID_CONFIG_SAVE_DIR: String = "id_configure_save_directory"
        const val ID_UNHIDE_CARDS: String = "id_unhide_cards"
        const val ID_EXAMPLES: String = "id_examples"
        const val ID_ABOUT: String = "id_about"
        const val ID_VERSION: String = "id_version"
        const val ID_CHANGELOGS: String = "id_changelogs"
        const val ID_REPORT: String = "id_report"
        const val ID_FEATURE: String = "id_feature"
        const val ID_GITHUB: String = "id_github"
        const val ID_TELEGRAM: String = "id_telegram"
        const val ID_DISCORD: String = "id_discord"
        const val ID_LICENSE: String = "id_license"

        /* <--------Transition Names-------> */
        const val SEND_TO_EXAMPLES: String = "sendButtonToExamples"
        const val SETTINGS_TO_SETTINGS: String = "settingsButtonToSettings"
        const val FRAGMENT_LOCAL_SHELL: String = "fragmentLocalShell"
        const val FRAGMENT_OTG_SHELL: String = "fragmentOtgShell"
        const val FRAGMENT_WIFI_ADB_SHELL: String = "fragmentWifiAdbShell"

        /* <--------U R L s -------> */
        const val URL_DEV_GITHUB: String = "https://github.com/DP-Hridayan"
        const val URL_DEV_BM_COFFEE: String = "https://www.buymeacoffee.com/hridayan"

        // url for the build.gradle file of the app
        const val URL_BUILD_GRADLE: String =
            "https://raw.githubusercontent.com/DP-Hridayan/aShellYou/master/app/build.gradle"
        const val URL_OTG_INSTRUCTIONS: String =
            "https://github.com/DP-Hridayan/aShellYou/blob/master/instructions/OTG.md"
        const val URL_WIRELESS_DEBUGGING_INSTRUCTIONS: String =
            "https://github.com/DP-Hridayan/aShellYou/blob/master/instructions/wirelessDebugging.md"
        const val GITHUB_OWNER: String = "dp-hridayan"
        const val GITHUB_REPOSITORY: String = "ashellyou"
        const val URL_GITHUB_REPOSITORY: String = "https://github.com/DP-Hridayan/aShellYou"

        // url for github release
        const val URL_GITHUB_RELEASE: String =
            "https://github.com/DP-Hridayan/aShellYou/releases/latest"
        const val URL_SHIZUKU_SITE: String = "https://shizuku.rikka.app/"
        const val URL_APP_LICENSE: String =
            "https://github.com/DP-Hridayan/aShellYou/blob/master/LICENSE.md"
        const val URL_TELEGRAM: String = "https://t.me/aShellYou"

        // used in OTG utils
        const val TAG: String = "flashbot"

        const val CURRENT_FRAGMENT: String = "current_fragment"

        // integers
        const val SORT_A_TO_Z: Int = 0
        const val SORT_Z_TO_A: Int = 1
        const val SORT_MOST_USED: Int = 2
        const val SORT_OLDEST: Int = 2
        const val SORT_NEWEST: Int = 3
        const val SORT_LEAST_USED: Int = 3
        const val HOME_FRAGMENT: Int = 2025
        const val LOCAL_FRAGMENT: Int = 0
        const val OTG_FRAGMENT: Int = 1
        const val WIFI_ADB_FRAGMENT: Int = 2
        const val MODE_LOCAL_ADB: Int = 0
        const val MODE_OTG: Int = 1
        const val MODE_WIFI_ADB: Int = 2
        const val MODE_REMEMBER_LAST_MODE: Int = 3
        const val MAX_BOOKMARKS_LIMIT: Int = 25
        const val UPDATE_AVAILABLE: Int = 1
        const val UPDATE_NOT_AVAILABLE: Int = 0
        const val CONNECTION_ERROR: Int = 2
        const val LAST_COMMAND_OUTPUT: Int = 0
        const val ALL_OUTPUT: Int = 1
        const val BASIC_MODE: Int = 0
        const val SHIZUKU_MODE: Int = 1
        const val ROOT_MODE: Int = 2
        const val LIST_STYLE: Int = 1
        const val GRID_STYLE: Int = 2

        // used in OTG utils
        const val PUSH_PERCENT: Double = 0.5

        // Set of some sensitive packages of the android which should be handled carefully while executing
        // adb commands
        private val SENSITIVE_PACKAGES: Set<String> = setOf(
            "com.android.systemui",
            "com.android.settings",
            "com.android.frameworkres",
            "com.android.providers.settings",
            "com.android.permissioncontroller",
            "com.android.inputmethod.latin",
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.providers.media",
            "com.android.packageinstaller",
            "com.android.externalstorage",
            "com.android.documentsui",
            "com.android.wifi"
        )

        // Check if packageName contains any of the strings in SENSITIVE_PACKAGES
        @JvmStatic
        fun isPackageSensitive(packageName: String): Boolean {
            return SENSITIVE_PACKAGES.any { packageName.contains(it) }
        }
    }
}
