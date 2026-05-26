package `in`.hridayan.ashell.navigation

import `in`.hridayan.ashell.shell.file_browser.domain.model.ConnectionMode
import kotlinx.serialization.Serializable

sealed class

NavRoutes {
    @Serializable
    data class AboutScreen(val highlightKey: String? = null) : NavRoutes()

    @Serializable
    data class AutoUpdateScreen(val highlightKey: String? = null) : NavRoutes()

    @Serializable
    data class BackupAndRestoreScreen(val highlightKey: String? = null) : NavRoutes()

    @Serializable
    object BackupSchedulerScreen : NavRoutes()

    @Serializable
    data class BehaviorScreen(val highlightKey: String? = null) : NavRoutes()

    @Serializable
    object TranslatorsScreen : NavRoutes()

    @Serializable
    object ContributorsScreen : NavRoutes()

    @Serializable
    object ChangelogScreen : NavRoutes()

    @Serializable
    object SettingsSearchScreen : NavRoutes()

    @Serializable
    object LicensesScreen : NavRoutes()

    @Serializable
    object LanguagesScreen : NavRoutes()

    @Serializable
    object CommandExamplesScreen : NavRoutes()

    @Serializable
    object CrashHistoryScreen : NavRoutes()

    @Serializable
    object CrashDetailsScreen : NavRoutes()

    @Serializable
    data class DarkThemeScreen(val highlightKey: String? = null) : NavRoutes()

    @Serializable
    object UiScaleScreen : NavRoutes()

    @Serializable
    object HomeScreen : NavRoutes()

    @Serializable
    object LocalAdbScreen : NavRoutes()

    @Serializable
    data class LookAndFeelScreen(val highlightKey: String? = null) : NavRoutes()

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
    data class FileBrowserScreen(
        val deviceAddress: String,
        val connectionMode: ConnectionMode = ConnectionMode.WIFI_ADB,
        val isOwnDevice: Boolean = false
    ) : NavRoutes()

    @Serializable
    object TileDashboardScreen : NavRoutes()

    @Serializable
    data class CreateTileScreen(val tileId: Int) : NavRoutes()

    @Serializable
    data class AiModelManagerScreen(val highlightKey: String? = null) : NavRoutes()

    @Serializable
    object ModelsScreen : NavRoutes()
}
