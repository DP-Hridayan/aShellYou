package in.hridayan.ashell.shell.localadb;

import android.content.Context;
import android.util.Log;
import com.topjohnwu.superuser.Shell;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.utils.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** A utility class for executing shell commands and scripts with root access. */
public class RootShell {

  private static List<String> mOutput;
  private static String mCommand;
  private static Process mProcess = null;
  private static final int TIMEOUT = 50; // in milliseconds
  private ScheduledExecutorService scheduler;
  private Context context;
  private RootPermCallback permCallback;

  public RootShell(List<String> output, String command) {
    mOutput = output;
    mCommand = command;
  }

  public RootShell(Context context, RootPermCallback permissionCallback) {
    this.context = context;
    this.permCallback = permissionCallback;
  }

  public interface RootPermCallback {
    void onRootPermGranted();
  }

  // Call this method after passing output and command to RootShell
  public static void exec() {
    try {
      /*We prefix the command with "su -c " to run the command with root privilege*/
      /*  String finalCommand = isMultiCommand(mCommand) ? mCommand : "su -c" + filterCommand(mCommand); */

      mProcess = Runtime.getRuntime().exec("su -c " + filterCommand(mCommand));

      BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
      BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
      String line;
      while ((line = mInput.readLine()) != null) {
        mOutput.add(line);
      }
      while ((line = mError.readLine()) != null) {
        mOutput.add("<font color=#FF0000>" + line + "</font>");
      }
      mProcess.waitFor();
    } catch (Exception e) {
      Log.e("RootShell", "Failed to execute command", e);
    }
  }

  private static String filterCommand(String command) {
    return command.replaceAll("(?i)^\\s*su\\s+-c\\s+", "");
  }

  /* public static boolean isMultiCommand(String command) {
    // Define special characters that indicate a multi-command
    String[] specialCharacters = {"|", ">", "<", "&&", "||", ";"};

    // Check if the command contains any special character
    for (String character : specialCharacters) {
      if (command.contains(character)) return true;
    }

    return false;
  } */

  static {
    initialise();
  }

  // Initialise the shell
  private static void initialise() {
    Shell.enableVerboseLogging = BuildConfig.DEBUG;
    Shell.setDefaultBuilder(
        Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(10));
  }

  /** Closes the current shell and starts a new one. */
  public static void refresh() {
    closeShell();
    initialise();
  }

  private static String parseOutput(List<String> output) {
    StringBuilder sb = new StringBuilder();
    for (String s : output) {
      sb.append(s).append("\n");
    }
    return sb.toString().trim();
  }

  /**
   * Executes a shell command and returns the output.
   *
   * @param command The command to execute.
   * @param root Whether to execute the command as root.
   * @return The output of the command.
   */
  public static String exec(String command, boolean root) {
    List<String> output;
    if (root) {
      output = Shell.cmd(command).exec().getOut();
    } else {
      output = Shell.cmd(command).exec().getOut();
    }
    return parseOutput(output);
  }

  /**
   * Executes a shell script and returns the output.
   *
   * @param script The script to execute.
   * @param root Whether to execute the script as root.
   * @return The output of the script.
   */
  public static String exec(InputStream script, boolean root) {
    List<String> output;
    if (root) {
      output = Shell.cmd(script).exec().getOut();
    } else {
      output = Shell.cmd(script).exec().getOut();
    }
    return parseOutput(output);
  }

  /**
   * Executes a shell script and writes the output to the provided list.
   *
   * @param script The script to execute.
   * @param root Whether to execute the script as root.
   * @param output The list to write the output to.
   */
  public static void exec(InputStream script, boolean root, ArrayList<String> output) {
    if (root) {
      Shell.cmd(script).to(output, output).exec();
    } else {
      Shell.cmd(script).to(output, output).exec();
    }
  }

  /**
   * Checks if app has root access We quickly return a result to avoid delays
   *
   * @return Whether root access is available.
   */
  public static boolean hasPermission() {
    refresh();
    final AtomicBoolean hasPerm = new AtomicBoolean(false);
    ExecutorService executor = Executors.newSingleThreadExecutor();

    try {
      Future<?> future = executor.submit(() -> hasPerm.set(Shell.getShell().isRoot()));
      future.get(500, TimeUnit.MILLISECONDS); // Adjust timeout as needed
    } catch (Exception e) {
      Log.e("RootShell", "Error checking permission", e);
    } finally {
      executor.shutdown();
    }

    return hasPerm.get();
  }

  /**
   * Checks if the device is rooted. This method quickly returns a result after a timeout to avoid
   * cases where output is delayed. Also, this method cannot detect root if the superuser
   * application hides the root status from aShell.
   */
  public static boolean isDeviceRooted() {
    final AtomicBoolean isRooted = new AtomicBoolean(false);
    ExecutorService executor = Executors.newSingleThreadExecutor();

    try {
      Future<?> future =
          executor.submit(
              () -> {
                Process process = null;
                try {
                  process = Runtime.getRuntime().exec("which su");
                  BufferedReader reader =
                      new BufferedReader(new InputStreamReader(process.getInputStream()));
                  String response = reader.readLine();
                  if (response != null && response.contains("su")) {
                    isRooted.set(true);
                  }
                } catch (Exception e) {
                  Log.e("RootShell", "Error checking if device is rooted", e);
                } finally {
                  if (process != null) {
                    process.destroy();
                  }
                }
              });

      future.get(TIMEOUT, TimeUnit.MILLISECONDS); // Adjust timeout as needed
    } catch (Exception e) {
      Log.e("RootShell", "Error checking if device is rooted", e);
    } finally {
      executor.shutdown();
    }

    return isRooted.get();
  }

  // Checks if root shell is busy or not
  public static boolean isBusy() {
    return mOutput != null
        && mOutput.size() > 0
        && !mOutput.get(mOutput.size() - 1).equals(Utils.shellDeadError());
  }

  /** Closes the current shell. */
  public static void closeShell() {
    try {
      Shell shell = Shell.getShell();
      if (shell != null) {
        shell.close();
        destroy();
      }
    } catch (IOException e) {
      Log.e("RootShell", "Failed to close shell", e);
    }
  }

  // Destroys the running shell process
  public static void destroy() {
    if (mProcess != null) {
      mProcess.destroy();
      mProcess = null; // Nullify the process after destruction
    }
  }

  // Start a background task to periodically check for Root permission
  public void startPermissionCheck() {
    // stops previous running checks
    stopPermissionCheck();

    if (scheduler == null || scheduler.isShutdown()) {
      scheduler = Executors.newScheduledThreadPool(1);
    }

    scheduler.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            if (hasPermission()) {
              if (permCallback != null) permCallback.onRootPermGranted();
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
    
    /**
 * Gets the root provider name.
 * @return The root provider name (e.g., "KernelSU", "Magisk"), or "Unknown" if not found.
 */
public static String getRootProvider() {
    // Check for KernelSU
    Shell.Result ksuResult = Shell.cmd("su -c ksu --version").exec();
    if (ksuResult.isSuccess()) {
        return "KernelSU";
    }

    // Check for Magisk
    Shell.Result magiskResult = Shell.cmd("su -c magisk -V").exec();
    if (magiskResult.isSuccess()) {
        return "Magisk";
    }

    return "Unknown";
}

/**
 * Gets the version of the current root provider.
 * @return The root version as a string, or "Unknown" if not found.
 */
public static String getRootVersion() {
    // Check KernelSU version
    Shell.Result ksuResult = Shell.cmd("su -c ksu --version").exec();
    if (ksuResult.isSuccess() && !ksuResult.getOut().isEmpty()) {
        return ksuResult.getOut().get(0);
    }

    // Check Magisk version
    Shell.Result magiskResult = Shell.cmd("su -c magisk -V").exec();
    if (magiskResult.isSuccess() && !magiskResult.getOut().isEmpty()) {
        return magiskResult.getOut().get(0);
    }

    return "Unknown";
}
}
