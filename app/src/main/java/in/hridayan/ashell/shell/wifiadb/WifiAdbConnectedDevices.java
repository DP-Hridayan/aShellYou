package in.hridayan.ashell.shell.wifiadb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for fetching connected wireless ADB devices.
 * This runs `adb devices` and filters only devices with an IP:Port format.
 */
public class WifiAdbConnectedDevices {

    /**
     * Callback interface to return the list of connected devices.
     */
    public interface ConnectedDevicesCallback {
        void onDevicesListed(List<String> devices);
        void onFailure(String errorMessage);
    }

    /**
     * Fetches the list of connected wireless ADB devices.
     *
     * @param context  The application context.
     * @param callback The callback to receive the results.
     */
    public static void getConnectedDevices(Context context, ConnectedDevicesCallback callback) {
        new Thread(() -> {
            List<String> connectedDevices = fetchConnectedDevices(context);
            if (connectedDevices != null && !connectedDevices.isEmpty()) {
                postResult(() -> callback.onDevicesListed(connectedDevices));
            } else {
                postResult(() -> callback.onFailure("No wireless devices connected"));
            }
        }).start();
    }

    /**
     * Executes the `adb devices` command and filters wireless ADB devices.
     *
     * @param context The application context.
     * @return A list of connected wireless ADB devices.
     */
    private static List<String> fetchConnectedDevices(Context context) {
        List<String> deviceList = new ArrayList<>();
        Process listProcess = null;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(WifiAdbShell.adbPath(context), "devices");
            WifiAdbShell.configureEnvironment(context, processBuilder);

            listProcess = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(listProcess.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(listProcess.getErrorStream()))) {

                // Ignore the first line (header)
                reader.readLine();

                String line;
                while ((line = reader.readLine()) != null) {
                    if (isWirelessDevice(line)) {
                        deviceList.add(line.split("\\s+")[0]); // Extract only IP:Port
                    }
                }

                if (listProcess.waitFor() != 0) {
                    return null;
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (listProcess != null) {
                listProcess.destroy();
                if (listProcess.isAlive()) {
                    listProcess.destroyForcibly();
                }
            }
        }
        return deviceList;
    }

    /**
     * Checks if a device entry represents a wireless ADB connection.
     *
     * @param deviceLine The line from `adb devices` output.
     * @return True if the line contains an IP:Port, false otherwise.
     */
    private static boolean isWirelessDevice(String deviceLine) {
        return deviceLine != null && deviceLine.contains(":") && deviceLine.contains("device");
    }

    /**
     * Posts a result to the main thread.
     *
     * @param runnable The runnable to execute on the main thread.
     */
    private static void postResult(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}