package in.hridayan.ashell.utils;

import android.util.Log;
import com.topjohnwu.superuser.Shell;
import in.hridayan.ashell.BuildConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** A utility class for executing shell commands and scripts with root access. */
public class RootShell {

  private static List<String> mOutput;
  private static String mCommand;
  private static Process mProcess = null;
  private static final int TIMEOUT = 750; // in milliseconds

  public RootShell(List<String> output, String command) {
    mOutput = output;
    mCommand = command;
  }

  // Call this method after passing output and command to RootShell
  public static void exec() {

    try {
      /*We prefix the command with "su -c " to run the command with root priviledge*/
      mProcess = Runtime.getRuntime().exec("su -c " + mCommand);

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
    } catch (Exception ignored) {

    }
  }

  static {
    initialise();
  }

  // initialise the shell
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
      output = Shell.su(command).exec().getOut();
    } else {
      output = Shell.sh(command).exec().getOut();
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
      output = Shell.su(script).exec().getOut();
    } else {
      output = Shell.sh(script).exec().getOut();
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
      Shell.su(script).to(output, output).exec();
    } else {
      Shell.sh(script).to(output, output).exec();
    }
  }

  /**
   * Checks if app has root access We quickly return a result to avoid delays
   *
   * @return Whether root access is available.
   */
  public static boolean hasPermission() {
    final AtomicBoolean hasPermission = new AtomicBoolean(false);
    final Thread thread =
        new Thread(
            () -> {
              try {
                String result = exec("echo /checkRoot/", true);
                hasPermission.set("/checkRoot/".equals(result));
              } catch (Exception e) {
                // Handle the exception
              }
            });

    thread.start();
    try {
      thread.join(TIMEOUT); // Wait for the specified timeout
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // If the thread is still alive after the timeout, interrupt it
    if (thread.isAlive()) {
      thread.interrupt();
    }

    return hasPermission.get();
  }

  /*Checks if device is rooted or not. This method quickly returns a result after a timeout to avoid cases where output is delayed. Also this method cannot detect root if the superuser application hides the root status from aShell You */
  public static boolean isDeviceRooted() {
    final AtomicBoolean isRooted = new AtomicBoolean(false);
    final Thread thread =
        new Thread(
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
                // Handle the exception
              } finally {
                if (process != null) {
                  process.destroy();
                }
              }
            });

    thread.start();
    try {
      thread.join(TIMEOUT); // Wait for the specified timeout
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // If the thread is still alive after the timeout, interrupt it
    if (thread.isAlive()) {
      thread.interrupt();
    }

    return isRooted.get();
  }

  // Checks if root shell is busy or not
  public static boolean isBusy() {
    return mOutput != null
        && mOutput.size() > 0
        && !mOutput.get(mOutput.size() - 1).equals("Shell is dead");
  }

  /** Closes the current shell. */
  public static void closeShell() {
    try {
      Shell shell = Shell.getCachedShell();
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
    if (mProcess != null) mProcess.destroy();
  }
}
