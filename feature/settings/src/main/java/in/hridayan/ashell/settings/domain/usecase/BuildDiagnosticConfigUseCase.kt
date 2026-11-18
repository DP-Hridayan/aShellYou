package `in`.hridayan.ashell.settings.domain.usecase

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.settings.domain.model.DiagnosticAppInfo
import `in`.hridayan.ashell.settings.domain.model.DiagnosticConfig
import `in`.hridayan.ashell.settings.domain.model.DiagnosticDeviceInfo
import `in`.hridayan.ashell.settings.domain.model.DiagnosticDisplayInfo
import `in`.hridayan.ashell.settings.domain.model.DiagnosticOsInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss"
private const val DEFAULT_MIN_SDK = "28"
private const val UNKNOWN_VALUE = "Unknown"
private const val APP_NAME = "aShellYou"

class BuildDiagnosticConfigUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(): DiagnosticConfig {
        val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US)
        val currentTime = dateFormat.format(Date())

        val packageInfo = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        }.getOrNull()

        val versionName = packageInfo?.versionName ?: UNKNOWN_VALUE
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode?.toString() ?: UNKNOWN_VALUE
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toString() ?: UNKNOWN_VALUE
        }

        val targetSdk = context.applicationInfo.targetSdkVersion.toString()
        val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.applicationInfo.minSdkVersion.toString()
        } else {
            DEFAULT_MIN_SDK
        }

        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val buildType = if (isDebuggable) "debug" else "release"

        val installTime = packageInfo?.firstInstallTime?.let {
            dateFormat.format(Date(it))
        } ?: UNKNOWN_VALUE

        val lastUpdateTime = packageInfo?.lastUpdateTime?.let {
            dateFormat.format(Date(it))
        } ?: UNKNOWN_VALUE

        val socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MANUFACTURER
        } else {
            UNKNOWN_VALUE
        }

        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            UNKNOWN_VALUE
        }

        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val densityDpi = displayMetrics.densityDpi
        val fontScale = context.resources.configuration.fontScale
        val systemLanguage = Locale.getDefault().toLanguageTag()

        return DiagnosticConfig(
            generatedTimestamp = currentTime,
            appInfo = DiagnosticAppInfo(
                appName = APP_NAME,
                packageName = context.packageName,
                versionName = versionName,
                versionCode = versionCode,
                buildType = buildType,
                targetSdk = targetSdk,
                minSdk = minSdk,
                installTime = installTime,
                lastUpdateTime = lastUpdateTime,
            ),
            deviceInfo = DiagnosticDeviceInfo(
                manufacturer = Build.MANUFACTURER ?: UNKNOWN_VALUE,
                brand = Build.BRAND ?: UNKNOWN_VALUE,
                model = Build.MODEL ?: UNKNOWN_VALUE,
                deviceCodename = Build.DEVICE ?: UNKNOWN_VALUE,
                supportedAbis = Build.SUPPORTED_ABIS.joinToString(", "),
                socManufacturer = socManufacturer,
            ),
            osInfo = DiagnosticOsInfo(
                androidVersion = Build.VERSION.RELEASE ?: UNKNOWN_VALUE,
                sdkInt = Build.VERSION.SDK_INT,
                securityPatch = securityPatch,
                buildFingerprint = Build.FINGERPRINT ?: UNKNOWN_VALUE,
                bootloader = Build.BOOTLOADER ?: UNKNOWN_VALUE,
            ),
            displayInfo = DiagnosticDisplayInfo(
                resolution = "$width x $height px",
                densityDpi = densityDpi,
                densityBucket = getDensityBucket(densityDpi),
                fontScale = fontScale,
                systemLanguage = systemLanguage,
            ),
        )
    }

    private fun getDensityBucket(densityDpi: Int): String {
        return when {
            densityDpi <= DisplayMetrics.DENSITY_LOW -> "ldpi"
            densityDpi <= DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
            densityDpi <= DisplayMetrics.DENSITY_HIGH -> "hdpi"
            densityDpi <= DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
            densityDpi <= DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
            else -> "xxxhdpi"
        }
    }
}
