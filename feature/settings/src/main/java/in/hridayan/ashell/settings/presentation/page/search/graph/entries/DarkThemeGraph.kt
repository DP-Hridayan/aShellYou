package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.darkThemeGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.DARK_THEME,
        title = R.string.dark_theme,
        navigate = { navController.navigate(NavRoutes.DarkThemeScreen) { launchSingleTop = true } },
    ) {
        entry(key = SettingsKeys.ThemeMode) {
            title(R.string.theme_mode)
            description(R.string.des_dark_theme)
            keywords(R.string.system, R.string.on, R.string.off)
        }

        entry(key = SettingsKeys.AutoDarkModeOnBatterySaver) {
            title(R.string.auto_dark_mode)
            description(R.string.des_auto_dark_mode)
            icon(R.drawable.ic_night_sight_auto)
        }

        entry(key = SettingsKeys.HighContrastDarkMode) {
            title(R.string.high_contrast_dark_mode)
            description(R.string.des_high_contrast_dark_mode)
            icon(R.drawable.ic_amoled_theme)
        }
    }
}