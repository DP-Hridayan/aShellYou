package `in`.hridayan.ashell.core.navigation

import `in`.hridayan.ashell.core.common.domain.model.AdbFileBrowserConnectionMode
import kotlinx.serialization.Serializable

sealed class

NavRoutes {
    @Serializable
    object AboutScreen : NavRoutes()

    @Serializable
    object AutoUpdateScreen : NavRoutes()

    @Serializable
    object BackupAndRestoreScreen : NavRoutes()

    @Serializable
    object BackupSchedulerScreen : NavRoutes()

    @Serializable
    object BehaviorScreen : NavRoutes()

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
    object PrivacyPolicyScreen : NavRoutes()

    @Serializable
    object LanguagesScreen : NavRoutes()

    @Serializable
    object CommandExamplesScreen : NavRoutes()

    @Serializable
    object CrashHistoryScreen : NavRoutes()

    @Serializable
    object CrashDetailsScreen : NavRoutes()

    @Serializable
    object DarkThemeScreen : NavRoutes()

    @Serializable
    object UiScaleScreen : NavRoutes()

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
    object FastbootScreen : NavRoutes()

    @Serializable
    object AdbSideloadScreen : NavRoutes()

    @Serializable
    object PairingOwnDeviceScreen : NavRoutes()

    @Serializable
    object SettingsScreen : NavRoutes()

    @Serializable
    data class WifiAdbScreen(val deviceName: String? = null) : NavRoutes()

    @Serializable
    data class FileBrowserScreen(
        val deviceAddress: String,
        val connectionMode: AdbFileBrowserConnectionMode = AdbFileBrowserConnectionMode.WIFI_ADB,
        val isOwnDevice: Boolean = false
    ) : NavRoutes()

    @Serializable
    object TileDashboardScreen : NavRoutes()

    @Serializable
    data class CreateTileScreen(val tileId: Int) : NavRoutes()

    @Serializable
    object LogcatScreen : NavRoutes()

    @Serializable
    object AiModelsScreen : NavRoutes()

    @Serializable
    object CloudModelsScreen : NavRoutes()

    @Serializable
    object GenerateColorSchemeScreen : NavRoutes()

    // Add EditColorSchemeScreen route
    @Serializable
    data class EditColorSchemeScreen(val themeId: Int) : NavRoutes()

    @Serializable
    object AiChatScreen : NavRoutes()
}


