package `in`.hridayan.ashell.utils

import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

object MiuiCheck {
    /**
     * Check if the device is running on MIUI.
     * By default, HyperOS is excluded from the check.
     * If you want to include HyperOS in the check, set excludeHyperOS to false.
     *
     * @param excludeHyperOS Whether to exclude HyperOS
     * @return True if the device is running on MIUI, false otherwise
     */
    fun isMiui(excludeHyperOS: Boolean): Boolean {
        // Check if the device is manufactured by Xiaomi, Redmi, or POCO.
        val brand = Build.BRAND.lowercase(Locale.getDefault())
        val validBrands: Set<String> = HashSet(mutableListOf("xiaomi", "redmi", "poco"))
        if (!validBrands.contains(brand)) return false

        // This property is present in both MIUI and HyperOS.
        val miuiVersion = getProperty("ro.miui.ui.version.name")
        val isMiui = !miuiVersion.isNullOrBlank()
        // This property is exclusive to HyperOS only and isn't present in MIUI.
        val hyperOSVersion = getProperty("ro.mi.os.version.name")
        val isHyperOS = !hyperOSVersion.isNullOrBlank()
        return isMiui && (!excludeHyperOS || !isHyperOS)
    }

    val isMiui: Boolean
        /**
         * Overloaded method to maintain the default behavior of excluding HyperOS.
         *
         * @return True if the device is running on MIUI, false otherwise
         */
        get() = isMiui(true)

    // Private function to get the property value from build.prop.
    private fun getProperty(property: String): String? {
        try {
            val process = Runtime.getRuntime().exec("getprop $property")
            BufferedReader(InputStreamReader(process.inputStream), 1024).use { input ->
                return input.readLine()
            }
        } catch (e: IOException) {
            Log.e("MiuiCheck", "Unable to read property $property", e)
            return null
        }
    }
}
