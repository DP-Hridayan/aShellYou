package in.hridayan.ashell.shell;

import in.hridayan.ashell.utils.Utils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class BasicShell {

  private static List<String> mOutput;
  private static String mCommand;
  private static Process mProcess = null;
  private static final int TIMEOUT = 750; // in milliseconds

  public BasicShell(List<String> output, String command) {
    mOutput = output;
    mCommand = command;
  }

  // Call this method after passing output and command to BasicShell
  public static void exec() {

    try {
      mProcess = Runtime.getRuntime().exec("sh -c " + mCommand);

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
}
