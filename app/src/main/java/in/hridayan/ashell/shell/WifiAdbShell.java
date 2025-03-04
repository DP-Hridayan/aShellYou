package in.hridayan.ashell.shell;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import in.hridayan.ashell.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class WifiAdbShell {

  private static List<String> mOutput;
  private static String mCommand;
  private static Process mProcess = null;
  private static final int TIMEOUT = 750; // in milliseconds

  public WifiAdbShell(List<String> output, String command) {
    mOutput = output;
    mCommand = command;
  }

  // Call this method after passing output and command to BasicShell
  public static void exec(Context context) {
    try {
      File adbFile = new File(context.getFilesDir(), "adb"); // Check if file exists
      if (!adbFile.exists()) {
        mOutput.add("ADB binary not found at: " + adbFile.getAbsolutePath());
        return;
      }

      String adbPath = adbFile.getAbsolutePath();

      // Use system linker to execute ADB
      String[] fullCommand = {"su", "-c", adbPath + " shell " + mCommand};

      mProcess = Runtime.getRuntime().exec(fullCommand);

      BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
      BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));

      String line;
      while ((line = mInput.readLine()) != null) {
        mOutput.add(line);
      }
      while ((line = mError.readLine()) != null) {
        mOutput.add("<font color=#FF0000>" + line + "</font>"); // Log errors in red
      }

      int exitCode = mProcess.waitFor();
      mOutput.add("Process exited with code: " + exitCode);

    } catch (Exception e) {
      mOutput.add("<font color=#FF0000>Exception: " + e.getMessage() + "</font>");
    }
  }

  public interface PairingCallback {
    void onSuccess();

    void onFailure(String errorMessage);
  }

  public static void pair(
      Context context, String ip, String port, String pairingCode, PairingCallback callback) {
    new Thread(
            () -> { // Run in background thread
              try {
                File adbFile = new File(context.getFilesDir(), "adb");
                if (!adbFile.exists()) {
                  if (callback != null)
                    callback.onFailure("ADB binary not found at: " + adbFile.getAbsolutePath());
                  return;
                }

                String adbPath = adbFile.getAbsolutePath();
                String[] pairCommand = {
                  "su", "-c", adbPath + " pair " + ip + ":" + port + " " + pairingCode
                };

                // kill the server first and start a fresh one
                killServer(context);
                Thread.sleep(500);
                startServer(context);

                Process pairingProcess = Runtime.getRuntime().exec(pairCommand);
                BufferedReader reader =
                    new BufferedReader(new InputStreamReader(pairingProcess.getInputStream()));
                BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(pairingProcess.getErrorStream()));

                String line;
                boolean isSuccess = false;
                StringBuilder errorMessage = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                  if (line.contains("Successfully paired")) { // Check for success message
                    isSuccess = true;
                    break;
                  }
                }

                while ((line = errorReader.readLine()) != null) {
                  errorMessage.append(line).append("\n");
                }

                pairingProcess.waitFor(); // Wait for process to finish

                boolean finalSuccess = isSuccess;
                String finalErrorMessage = errorMessage.toString().trim();

                // Run callback on main thread
                new Handler(Looper.getMainLooper())
                    .post(
                        () -> {
                          if (finalSuccess) {
                            if (callback != null) callback.onSuccess();
                          } else {
                            if (callback != null) callback.onFailure(finalErrorMessage);
                          }
                        });

              } catch (Exception e) {
                new Handler(Looper.getMainLooper())
                    .post(
                        () -> {
                          if (callback != null) callback.onFailure(e.getMessage());
                        });
              }
            })
        .start();
  }

  public interface ConnectingCallback {
    void onSuccess();

    void onFailure(String errorMessage);
  }

  public static void connect(Context context, String ip, String port, ConnectingCallback callback) {
    new Thread(
            () -> { // Run in background thread
              try {
                File adbFile = new File(context.getFilesDir(), "adb");
                if (!adbFile.exists()) {
                  if (callback != null)
                    callback.onFailure("ADB binary not found at: " + adbFile.getAbsolutePath());
                  return;
                }

                String adbPath = adbFile.getAbsolutePath();
                String[] connectCommand = {"su", "-c", adbPath + " connect " + ip + ":" + port};

                Process connectingProcess = Runtime.getRuntime().exec(connectCommand);
                BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connectingProcess.getInputStream()));
                BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(connectingProcess.getErrorStream()));

                String line;
                boolean isSuccess = false;
                StringBuilder errorMessage = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                  if (line.contains("connected to")) { // Check for success message
                    isSuccess = true;
                    break;
                  }
                }

                while ((line = errorReader.readLine()) != null) {
                  errorMessage.append(line).append("\n");
                }

                connectingProcess.waitFor(); // Wait for process to finish

                boolean finalSuccess = isSuccess;
                String finalErrorMessage = errorMessage.toString().trim();

                // Run callback on main thread
                new Handler(Looper.getMainLooper())
                    .post(
                        () -> {
                          // finalSuccess = device connected
                          if (finalSuccess) {
                            if (callback != null) callback.onSuccess();
                          } else {
                            if (callback != null) callback.onFailure(finalErrorMessage);
                          }
                        });

              } catch (Exception e) {
                new Handler(Looper.getMainLooper())
                    .post(
                        () -> {
                          if (callback != null) callback.onFailure(e.getMessage());
                        });
              }
            })
        .start();
  }

  private static String adbPath(Context context) {
    File adbFile = new File(context.getFilesDir(), "adb");
    if (!adbFile.exists()) {
      return "adb file not found";
    }
    return adbFile.getAbsolutePath();
  }

  // Starts the adb tcpip server
  public static void startServer(Context context) {
    try {
      String[] startServer = {"su", "-c", adbPath(context) + "start-server"};
      Runtime.getRuntime().exec(startServer);
    } catch (Exception e) {
    }
  }

  // Kills the adb tcpip server
  public static void killServer(Context context) {
    try {
      String[] killServer = {"su", "-c", adbPath(context) + "kill-server"};
      Runtime.getRuntime().exec(killServer);
    } catch (Exception e) {
    }
  }

  // Checks if shell is busy or not
  public static boolean isBusy() {
    return mOutput != null
        && mOutput.size() > 0
        && !mOutput.get(mOutput.size() - 1).equals(Utils.shellDeadError());
  }

  // Destroys the running shell process
  public static void destroy() {
    if (mProcess != null) mProcess.destroy();
  }

  public static void copyAdbBinaryToData(Context context) {
    try {
      File adbFile = new File(context.getFilesDir(), "adb");

      if (!adbFile.exists()) {
        InputStream in = context.getAssets().open("adb");
        FileOutputStream out = new FileOutputStream(adbFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
          out.write(buffer, 0, read);
        }

        in.close();
        out.close();

        // Set permissions
        adbFile.setExecutable(true, false);
        adbFile.setReadable(true, false);
        adbFile.setWritable(true, false);

        // Extra step: chmod 755
        Runtime.getRuntime().exec("chmod 755 " + adbFile.getAbsolutePath()).waitFor();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
