package `in`.hridayan.ashell.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface NavRoutes : NavKey {
    @Serializable
    data object AboutScreen : NavRoutes, NavKey

    @Serializable
    data object AutoUpdateScreen : NavRoutes, NavKey

    @Serializable
    data object BackupAndRestoreScreen : NavRoutes, NavKey

    @Serializable
    data object BehaviorScreen : NavRoutes, NavKey

    @Serializable
    data object ChangelogScreen : NavRoutes, NavKey

    @Serializable
    data object CommandExamplesScreen : NavRoutes, NavKey

    @Serializable
    data object CrashHistoryScreen : NavRoutes, NavKey

    @Serializable
    data object CrashDetailsScreen : NavRoutes, NavKey

    @Serializable
    data object DarkThemeScreen : NavRoutes, NavKey

    @Serializable
    data object HomeScreen : NavRoutes, NavKey

    @Serializable
    data object LocalAdbScreen : NavRoutes, NavKey

    @Serializable
    data object LookAndFeelScreen : NavRoutes, NavKey

    @Serializable
    data object OnboardingScreen : NavRoutes, NavKey

    @Serializable
    data object OtgAdbScreen : NavRoutes, NavKey

    @Serializable
    data object PairingOtherDeviceScreen : NavRoutes, NavKey

    @Serializable
    data object PairingOwnDeviceScreen : NavRoutes, NavKey

    @Serializable
    data object SettingsScreen : NavRoutes, NavKey

    @Serializable
    data class WifiAdbScreen(val deviceName: String? = null) : NavRoutes, NavKey

    @Serializable
    data class FileBrowserScreen(
        val deviceAddress: String,
        val isOwnDevice: Boolean = false
    ) : NavRoutes, NavKey
}
