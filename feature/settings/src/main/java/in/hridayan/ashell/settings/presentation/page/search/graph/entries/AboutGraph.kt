package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.aboutGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.ABOUT,
        title = R.string.about,
        navigate = { navController.navigate(NavRoutes.AboutScreen) { launchSingleTop = true } },
    ) {
        entry(key = SettingsKeys.Contributors) {
            title(R.string.contributors)
            description(R.string.des_contributors)
            icon(R.drawable.ic_crowdsource)
        }

        entry(key = SettingsKeys.Translators) {
            title(R.string.translators)
            description(R.string.des_translators)
            icon(R.drawable.ic_translate)
        }

        entry(key = SettingsKeys.Changelogs) {
            title(R.string.changelogs)
            description(R.string.des_changelogs)
            icon(R.drawable.ic_changelog)
        }

        entry(key = SettingsKeys.Report) {
            title(R.string.report_issue)
            description(R.string.des_report_issue)
            icon(R.drawable.ic_report)
        }

        entry(key = SettingsKeys.FeatureRequest) {
            title(R.string.feature_request)
            description(R.string.des_feature_request)
            icon(R.drawable.ic_add_comment)
        }

        entry(key = SettingsKeys.CrashHistory) {
            title(R.string.crash_history)
            description(R.string.des_crash_history)
            icon(R.drawable.ic_bug)
        }

        entry(key = SettingsKeys.Licenses) {
            title(R.string.libraries_and_licenses)
            description(R.string.des_libraries_and_licenses)
            icon(R.drawable.ic_license)
        }

        entry(key = SettingsKeys.PrivacyPolicy) {
            title(R.string.privacy_policy)
            description(R.string.des_privacy_policy)
            icon(R.drawable.ic_privacy_tip)
        }
    }
}