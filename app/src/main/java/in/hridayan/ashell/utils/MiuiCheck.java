package in.hridayan.ashell.utils;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MiuiCheck {

    /**
     * Check if the device is running on MIUI.
     * By default, HyperOS is excluded from the check.
     * If you want to include HyperOS in the check, set excludeHyperOS to false.
     *
     * @param excludeHyperOS Whether to exclude HyperOS
     * @return True if the device is running on MIUI, false otherwise
     */
    public static boolean isMiui(boolean excludeHyperOS) {
        // Check if the device is manufactured by Xiaomi, Redmi, or POCO.
        String brand = android.os.Build.BRAND.toLowerCase();
        Set<String> validBrands = new HashSet<>(Arrays.asList("xiaomi", "redmi", "poco"));
        if (!validBrands.contains(brand)) return false;

        // This property is present in both MIUI and HyperOS.
        String miuiVersion = getProperty("ro.miui.ui.version.name");
        boolean isMiui = miuiVersion != null && !miuiVersion.isBlank();
        // This property is exclusive to HyperOS only and isn't present in MIUI.
        String hyperOSVersion = getProperty("ro.mi.os.version.name");
        boolean isHyperOS = hyperOSVersion != null && !hyperOSVersion.isBlank();
        return isMiui && (!excludeHyperOS || !isHyperOS);
    }

    /**
     * Overloaded method to maintain the default behavior of excluding HyperOS.
     *
     * @return True if the device is running on MIUI, false otherwise
     */
    public static boolean isMiui() {
        return isMiui(true);
    }

    // Private function to get the property value from build.prop.
    private static String getProperty(String property) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + property);
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024)) {
                return input.readLine();
            }
        } catch (IOException e) {
            Log.e("MiuiCheck", "Unable to read property " + property, e);
            return null;
        }
    }
}
