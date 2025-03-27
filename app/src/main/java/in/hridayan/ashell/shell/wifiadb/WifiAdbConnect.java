package in.hridayan.ashell.shell.wifiadb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WifiAdbConnect {

    public interface ConnectingCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    /**
     * Connects a device via ADB over Wi-Fi.
     */
    public static void connect(Context context, String ip, String port, ConnectingCallback callback) {
        new Thread(() -> executeConnection(context, ip, port, callback)).start();
    }

    /**
     * Executes the ADB connect command.
     */
    private static void executeConnection(Context context, String ip, String port, ConnectingCallback callback) {
        Process connectingProcess = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(WifiAdbShell.adbPath(context), "connect", ip + ":" + port);
            WifiAdbShell.configureEnvironment(context, processBuilder);

            connectingProcess = processBuilder.start();

            boolean isSuccess = checkSuccess(connectingProcess);
            String errorMessage = readError(connectingProcess);

            runOnMainThread(() -> {
                if (callback != null) {
                    if (isSuccess) callback.onSuccess();
                    else callback.onFailure(errorMessage);
                }
            });

        } catch (Exception e) {
            runOnMainThread(() -> {
                if (callback != null) callback.onFailure(e.getMessage());
            });
        } finally {
            if (connectingProcess != null) {
                connectingProcess.destroy();
                if (connectingProcess.isAlive()) connectingProcess.destroyForcibly();
            }
        }
    }

    /**
     * Checks if the ADB connection was successful.
     */
    private static boolean checkSuccess(Process process) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("connected to")) return true;
        }
        return false;
    }

    /**
     * Reads error output from the ADB process.
     */
    private static String readError(Process process) throws Exception {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorMessage = new StringBuilder();
        String line;
        while ((line = errorReader.readLine()) != null) {
            errorMessage.append(line).append("\n");
        }
        return errorMessage.toString().trim();
    }

    /**
     * Runs a task on the main thread.
     */
    private static void runOnMainThread(Runnable task) {
        new Handler(Looper.getMainLooper()).post(task);
    }
}