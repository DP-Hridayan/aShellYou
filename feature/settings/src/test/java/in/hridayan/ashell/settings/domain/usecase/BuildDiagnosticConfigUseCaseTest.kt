package `in`.hridayan.ashell.settings.domain.usecase

import `in`.hridayan.ashell.settings.domain.model.DiagnosticAppInfo
import `in`.hridayan.ashell.settings.domain.model.DiagnosticConfig
import `in`.hridayan.ashell.settings.domain.model.DiagnosticDeviceInfo
import `in`.hridayan.ashell.settings.domain.model.DiagnosticDisplayInfo
import `in`.hridayan.ashell.settings.domain.model.DiagnosticOsInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildDiagnosticConfigUseCaseTest {

    @Test
    fun `formatReport generates correct sections and excludes unwanted details`() {
        val config = DiagnosticConfig(
            generatedTimestamp = "2026-11-18 21:30:00",
            appInfo = DiagnosticAppInfo(
                appName = "aShellYou",
                packageName = "in.hridayan.ashell",
                versionName = "1.0.0",
                versionCode = "100",
                buildType = "debug",
                targetSdk = "35",
                minSdk = "28",
                installTime = "2026-11-18 20:00:00",
                lastUpdateTime = "2026-11-18 20:00:00",
            ),
            deviceInfo = DiagnosticDeviceInfo(
                manufacturer = "Google",
                brand = "google",
                model = "Pixel 8 Pro",
                deviceCodename = "husky",
                supportedAbis = "arm64-v8a",
                socManufacturer = "Google",
            ),
            osInfo = DiagnosticOsInfo(
                androidVersion = "15",
                sdkInt = 35,
                securityPatch = "2026-11-01",
                buildFingerprint = "google/husky/husky:15/AP2A.240805.005:user/release-keys",
                bootloader = "husky-1.0",
            ),
            displayInfo = DiagnosticDisplayInfo(
                resolution = "1344 x 2992 px",
                densityDpi = 480,
                densityBucket = "xxhdpi",
                fontScale = 1.0f,
                systemLanguage = "en-US",
            ),
        )

        val report = config.formatReport()

        // Required headers
        assertTrue(report.contains("aShellYou Diagnostic Configuration Report"))
        assertTrue(report.contains("[APP INFORMATION]"))
        assertTrue(report.contains("[DEVICE & HARDWARE]"))
        assertTrue(report.contains("[OS & FIRMWARE]"))
        assertTrue(report.contains("[DISPLAY & LOCALE]"))
        assertTrue(report.contains("End of Diagnostic Report"))

        // Key expected fields
        assertTrue(report.contains("Package Name:        in.hridayan.ashell"))
        assertTrue(report.contains("Version Name:        1.0.0"))
        assertTrue(report.contains("Model:               Pixel 8 Pro"))
        assertTrue(report.contains("Android Version:     15 (API 35)"))
        assertTrue(report.contains("Screen Density:      480 dpi (xxhdpi)"))

        // Verification of excluded sections
        assertFalse(report.contains("[PRIVILEGES & SHELL STATE]"))
        assertFalse(report.contains("Shizuku"))
        assertFalse(report.contains("Root Access"))
        assertFalse(report.contains("[APP PREFERENCES"))
        assertFalse(report.contains("Haptics & Vibration"))
        assertFalse(report.contains("Timezone:"))
    }
}
