package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.rounded.TextFields
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.lookAndFeelGraph(
    navController: NavController,
    supportsDynamicColor: Boolean,
    dynamicColorEnabled: Boolean,
    customSchemeApplied: Boolean,
) {
    screen(
        id = SettingsGraphId.LOOK_AND_FEEL,
        title = R.string.look_and_feel,
        navigate = {
            navController.navigate(NavRoutes.LookAndFeelScreen) {
                launchSingleTop = true
            }
        },
    ) {
        entry(key = SettingsKeys.DynamicColors) {
            title(R.string.dynamic_colors)
            description(R.string.des_dynamic_colors)
            icon(R.drawable.ic_dynamic_color)
            availableWhen(supportsDynamicColor)
        }

        entry(key = SettingsKeys.PaletteStyle) {
            title(R.string.palette_style)
            description(R.string.des_palette_style)
            icon(R.drawable.ic_styles)
            availableWhen(!(dynamicColorEnabled || customSchemeApplied))
        }

        entry(key = SettingsKeys.DarkTheme) {
            title(R.string.dark_theme)
            description(R.string.des_dark_theme)
            icon(Icons.Outlined.DarkMode)
            availableWhen(!customSchemeApplied || dynamicColorEnabled)
        }

        entry(key = SettingsKeys.FontFamily) {
            title(R.string.font_family)
            description(R.string.des_font_family)
            icon(Icons.Rounded.TextFields)
        }

        entry(key = SettingsKeys.AutoScaleUi) {
            title(R.string.auto_scale_ui)
            description(R.string.des_auto_scale_ui)
            icon(R.drawable.ic_transform)
        }

        entry(key = SettingsKeys.CustomUiScale) {
            title(R.string.custom_ui_scale)
            description(R.string.des_ui_scale)
            icon(R.drawable.ic_high_density)
        }

        entry(key = SettingsKeys.HapticsAndVibration) {
            title(R.string.haptics_and_vibration)
            description(R.string.des_haptics_and_vibration)
            icon(R.drawable.ic_vibration)
        }

        entry(key = SettingsKeys.Language) {
            title(R.string.default_language)
            description(R.string.des_default_language)
            icon(R.drawable.ic_language)
        }

        darkThemeGraph(navController)
    }
}