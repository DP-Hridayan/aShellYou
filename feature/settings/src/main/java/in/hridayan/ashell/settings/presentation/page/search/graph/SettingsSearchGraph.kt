package `in`.hridayan.ashell.settings.presentation.page.search.graph

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.FeatureConfig
import `in`.hridayan.ashell.core.common.domain.model.AuthenticationTimeout
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.ashell.settings.presentation.page.search.graph.entries.aboutGraph
import `in`.hridayan.ashell.settings.presentation.page.search.graph.entries.aiModelsGraph
import `in`.hridayan.ashell.settings.presentation.page.search.graph.entries.autoUpdateGraph
import `in`.hridayan.ashell.settings.presentation.page.search.graph.entries.backupAndRestoreGraph
import `in`.hridayan.ashell.settings.presentation.page.search.graph.entries.behaviorGraph
import `in`.hridayan.ashell.settings.presentation.page.search.graph.entries.lookAndFeelGraph
import `in`.hridayan.ashell.settings.presentation.page.search.graph.entries.privacyAndSecurityGraph
import `in`.hridayan.ashell.settings.presentation.page.search.graph.entries.rootGraph
import `in`.hridayan.settingsgraph.search.SearchGraph
import `in`.hridayan.settingsgraph.search.searchGraph

/**
 * Builds the settings search index.
 *
 * Availability gates are read here, in composition, and passed to [remember] as keys, so the graph
 * is rebuilt whenever one changes. A setting that is currently unreachable is therefore absent
 * from the index rather than filtered out of results later.
 *
 * @param navController Captured by each screen's navigation lambda.
 */
@Composable
fun rememberSettingsSearchGraph(navController: NavController): SearchGraph {
    val settings = LocalSettings.current
    val aiEnabled = FeatureConfig.isAiEnabled
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val dynamicColorEnabled = settings[SettingsKeys.DynamicColors]
    val customSchemeApplied = settings[SettingsKeys.UserGeneratedColorSchemeApplied]
    val aiCacheEnabled = settings[SettingsKeys.AiCacheEnabled]
    val requireAuth = settings[SettingsKeys.RequireAuthentication]
    val currentTimeout = settings[SettingsKeys.AuthenticationTimeout]

    val timeoutText = getTimeoutText(currentTimeout)

    return remember(
        navController,
        aiEnabled,
        supportsDynamicColor,
        dynamicColorEnabled,
        customSchemeApplied,
        aiCacheEnabled,
        requireAuth,
        currentTimeout
    ) {
        searchGraph {
            screen(
                id = SettingsGraphId.SETTINGS,
                title = R.string.settings,
                navigate = {
                    navController.navigate(NavRoutes.SettingsScreen) {
                        launchSingleTop = true
                    }
                },
            ) {
                rootGraph(aiEnabled = aiEnabled)

                lookAndFeelGraph(
                    navController = navController,
                    supportsDynamicColor = supportsDynamicColor,
                    dynamicColorEnabled = dynamicColorEnabled,
                    customSchemeApplied = customSchemeApplied,
                )

                behaviorGraph(navController = navController)

                aiModelsGraph(
                    navController = navController,
                    aiEnabled = aiEnabled,
                    aiCacheEnabled = aiCacheEnabled
                )

                autoUpdateGraph(navController = navController)

                privacyAndSecurityGraph(
                    navController = navController,
                    requireAuth = requireAuth,
                    timeoutText = timeoutText
                )

                backupAndRestoreGraph(navController = navController)

                aboutGraph(navController = navController)
            }
        }
    }
}

@Composable
private fun getTimeoutText(currentTimeout: Int): String {
    return when (currentTimeout) {
        AuthenticationTimeout.NEVER -> stringResource(R.string.timeout_never)
        AuthenticationTimeout.IMMEDIATE -> stringResource(R.string.timeout_immediate)
        AuthenticationTimeout.MIN_1 -> pluralStringResource(R.plurals.timeout_minutes, 1, 1)
        AuthenticationTimeout.MIN_5 -> pluralStringResource(R.plurals.timeout_minutes, 5, 5)
        AuthenticationTimeout.MIN_10 -> pluralStringResource(R.plurals.timeout_minutes, 10, 10)
        else -> stringResource(R.string.timeout_immediate)
    }
}