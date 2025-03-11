package in.hridayan.ashell.shell.localadb;

import android.content.Context;
import android.content.pm.PackageManager;
import in.hridayan.ashell.utils.Utils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 12, 2022
 */
public class ShizukuShell {

  private static List<String> mOutput;
  private static ShizukuRemoteProcess mProcess = null;
  private static String mCommand;
  private static String mDir = "/";
  private ScheduledExecutorService scheduler;
  private Context context;
  private ShizukuPermCallback permCallback;

  public ShizukuShell(List<String> output, String command) {
    mOutput = output;
    mCommand = command;
  }

  public ShizukuShell(Context context, ShizukuPermCallback permissionCallback) {
    this.context = context;
    this.permCallback = permissionCallback;
  }

  public interface ShizukuPermCallback {
    void onShizukuPermGranted();
  }

  // Call this function after passing out output and command to ShizukuShell
  public void exec() {
    try {
      mProcess = Shizuku.newProcess(new String[] {"sh", "-c", mCommand}, null, mDir);
      BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
      BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
      String line;
      while ((line = mInput.readLine()) != null) {
        mOutput.add(line);
      }
      while ((line = mError.readLine()) != null) {
        mOutput.add("<font color=#FF0000>" + line + "</font>");
      }

      // Handle current directory
      if (mCommand.startsWith("cd ") && !mOutput.get(mOutput.size() - 1).endsWith("</font>")) {
        String[] array = mCommand.split("\\s+");
        String dir;
        if (array[array.length - 1].equals("/")) {
          dir = "/";
        } else if (array[array.length - 1].startsWith("/")) {
          dir = array[array.length - 1];
        } else {
          dir = mDir + array[array.length - 1];
        }
        if (!dir.endsWith("/")) {
          dir = dir + "/";
        }
        mDir = dir;
      }

      mProcess.waitFor();
    } catch (Exception ignored) {
    }
  }

  // Checks if the shizuku shell is busy , i.e. running commands
  public static boolean isBusy() {
    return mOutput != null
        && mOutput.size() > 0
        && !mOutput.get(mOutput.size() - 1).equals(Utils.shellDeadError());
  }

  // Call this method to destroy the shell
  public static void destroy() {
    if (mProcess != null) mProcess.destroy();
  }

  // Checks if the app has been granted shizuku permission
  public static boolean hasPermission() {
    if (Shizuku.pingBinder())
      return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    else return false;
  }

  // Start a background task to periodically check for Shizuku permission
  public void startPermissionCheck() {

    if (scheduler == null || scheduler.isShutdown()) {
      scheduler = Executors.newScheduledThreadPool(1);
    }

    scheduler.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            if (hasPermission()) {
              if (permCallback != null) permCallback.onShizukuPermGranted();
              stopPermissionCheck();
            }
          }
        },
        0,
        500,
        TimeUnit.MILLISECONDS);
  }

  public void stopPermissionCheck() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
  }
}
