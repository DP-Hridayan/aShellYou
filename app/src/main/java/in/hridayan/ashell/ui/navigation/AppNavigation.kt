package `in`.hridayan.ashell.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import `in`.hridayan.ashell.adbsideload.presentation.screens.AdbSideloadScreen
import `in`.hridayan.ashell.ai.presentation.screens.AiChatScreen
import `in`.hridayan.ashell.ai.presentation.screens.AiModelsScreen
import `in`.hridayan.ashell.ai.presentation.screens.CloudModelsScreen
import `in`.hridayan.ashell.commandexamples.presentation.screens.CommandExamplesScreen
import `in`.hridayan.ashell.core.common.LocalAnimatedContentScope
import `in`.hridayan.ashell.core.common.domain.model.SharedTextHolder
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.predictiveEnter
import `in`.hridayan.ashell.core.navigation.predictiveExit
import `in`.hridayan.ashell.core.navigation.slideFadeInFromLeft
import `in`.hridayan.ashell.core.navigation.slideFadeInFromRight
import `in`.hridayan.ashell.core.navigation.slideFadeOutToLeft
import `in`.hridayan.ashell.core.navigation.slideFadeOutToRight
import `in`.hridayan.ashell.crashreporter.presentation.screens.CrashDetailsScreen
import `in`.hridayan.ashell.crashreporter.presentation.screens.CrashHistoryScreen
import `in`.hridayan.ashell.logcat.presentation.screens.LogcatScreen
import `in`.hridayan.ashell.onboarding.presentation.screens.OnboardingScreen
import `in`.hridayan.ashell.qstiles.presentation.screen.CreateTileScreen
import `in`.hridayan.ashell.qstiles.presentation.screen.TileDashBoardScreen
import `in`.hridayan.ashell.settings.presentation.page.about.screens.AboutScreen
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.screens.AutoUpdateScreen
import `in`.hridayan.ashell.settings.presentation.page.backup.screens.BackupAndRestoreScreen
import `in`.hridayan.ashell.settings.presentation.page.backup.screens.BackupSchedulerScreen
import `in`.hridayan.ashell.settings.presentation.page.behavior.screens.BehaviorScreen
import `in`.hridayan.ashell.settings.presentation.page.changelog.screens.ChangelogScreen
import `in`.hridayan.ashell.settings.presentation.page.contributors.screens.ContributorsScreen
import `in`.hridayan.ashell.settings.presentation.page.contributors.screens.TranslatorsScreen
import `in`.hridayan.ashell.settings.presentation.page.languages.screens.LanguagesScreen
import `in`.hridayan.ashell.settings.presentation.page.licenses.screens.LicensesScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.DarkThemeScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.EditColorSchemeScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.GenerateColorSchemeScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.LookAndFeelScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.UiScaleScreen
import `in`.hridayan.ashell.settings.presentation.page.mainscreen.screen.SettingsScreen
import `in`.hridayan.ashell.settings.presentation.page.privacypolicy.screens.PrivacyPolicyScreen
import `in`.hridayan.ashell.settings.presentation.page.search.screens.SettingsSearchScreen
import `in`.hridayan.ashell.shell.fastboot.presentation.screens.FastbootScreen
import `in`.hridayan.ashell.shell.file_browser.presentation.screens.FileBrowserScreen
import `in`.hridayan.ashell.shell.local_adb_shell.presentation.screens.LocalAdbScreen
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.screens.OtgAdbScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.PairingOtherDeviceScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.PairingOwnDeviceScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.WifiAdbScreen
import `in`.hridayan.ashell.ui.home.HomeRoute
import kotlinx.serialization.serializer
import kotlin.reflect.KType

@Composable
fun AppNavigation(
    isFirstLaunch: Boolean = false,
    defaultLaunchIsLocalAdb: Boolean = false,
    deepLinkViewModel: NavDeepLinkViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalNavController provides navController,
    ) {

        LaunchedEffect(navController) {
            deepLinkViewModel.sessionHolder.navigationEvents.collect {
                navController.navigate(NavRoutes.LogcatScreen) {
                    launchSingleTop = true
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = if (isFirstLaunch) {
                NavRoutes.OnboardingScreen
            } else if (defaultLaunchIsLocalAdb || SharedTextHolder.text != null) {
                NavRoutes.LocalAdbScreen
            } else {
                NavRoutes.HomeScreen
            },
            enterTransition = { slideFadeInFromRight() },
            exitTransition = { slideFadeOutToLeft() },
            popEnterTransition = { slideFadeInFromLeft() },
            popExitTransition = { slideFadeOutToRight() },
            predictivePopEnterTransition = { predictiveEnter() },
            predictivePopExitTransition = { predictiveExit() }
        ) {
            composable<NavRoutes.OnboardingScreen> {
                OnboardingScreen()
            }

            composable<NavRoutes.HomeScreen>(
                enterTransition = {
                    if (initialState.destination.route?.contains(serializer<NavRoutes.LocalAdbScreen>().descriptor.serialName) == true) {
                        slideFadeInFromLeft()
                    } else {
                        slideFadeInFromRight()
                    }
                },
                popEnterTransition = { slideFadeInFromLeft() }
            ) {
                HomeRoute()
            }

            composable<NavRoutes.SettingsScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.SettingsScreen>()
                SettingsScreen(highlightKey = route.highlightKey)
            }

            composable<NavRoutes.LookAndFeelScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.LookAndFeelScreen>()
                LookAndFeelScreen(highlightKey = route.highlightKey)
            }

            composable<NavRoutes.DarkThemeScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.DarkThemeScreen>()
                DarkThemeScreen(highlightKey = route.highlightKey)
            }

            composable<NavRoutes.UiScaleScreen> {
                UiScaleScreen()
            }

            composable<NavRoutes.BehaviorScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.BehaviorScreen>()
                BehaviorScreen(highlightKey = route.highlightKey)
            }

            composable<NavRoutes.AboutScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.AboutScreen>()
                AboutScreen(highlightKey = route.highlightKey)
            }

            composable<NavRoutes.CommandExamplesScreen> {
                CommandExamplesScreen()
            }

            composable<NavRoutes.TranslatorsScreen> {
                TranslatorsScreen()
            }

            composable<NavRoutes.ContributorsScreen> {
                ContributorsScreen()
            }

            composable<NavRoutes.ChangelogScreen> {
                ChangelogScreen()
            }

            composable<NavRoutes.SettingsSearchScreen> {
                SettingsSearchScreen()
            }

            composable<NavRoutes.LanguagesScreen> {
                LanguagesScreen()
            }

            composable<NavRoutes.LicensesScreen> {
                LicensesScreen()
            }

            composable<NavRoutes.PrivacyPolicyScreen> {
                PrivacyPolicyScreen()
            }

            animatedComposable<NavRoutes.CrashHistoryScreen> {
                CrashHistoryScreen()
            }

            animatedComposable<NavRoutes.CrashDetailsScreen> {
                CrashDetailsScreen()
            }

            composable<NavRoutes.AutoUpdateScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.AutoUpdateScreen>()
                AutoUpdateScreen(highlightKey = route.highlightKey)
            }

            composable<NavRoutes.BackupAndRestoreScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.BackupAndRestoreScreen>()
                BackupAndRestoreScreen(highlightKey = route.highlightKey)
            }

            composable<NavRoutes.BackupSchedulerScreen> {
                BackupSchedulerScreen()
            }

            composable<NavRoutes.LocalAdbScreen>(
                exitTransition = {
                    if (targetState.destination.route?.contains(serializer<NavRoutes.HomeScreen>().descriptor.serialName) == true) {
                        slideFadeOutToRight()
                    } else {
                        slideFadeOutToLeft()
                    }
                },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                val isRoot = navController.previousBackStackEntry == null

                BackHandler(enabled = isRoot) {
                    navController.navigate(NavRoutes.HomeScreen) {
                        popUpTo(NavRoutes.LocalAdbScreen) { inclusive = true }
                    }
                }

                LocalAdbScreen()
            }

            composable<NavRoutes.OtgAdbScreen> {
                OtgAdbScreen()
            }

            composable<NavRoutes.FastbootScreen> {
                FastbootScreen()
            }

            composable<NavRoutes.AdbSideloadScreen> {
                AdbSideloadScreen()
            }

            composable<NavRoutes.PairingOwnDeviceScreen> {
                PairingOwnDeviceScreen()
            }

            composable<NavRoutes.PairingOtherDeviceScreen> {
                PairingOtherDeviceScreen()
            }

            composable<NavRoutes.WifiAdbScreen> {
                WifiAdbScreen()
            }

            composable<NavRoutes.FileBrowserScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.FileBrowserScreen>()
                FileBrowserScreen(
                    deviceAddress = route.deviceAddress,
                    connectionMode = route.connectionMode,
                    isOwnDevice = route.isOwnDevice
                )
            }

            composable<NavRoutes.TileDashboardScreen> {
                TileDashBoardScreen()
            }

            composable<NavRoutes.CreateTileScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.CreateTileScreen>()
                CreateTileScreen(tileId = route.tileId)
            }

            composable<NavRoutes.LogcatScreen> {
                LogcatScreen(navController = navController)
            }

            composable<NavRoutes.AiModelsScreen> {
                AiModelsScreen()
            }
            composable<NavRoutes.CloudModelsScreen> {
                CloudModelsScreen()
            }

            composable<NavRoutes.GenerateColorSchemeScreen> {
                GenerateColorSchemeScreen()
            }

            composable<NavRoutes.EditColorSchemeScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.EditColorSchemeScreen>()
                EditColorSchemeScreen(themeId = route.themeId)
            }

            composable<NavRoutes.AiChatScreen> {
                AiChatScreen()
            }

        }
    }
}

inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline enterTransition: (
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?
    )? = null,
    noinline exitTransition: (
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?
    )? = null,
    noinline popEnterTransition: (
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?
    )? = enterTransition,
    noinline popExitTransition: (
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?
    )? = exitTransition,
    noinline sizeTransform: (
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards SizeTransform?
    )? = null,
    noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform
    ) { backStackEntry ->
        val animatedContentScope = this

        CompositionLocalProvider(
            LocalAnimatedContentScope provides animatedContentScope
        ) {
            content(backStackEntry)
        }
    }
}