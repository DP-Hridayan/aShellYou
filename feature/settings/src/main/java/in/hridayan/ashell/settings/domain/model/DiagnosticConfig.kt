package `in`.hridayan.ashell.settings.domain.model

data class DiagnosticAppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: String,
    val buildType: String,
    val targetSdk: String,
    val minSdk: String,
    val installTime: String,
    val lastUpdateTime: String,
)

data class DiagnosticDeviceInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val deviceCodename: String,
    val supportedAbis: String,
    val socManufacturer: String,
)

data class DiagnosticOsInfo(
    val androidVersion: String,
    val sdkInt: Int,
    val securityPatch: String,
    val buildFingerprint: String,
    val bootloader: String,
)

data class DiagnosticDisplayInfo(
    val resolution: String,
    val densityDpi: Int,
    val densityBucket: String,
    val fontScale: Float,
    val systemLanguage: String,
)

data class DiagnosticConfig(
    val generatedTimestamp: String,
    val appInfo: DiagnosticAppInfo,
    val deviceInfo: DiagnosticDeviceInfo,
    val osInfo: DiagnosticOsInfo,
    val displayInfo: DiagnosticDisplayInfo,
) {
    fun formatReport(): String {
        return buildString {
            appendLine("=======================================================")
            appendLine("aShellYou Diagnostic Configuration Report")
            appendLine("Generated: $generatedTimestamp")
            appendLine("=======================================================")
            appendLine()
            appendLine("[APP INFORMATION]")
            appendLine("Application:         ${appInfo.appName}")
            appendLine("Package Name:        ${appInfo.packageName}")
            appendLine("Version Name:        ${appInfo.versionName}")
            appendLine("Version Code:        ${appInfo.versionCode}")
            appendLine("Build Type:          ${appInfo.buildType}")
            appendLine("Target SDK:          ${appInfo.targetSdk}")
            appendLine("Min SDK:             ${appInfo.minSdk}")
            appendLine("Install Time:        ${appInfo.installTime}")
            appendLine("Last Update:         ${appInfo.lastUpdateTime}")
            appendLine()
            appendLine("[DEVICE & HARDWARE]")
            appendLine("Manufacturer:        ${deviceInfo.manufacturer}")
            appendLine("Brand:               ${deviceInfo.brand}")
            appendLine("Model:               ${deviceInfo.model}")
            appendLine("Device / Board:      ${deviceInfo.deviceCodename}")
            appendLine("Supported ABIs:      ${deviceInfo.supportedAbis}")
            appendLine("SoC Manufacturer:    ${deviceInfo.socManufacturer}")
            appendLine()
            appendLine("[OS & FIRMWARE]")
            appendLine("Android Version:     ${osInfo.androidVersion} (API ${osInfo.sdkInt})")
            appendLine("Security Patch:      ${osInfo.securityPatch}")
            appendLine("Build Fingerprint:   ${osInfo.buildFingerprint}")
            appendLine("Bootloader:          ${osInfo.bootloader}")
            appendLine()
            appendLine("[DISPLAY & LOCALE]")
            appendLine("Screen Resolution:   ${displayInfo.resolution}")
            appendLine("Screen Density:      ${displayInfo.densityDpi} dpi (${displayInfo.densityBucket})")
            appendLine("Font Scale:          ${displayInfo.fontScale}")
            appendLine("System Language:     ${displayInfo.systemLanguage}")
            appendLine()
            appendLine("=======================================================")
            appendLine("End of Diagnostic Report")
            append("=======================================================")
        }
    }
}
