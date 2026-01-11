@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.hridayan.ashell.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import `in`.hridayan.ashell.commandexamples.presentation.screens.CommandExamplesScreen
import `in`.hridayan.ashell.core.common.LocalSharedTransitionScope
import `in`.hridayan.ashell.core.domain.model.SharedTextHolder
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
import `in`.hridayan.ashell.shell.file_browser.presentation.screens.FileBrowserScreen
import `in`.hridayan.ashell.shell.local_adb_shell.presentation.screens.LocalAdbScreen
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.screens.OtgAdbScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.PairingOtherDeviceScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.PairingOwnDeviceScreen
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens.WifiAdbScreen

@Composable
fun Navigation(isFirstLaunch: Boolean = false) {
    val startDestination: NavKey =
        if (isFirstLaunch) NavRoutes.OnboardingScreen else NavRoutes.HomeScreen
    val backStack = rememberNavBackStack(startDestination)
    val sharedTransitionScope = LocalSharedTransitionScope.current

    LaunchedEffect(Unit) {
        SharedTextHolder.text?.let {
            backStack.add(NavRoutes.LocalAdbScreen)
        }
    }

    with(sharedTransitionScope) {
        CompositionLocalProvider(
            LocalBackStack provides backStack
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeAt(backStack.lastIndex) },

                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = slideFadeInFromRight(),
                        initialContentExit = slideFadeOutToLeft()
                    )
                },

                popTransitionSpec = {
                    ContentTransform(
                        targetContentEnter = slideFadeInFromLeft(),
                        initialContentExit = slideFadeOutToRight()
                    )
                },

                predictivePopTransitionSpec = { progress ->
                    val scale = 1f - (progress * 0.15f)
                    val horizontalOffsetPercent = progress * 0.10f

                    ContentTransform(
                        targetContentEnter = fadeIn(tween(0)),
                        initialContentExit = scaleOut(
                            targetScale = scale,
                            transformOrigin = TransformOrigin.Center
                        ) + slideOutHorizontally { (it * horizontalOffsetPercent).toInt() }
                    )
                },

                entryProvider = entryProvider {
                    entry<NavRoutes.OnboardingScreen> {
                        OnboardingScreen()
                    }

                    entry<NavRoutes.HomeScreen> {
                        HomeScreen()
                    }

                    entry<NavRoutes.SettingsScreen> {
                        SettingsScreen()
                    }

                    entry<NavRoutes.LookAndFeelScreen> {
                        LookAndFeelScreen()
                    }

                    entry<NavRoutes.DarkThemeScreen> {
                        DarkThemeScreen()
                    }

                    entry<NavRoutes.BehaviorScreen> {
                        BehaviorScreen()
                    }

                    entry<NavRoutes.AboutScreen> {
                        AboutScreen()
                    }

                    entry<NavRoutes.CommandExamplesScreen> {
                        CommandExamplesScreen()
                    }

                    entry<NavRoutes.ChangelogScreen> {
                        ChangelogScreen()
                    }

                    // Crash screens with shared element transition support
                    entry<NavRoutes.CrashHistoryScreen> {
                        CrashHistoryScreen()
                    }

                    entry<NavRoutes.CrashDetailsScreen> {
                        CrashDetailsScreen()
                    }

                    entry<NavRoutes.AutoUpdateScreen> {
                        AutoUpdateScreen()
                    }

                    entry<NavRoutes.BackupAndRestoreScreen> {
                        BackupAndRestoreScreen()
                    }

                    entry<NavRoutes.LocalAdbScreen> {
                        LocalAdbScreen()
                    }

                    entry<NavRoutes.OtgAdbScreen> {
                        OtgAdbScreen()
                    }

                    entry<NavRoutes.PairingOwnDeviceScreen> {
                        PairingOwnDeviceScreen()
                    }

                    entry<NavRoutes.PairingOtherDeviceScreen> {
                        PairingOtherDeviceScreen()
                    }

                    entry<NavRoutes.WifiAdbScreen> {
                        WifiAdbScreen()
                    }

                    entry<NavRoutes.FileBrowserScreen> { key ->
                        FileBrowserScreen(
                            deviceAddress = key.deviceAddress,
                            isOwnDevice = key.isOwnDevice
                        )
                    }
                }
            )
        }
    }
}