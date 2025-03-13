package in.hridayan.ashell.shell.wifiadb;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import in.hridayan.ashell.utils.PermissionUtils;
import in.hridayan.ashell.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class WifiAdbShell {

  private static List<String> mOutput;
  private static String mCommand;
  private static String mSelectedDevice;
  private static Process mProcess = null;

  public WifiAdbShell(List<String> output, String command, String selectedDevice) {
    mOutput = output;
    mCommand = command;
    mSelectedDevice = selectedDevice;
  }

  /** Executes an ADB command with proper permissions. */
  public static void execCommand(Context context, Activity activity) {
    if (!PermissionUtils.haveStoragePermission(context)
        && doesAdbCommandNeedStorageAccess(mCommand)) {
      PermissionUtils.requestStoragePermission(activity);
      return;
    }

    try {
      String deviceTargetedCommand = mCommand;

      if (!mCommand.equals("devices")) {
        deviceTargetedCommand = "-s " + mSelectedDevice + " " + filterCommand(mCommand);
      }

      String[] commandArray = deviceTargetedCommand.split(" ");
      String[] fullCommand = new String[commandArray.length + 1];

      fullCommand[0] = adbPath(context);
      System.arraycopy(commandArray, 0, fullCommand, 1, commandArray.length);

      ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);
      configureEnvironment(context, processBuilder);

      mProcess = processBuilder.start();

      BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
      BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));

      String line;
      while ((line = mInput.readLine()) != null) mOutput.add(line);
      while ((line = mError.readLine()) != null)
        mOutput.add("<font color=#FF0000>" + line + "</font>");

      mOutput.add("Process exited with code: " + mProcess.waitFor());

    } catch (Exception e) {
      mOutput.add("<font color=#FF0000>Exception: " + e.getMessage() + "</font>");
    }
  }

  /** Executes an ADB command and returns its output. */
  public static boolean exec(Context context, String... command) {
    Process process = null;
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      configureEnvironment(context, processBuilder);
      process = processBuilder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader errorReader =
          new BufferedReader(new InputStreamReader(process.getErrorStream()));

      StringBuilder output = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
        return true;
      }
      while ((line = errorReader.readLine()) != null) {
        output.append("ERROR: ").append(line).append("\n");
        return false;
      }

      process.waitFor();
      return true;

    } catch (Exception e) {
      return false;
    } finally {
      if (process != null) {
        process.destroy();
        if (process.isAlive()) process.destroyForcibly();
      }
    }
  }

  /** Restarts the ADB server. */
  public static void restartAdbServer(Context context) {
    exec(context, "kill-server");
    exec(context, "start-server");
  }

  /** Starts the ADB server. */
  public static void startServer(Context context) {
    exec(context, "start-server");
  }

  /** Kills the ADB server. */
  public static void killServer(Context context) {
    exec(context, "kill-server");
  }

  /** Checks if the ADB shell is busy. */
  public static boolean isBusy() {
    return mOutput != null
        && mOutput.size() > 0
        && !mOutput.get(mOutput.size() - 1).equals(Utils.shellDeadError());
  }

  /** Destroys the running shell process. */
  public static void destroy() {
    if (mProcess != null) mProcess.destroy();
  }

  /** Configures environment variables for ADB execution. */
  public static void configureEnvironment(Context context, ProcessBuilder processBuilder) {
    String tmpDir = ensureTmpDir(context);
    Map<String, String> env = processBuilder.environment();
    env.put("HOME", context.getFilesDir().getAbsolutePath());
    env.put("ADB_VENDOR_KEYS", context.getFilesDir().getAbsolutePath());
    env.put("TMPDIR", tmpDir);
  }

  /** Ensures the TMP directory exists and returns its path. */
  public static String ensureTmpDir(Context context) {
    File tmpDir = new File(context.getFilesDir(), "tmp");
    if (!tmpDir.exists()) {
      Log.d("WifiAdbShell", "TMPDIR created: " + tmpDir.mkdirs());
    }
    return tmpDir.getAbsolutePath();
  }

  /** Returns the ADB binary path. */
  public static String adbPath(Context context) {
    return context.getApplicationInfo().nativeLibraryDir + "/libadb.so";
  }

  private static String filterCommand(String command) {
    return command.replaceAll("adb ", " ");
  }

  /** Checks if the given ADB command requires storage access permissions. */
  private static boolean doesAdbCommandNeedStorageAccess(String command) {
    String[] storageCommands = {
      "push",
      "pull",
      "backup",
      "restore",
      "install",
      "uninstall",
      "cp",
      "mv",
      "rm",
      "rmdir",
      "mkdir",
      "ls",
      "stat",
      "find",
      "touch",
      "chmod",
      "chown"
    };

    for (String cmd : storageCommands) {
      if (command.contains(cmd + " ")) {
        return true;
      }
    }

    return command.matches(".*\\b(/sdcard/|/storage/emulated/|\\.apk)\\b.*");
  }
}
