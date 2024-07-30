package in.hridayan.ashell.utils;

import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import in.hridayan.ashell.BuildConfig;

/**
 * A utility class for executing shell commands and scripts
 * with root access.
 */
public class RootShell {

    static {
        initialise();
    }

    private static void initialise() {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }

    /**
     * Closes the current shell and starts a new one.
     */
    public static void refresh() {
        closeShell();
        initialise();
    }

    private static String parseOutput(List<String> output) {
        StringBuilder sb = new StringBuilder();
        for (String s : output) {
            sb.append(s);
            sb.append("\n");
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
     * Closes the current shell.
     */
    public static void closeShell() {
        try {
            Shell shell = Shell.getCachedShell();
            if (shell != null) {
                shell.close();
            }
        } catch (IOException e) {
            Log.e("RootShell", "Failed to close shell", e);
        }
    }

    /**
     * Checks if root access is available.
     *
     * @return Whether root access is available.
     */
    public static boolean checkRoot() {
        try {
            return exec("echo /checkRoot/", true).equals("/checkRoot/");
        } catch (Exception exc) {
            return false;
        }
    }
}
