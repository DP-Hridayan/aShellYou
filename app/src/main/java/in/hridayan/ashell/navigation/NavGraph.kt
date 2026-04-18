@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.hridayan.ashell.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import `in`.hridayan.ashell.commandexamples.presentation.screens.CommandExamplesScreen
import `in`.hridayan.ashell.core.common.LocalAnimatedContentScope
import `in`.hridayan.ashell.core.domain.model.SharedTextHolder
import `in`.hridayan.ashell.home.presentation.screens.HomeScreen
import `in`.hridayan.ashell.onboarding.presentation.screens.OnboardingScreen
import `in`.hridayan.ashell.qstiles.presentation.screen.CreateTileScreen
import `in`.hridayan.ashell.qstiles.presentation.screen.TileDashBoardScreen
import `in`.hridayan.ashell.qstiles.presentation.screen.TileLogsScreen
import `in`.hridayan.ashell.settings.presentation.page.about.screens.AboutScreen
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.screens.AutoUpdateScreen
import `in`.hridayan.ashell.settings.presentation.page.backup.screens.BackupAndRestoreScreen
import `in`.hridayan.ashell.settings.presentation.page.backup.screens.BackupSchedulerScreen
import `in`.hridayan.ashell.settings.presentation.page.behavior.screens.BehaviorScreen
import `in`.hridayan.ashell.settings.presentation.page.changelog.screens.ChangelogScreen
import `in`.hridayan.ashell.settings.presentation.page.crashhistory.screens.CrashDetailsScreen
import `in`.hridayan.ashell.settings.presentation.page.crashhistory.screens.CrashHistoryScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.DarkThemeScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.LookAndFeelScreen
import `in`.hridayan.ashell.settings.presentation.page.mainscreen.screen.SettingsScreen
import `in`.hridayan.ashell.shell.file_browser.presentation.screens.FileBrowserScreen
import `in`.hridayan.ashell.shell.local_adb_shell.presentation.screens.LocalAdbScreen
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.screens.OtgAdbScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.PairingOtherDeviceScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.PairingOwnDeviceScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.WifiAdbScreen
import kotlin.reflect.KType

@Composable
fun Navigation(isFirstLaunch: Boolean = false) {
    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalNavController provides navController,
    ) {
        LaunchedEffect(Unit) {
            SharedTextHolder.text?.let {
                navController.navigate(NavRoutes.LocalAdbScreen)
            }
        }

        NavHost(
            navController = navController,
            startDestination = if (isFirstLaunch) NavRoutes.OnboardingScreen else NavRoutes.HomeScreen,
            enterTransition = { slideFadeInFromRight() },
            exitTransition = { slideFadeOutToLeft() },
            popEnterTransition = { slideFadeInFromLeft() },
            popExitTransition = { slideFadeOutToRight() }
        ) {
            composable<NavRoutes.OnboardingScreen> {
                OnboardingScreen()
            }

            composable<NavRoutes.HomeScreen> {
                HomeScreen()
            }

            composable<NavRoutes.SettingsScreen> {
                SettingsScreen()
            }

            composable<NavRoutes.LookAndFeelScreen> {
                LookAndFeelScreen()
            }

            composable<NavRoutes.DarkThemeScreen> {
                DarkThemeScreen()
            }

            composable<NavRoutes.BehaviorScreen> {
                BehaviorScreen()
            }

            composable<NavRoutes.AboutScreen> {
                AboutScreen()
            }

            composable<NavRoutes.CommandExamplesScreen> {
                CommandExamplesScreen()
            }


            composable<NavRoutes.ChangelogScreen> {
                ChangelogScreen()
            }

            animatedComposable<NavRoutes.CrashHistoryScreen> {
                CrashHistoryScreen()
            }

            animatedComposable<NavRoutes.CrashDetailsScreen> {
                CrashDetailsScreen()
            }

            composable<NavRoutes.AutoUpdateScreen> {
                AutoUpdateScreen()
            }

            composable<NavRoutes.BackupAndRestoreScreen> {
                BackupAndRestoreScreen()
            }

            composable<NavRoutes.BackupSchedulerScreen> {
                BackupSchedulerScreen()
            }

            composable<NavRoutes.LocalAdbScreen> {
                LocalAdbScreen()
            }

            composable<NavRoutes.OtgAdbScreen> {
                OtgAdbScreen()
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

            composable<NavRoutes.TileLogsScreen> {
                TileLogsScreen()
            }

            composable<NavRoutes.CreateTileScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.CreateTileScreen>()
                CreateTileScreen(tileId = route.tileId)
            }

        }
    }
}

inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?)? = null,
    noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?)? = null,
    noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?)? = enterTransition,
    noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?)? = exitTransition,
    noinline sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards SizeTransform?)? = null,
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