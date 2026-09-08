package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.behaviorGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.BEHAVIOR,
        title = R.string.behavior,
        navigate = { navController.navigate(NavRoutes.BehaviorScreen) { launchSingleTop = true } },
    ) {
        entry(key = SettingsKeys.LocalAdbWorkingMode) {
            title(R.string.local_adb_shell)
            keywords(R.string.basic_shell, R.string.shizuku, R.string.root, R.string.tcpip_mode)
        }

        entry(key = SettingsKeys.DefaultLaunchIsLocalAdb) {
            title(R.string.set_local_adb_as_default_launch)
            description(R.string.des_set_local_adb_as_default_launch)
            icon(R.drawable.ic_rocket_launch)
        }

        entry(key = SettingsKeys.SmoothScrolling) {
            title(R.string.smooth_scrolling)
            description(R.string.des_smooth_scroll)
        }

        entry(key = SettingsKeys.ClearOutputConfirmation) {
            title(R.string.clear_output_confirmation)
            description(R.string.des_clear_output_confirmation)
            icon(R.drawable.ic_clear)
        }

        entry(key = SettingsKeys.DisableSoftKeyboard) {
            title(R.string.disable_softkey)
            description(R.string.des_disable_softkey)
            icon(R.drawable.ic_disable_keyboard)
        }

        entry(key = SettingsKeys.TerminalFontStyle) {
            title(R.string.terminal_font_style)
            keywords(R.string.monospace, R.string.system_font)
        }

        entry(key = SettingsKeys.OutputSaveDirectory) {
            title(R.string.configure_save_directory)
            description(R.string.des_configure_save_directory)
            icon(R.drawable.ic_directory)
        }

        entry(key = SettingsKeys.SaveWholeOutput) {
            title(R.string.save_whole_output)
            description(R.string.des_save_whole_output)
            icon(R.drawable.ic_save_as)
        }
    }
}