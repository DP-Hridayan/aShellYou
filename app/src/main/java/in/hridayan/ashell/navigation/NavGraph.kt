@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.hridayan.ashell.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `in`.hridayan.ashell.commandexamples.presentation.screens.CommandExamplesScreen
import `in`.hridayan.ashell.core.common.LocalAnimatedContentScope
import `in`.hridayan.ashell.core.common.LocalSharedTransitionScope
import `in`.hridayan.ashell.home.presentation.screens.HomeScreen
import `in`.hridayan.ashell.onboarding.presentation.screens.OnboardingScreen
import `in`.hridayan.ashell.settings.presentation.page.about.screens.AboutScreen
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.screens.AutoUpdateScreen
import `in`.hridayan.ashell.settings.presentation.page.backup.screens.BackupAndRestoreScreen
import `in`.hridayan.ashell.settings.presentation.page.behavior.screens.BehaviorScreen
import `in`.hridayan.ashell.settings.presentation.page.changelog.screens.ChangelogScreen
import `in`.hridayan.ashell.settings.presentation.page.crashhistory.screens.CrashDetailsScreen
import `in`.hridayan.ashell.settings.presentation.page.crashhistory.screens.CrashHistoryScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.DarkThemeScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.LookAndFeelScreen
import `in`.hridayan.ashell.settings.presentation.page.mainscreen.screen.SettingsScreen
import `in`.hridayan.ashell.shell.local_adb_shell.presentation.screens.LocalAdbScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.presentation.screens.PairingOtherDeviceScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.presentation.screens.PairingOwnDeviceScreen

@Composable
fun Navigation(isFirstLaunch: Boolean = false) {
    val navController = rememberNavController()
    val sharedTransitionScope = LocalSharedTransitionScope.current

    with(sharedTransitionScope) {
        CompositionLocalProvider(
            LocalNavController provides navController,
        ) {
            NavHost(
                navController = navController,
                startDestination = if (isFirstLaunch) NavRoutes.OnboardingScreen else NavRoutes.HomeScreen
            ) {
                composable<NavRoutes.OnboardingScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    OnboardingScreen()
                }

                composable<NavRoutes.HomeScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() }
                ) {
                    HomeScreen()
                }

                composable<NavRoutes.SettingsScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    SettingsScreen()
                }

                composable<NavRoutes.LookAndFeelScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    LookAndFeelScreen()
                }

                composable<NavRoutes.DarkThemeScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    DarkThemeScreen()
                }

                composable<NavRoutes.BehaviorScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    BehaviorScreen()
                }

                composable<NavRoutes.AboutScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    AboutScreen()
                }

                composable<NavRoutes.CommandExamplesScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    CommandExamplesScreen()
                }


                composable<NavRoutes.ChangelogScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    ChangelogScreen()
                }

                composable<NavRoutes.CrashHistoryScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    val animatedScope = this
                    CompositionLocalProvider(
                        LocalAnimatedContentScope provides animatedScope
                    ) {
                        CrashHistoryScreen()
                    }
                }

                composable<NavRoutes.CrashDetailsScreen>(
                ) {
                    val animatedScope = this
                    CompositionLocalProvider(
                        LocalAnimatedContentScope provides animatedScope
                    ) {
                        CrashDetailsScreen()
                    }
                }

                composable<NavRoutes.AutoUpdateScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    AutoUpdateScreen()
                }

                composable<NavRoutes.BackupAndRestoreScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    BackupAndRestoreScreen()
                }

                composable<NavRoutes.LocalAdbScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    LocalAdbScreen()
                }

                composable<NavRoutes.PairingOwnDeviceScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    PairingOwnDeviceScreen()
                }

                composable<NavRoutes.PairingOtherDeviceScreen>(
                    enterTransition = { slideFadeInFromRight() },
                    exitTransition = { slideFadeOutToLeft() },
                    popEnterTransition = { slideFadeInFromLeft() },
                    popExitTransition = { slideFadeOutToRight() }
                ) {
                    PairingOtherDeviceScreen()
                }
            }
        }
    }
}