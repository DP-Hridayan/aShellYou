package `in`.hridayan.ashell.settings.domain.model

import `in`.hridayan.ashell.navigation.NavRoutes

/**
 * Identifies which settings sub-screen a [SearchableSettingsEntry] belongs to.
 * Used to construct the correct [NavRoutes] with a `highlightKey` argument
 * when the user taps a search result.
 */
enum class SettingsScreenId {
    SETTINGS_MAIN,
    LOOK_AND_FEEL,
    DARK_THEME,
    BEHAVIOR,
    AUTO_UPDATE,
    ABOUT,
    BACKUP_AND_RESTORE;

    /** Creates the NavRoute for this screen, optionally highlighting a specific item. */
    fun toNavRoute(highlightKey: String? = null): NavRoutes = when (this) {
        SETTINGS_MAIN -> NavRoutes.SettingsScreen
        LOOK_AND_FEEL -> NavRoutes.LookAndFeelScreen(highlightKey)
        DARK_THEME -> NavRoutes.DarkThemeScreen(highlightKey)
        BEHAVIOR -> NavRoutes.BehaviorScreen(highlightKey)
        AUTO_UPDATE -> NavRoutes.AutoUpdateScreen(highlightKey)
        ABOUT -> NavRoutes.AboutScreen(highlightKey)
        BACKUP_AND_RESTORE -> NavRoutes.BackupAndRestoreScreen(highlightKey)
    }
}
