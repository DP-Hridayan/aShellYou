package in.hridayan.ashell.shell.wifiadb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class WifiAdbPair {
  private static final int TIMEOUT = 5; // Timeout in seconds

  public interface PairingCallback {
    void onSuccess();
    void onFailure(String errorMessage);
  }

  // Initiates pairing with the given IP, port, and pairing code
  public static void pair(Context context, String ip, String port, String pairingCode, PairingCallback callback) {
    new Thread(() -> {
      Process pairingProcess = null;
      try {
        WifiAdbShell.killServer(context); // Restart ADB server before pairing
        Thread.sleep(500);

        pairingProcess = startPairingProcess(context, ip, port, pairingCode);
        boolean isSuccess = checkPairingSuccess(pairingProcess);
        String errorOutput = readErrorOutput(pairingProcess);

        // Handle timeout
        if (!pairingProcess.waitFor(TIMEOUT, TimeUnit.SECONDS)) {
          pairingProcess.destroy();
        }

        postResult(callback, isSuccess, errorOutput);

      } catch (Exception e) {
        postFailure(callback, e.getMessage());
      } finally {
        destroyProcess(pairingProcess);
      }
    }).start();
  }

  // Starts the ADB pairing process
  private static Process startPairingProcess(Context context, String ip, String port, String pairingCode) throws Exception {
    ProcessBuilder processBuilder = new ProcessBuilder(
        WifiAdbShell.adbPath(context), "pair", ip + ":" + port, pairingCode);

    WifiAdbShell.configureEnvironment(context, processBuilder);
    return processBuilder.start();
  }

  // Checks if the pairing was successful
  private static boolean checkPairingSuccess(Process process) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.contains("Successfully paired")) {
        return true;
      }
    }
    return false;
  }

  // Reads error output from the process
  private static String readErrorOutput(Process process) throws Exception {
    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    StringBuilder errorMessage = new StringBuilder();
    String line;
    while ((line = errorReader.readLine()) != null) {
      errorMessage.append(line).append("\n");
    }
    return errorMessage.toString().trim();
  }

  // Posts success or failure results on the main thread
  private static void postResult(PairingCallback callback, boolean isSuccess, String errorMessage) {
    new Handler(Looper.getMainLooper()).post(() -> {
      if (callback != null) {
        if (isSuccess) {
          callback.onSuccess();
        } else {
          callback.onFailure(errorMessage.isEmpty() ? "Pairing failed" : errorMessage);
        }
      }
    });
  }

  // Posts failure result
  private static void postFailure(PairingCallback callback, String errorMessage) {
    new Handler(Looper.getMainLooper()).post(() -> {
      if (callback != null) callback.onFailure(errorMessage);
    });
  }

  // Ensures the process is properly terminated
  private static void destroyProcess(Process process) {
    if (process != null) {
      process.destroy();
      if (process.isAlive()) process.destroyForcibly();
    }
  }
}