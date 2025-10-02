package `in`.hridayan.ashell.navigation

import kotlinx.serialization.Serializable

sealed class NavRoutes {

    @Serializable object SettingsScreen : NavRoutes()
    @Serializable object LookAndFeelScreen : NavRoutes()
    @Serializable object DarkThemeScreen : NavRoutes()
    @Serializable object AboutScreen : NavRoutes()
    @Serializable object AutoUpdateScreen : NavRoutes()
    @Serializable object ChangelogScreen : NavRoutes()
    @Serializable object BehaviorScreen : NavRoutes()
    @Serializable object HomeScreen : NavRoutes()
    @Serializable object CommandExamplesScreen : NavRoutes()
    @Serializable object BackupAndRestoreScreen : NavRoutes()
    @Serializable object OnboardingScreen : NavRoutes()
    @Serializable object LocalAdbScreen : NavRoutes()
    @Serializable object WifiAdbPairingScreen : NavRoutes()
}