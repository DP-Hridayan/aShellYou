package `in`.hridayan.ashell.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `in`.hridayan.ashell.commandexamples.presentation.screens.CommandExamplesScreen
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.home.presentation.screens.HomeScreen
import `in`.hridayan.ashell.onboarding.presentation.screens.OnboardingScreen
import `in`.hridayan.ashell.settings.presentation.page.about.screens.AboutScreen
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.screens.AutoUpdateScreen
import `in`.hridayan.ashell.settings.presentation.page.backup.screens.BackupAndRestoreScreen
import `in`.hridayan.ashell.settings.presentation.page.behavior.screens.BehaviorScreen
import `in`.hridayan.ashell.settings.presentation.page.changelog.screens.ChangelogScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.DarkThemeScreen
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens.LookAndFeelScreen
import `in`.hridayan.ashell.settings.presentation.page.mainscreen.screen.SettingsScreen
import kotlinx.serialization.Serializable

@Composable
fun Navigation() {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        val isFirstLaunch = LocalSettings.current.isFirstLaunch
        NavHost(
            navController = navController,
            startDestination = if (isFirstLaunch) OnboardingScreen else HomeScreen
        ) {
            composable<OnboardingScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                OnboardingScreen()
            }

            composable<HomeScreen>(
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() }
            ) {
                HomeScreen()
            }

            composable<SettingsScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                SettingsScreen()
            }

            composable<LookAndFeelScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                LookAndFeelScreen()
            }

            composable<DarkThemeScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                DarkThemeScreen()
            }

            composable<BehaviorScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                BehaviorScreen()
            }

            composable<AboutScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                AboutScreen()
            }

            composable<CommandExamplesScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                CommandExamplesScreen()
            }


            composable<ChangelogScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                ChangelogScreen()
            }


            composable<AutoUpdateScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                AutoUpdateScreen()
            }

            composable<BackupAndRestoreScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                BackupAndRestoreScreen()
            }
        }
    }
}

@Serializable
object SettingsScreen

@Serializable
object LookAndFeelScreen

@Serializable
object DarkThemeScreen

@Serializable
object AboutScreen

@Serializable
object AutoUpdateScreen

@Serializable
object ChangelogScreen

@Serializable
object BehaviorScreen

@Serializable
object HomeScreen

@Serializable
object CommandExamplesScreen

@Serializable
object BackupAndRestoreScreen

@Serializable
object OnboardingScreen
