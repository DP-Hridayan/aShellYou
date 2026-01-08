package `in`.hridayan.ashell.navigation

import kotlinx.serialization.Serializable

sealed class NavRoutes {
    @Serializable
    object AboutScreen : NavRoutes()

    @Serializable
    object AutoUpdateScreen : NavRoutes()

    @Serializable
    object BackupAndRestoreScreen : NavRoutes()

    @Serializable
    object BehaviorScreen : NavRoutes()

    @Serializable
    object ChangelogScreen : NavRoutes()

    @Serializable
    object CommandExamplesScreen : NavRoutes()

    @Serializable
    object CrashHistoryScreen : NavRoutes()

    @Serializable
    object CrashDetailsScreen : NavRoutes()

    @Serializable
    object DarkThemeScreen : NavRoutes()

    @Serializable
    object HomeScreen : NavRoutes()

    @Serializable
    object LocalAdbScreen : NavRoutes()

    @Serializable
    object LookAndFeelScreen : NavRoutes()

    @Serializable
    object OnboardingScreen : NavRoutes()

    @Serializable
    object OtgAdbScreen : NavRoutes()

    @Serializable
    object PairingOtherDeviceScreen : NavRoutes()

    @Serializable
    object PairingOwnDeviceScreen : NavRoutes()

    @Serializable
    object SettingsScreen : NavRoutes()

    @Serializable
    data class WifiAdbScreen(val deviceName: String? = null) : NavRoutes()

    @Serializable
    object FileBrowserScreen : NavRoutes()

}
