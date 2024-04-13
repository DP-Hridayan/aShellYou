package in.hridayan.ashell.utils;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import in.hridayan.ashell.utils.Preferences;

public class Commands {
  private static List<CommandItems> mPackages = null;

  public static List<CommandItems> commandList(Context context) {
    List<CommandItems> mCommands = new ArrayList<>();
    mCommands.add(
        new CommandItems(
            "am force-stop <package>",
            "Completely stop a given package",
            "am force-stop com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "am kill <package>",
            "Kill all background processes associated with a given package",
            "am kill com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "am kill-all",
            "Kill all processes that are safe to kill (cached, etc)",
            "am kill-all",
            context));
    mCommands.add(
        new CommandItems(
            "cat <file_path>",
            "Display the contents of a text file",
            "cat /system/build.prop",
            context));
    mCommands.add(new CommandItems("clear", "Clear terminal screen", "clear", context));
    mCommands.add(
        new CommandItems(
            "cp <from> <to>",
            "Copy a file",
            "cp /system/build.prop /sdcard\nor\ncp /system/build.prop /sdcard/build_prop-backup.txt",
            context));
    mCommands.add(
        new CommandItems(
            "cp -r <from> <to>",
            "Copy a file or directory",
            "cp -r /system/app /sdcard\n\ncp -r /system/app /sdcard/abc",
            context));

    mCommands.add(
        new CommandItems("du -h", "Shows disk usage in human readable form", "du -h", context));
    mCommands.add(
        new CommandItems("dumpsys activity", "Print activity info", "dumpsys activity", context));
    mCommands.add(
        new CommandItems("dumpsys battery", "Primt battery stats", "dumpsys battery", context));
    mCommands.add(
        new CommandItems(
            "dumpsys battery set level <n>",
            "Change the level from 0 to 100",
            "dumpsys battery set level 95",
            context));
    mCommands.add(
        new CommandItems(
            "dumpsys battery set status <n>",
            "Change the level to unknown, charging, discharging, not charging or full",
            "dumpsys battery set status 0",
            context));
    mCommands.add(
        new CommandItems(
            "dumpsys battery reset", "Reset battery", "dumpsys battery reset", context));
    mCommands.add(
        new CommandItems("dumpsys display", "Primt display stats", "dumpsys display", context));
    mCommands.add(
        new CommandItems(
            "echo <message>", "Display message on screen", "echo Hello World", context));
    mCommands.add(new CommandItems("exit", "Exit the shell", "exit", context));
    mCommands.add(
        new CommandItems(
            "file <file_path>", "Determine file type", "file /system/build.prop", context));
    mCommands.add(
        new CommandItems(
            "goto top", "Go to the top of the shell output (fun command)", "goto top", context));

    mCommands.add(
        new CommandItems(
            "goto bottom",
            "Go to the bottom of the shell output (fun command)",
            "goto bottom",
            context));
    mCommands.add(
        new CommandItems(
            "grep", "Search file(s) for lines that match a given pattern", "grep", context));
    mCommands.add(
        new CommandItems(
            "kill <pid>", "Kill a process by specifying its PID", "kill <pid>", context));
    mCommands.add(
        new CommandItems(
            "logcat",
            "Display real-time log of system messages, including stack traces",
            "logcat",
            context));
    mCommands.add(
        new CommandItems("logcat -g", "Displays current log buffer sizes", "logcat -g", context));
    mCommands.add(
        new CommandItems(
            "logcat -G <size>", "Sets the buffer size (K or M)", "logcat -G 1M", context));
    mCommands.add(new CommandItems("logcat -c", "Clears the log buffers", "logcat -c", context));
    mCommands.add(new CommandItems("ls", "List contents of a directory", "ls /system", context));
    mCommands.add(
        new CommandItems("ls -R", "List subdirectories recursively", "ls -R /system", context));
    mCommands.add(new CommandItems("ls -s", "Print size of each file", "ls -s /system", context));
    mCommands.add(
        new CommandItems("mkdir <file_path>", "Create a directory", "mkdir /sdcard/abc", context));
    mCommands.add(
        new CommandItems(
            "mv <from> <to>",
            "Move a file or directory",
            "mv /system/app /sdcard\n\nmv /system/app /sdcard/abc",
            context));
    mCommands.add(new CommandItems("netstat", "List TCP connectivity", "netstat", context));
    mCommands.add(new CommandItems("ping", "Test a network connection", "ping", context));
    mCommands.add(
        new CommandItems(
            "pm clear <package>",
            "Delete all data associated with an app",
            "pm clear com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm clear --cache-only <package>",
            "Only clear cache data associated with an app",
            "pm clear --cache-only com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm clear --user <user_id> <package>",
            "Only delete the data associated with a given user",
            "pm clear --user 0 com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm disable <package/component>",
            "Disable a given package or component (written as package/class",
            "pm disable com.android.package/com.android.package.exampleActivity",
            context));
    mCommands.add(
        new CommandItems(
            "pm dump <package>", "List info of one app", "pm dump com.android.package", context));
    mCommands.add(
        new CommandItems(
            "pm dump package packages",
            "List info of all apps",
            "pm dump package packages",
            context));
    mCommands.add(
        new CommandItems(
            "pm enable <package/component>",
            "Enable a given package or component (written as package/class",
            "pm enable com.android.package/com.android.package.exampleActivity",
            context));
    mCommands.add(
        new CommandItems(
            "pm grant <package> <Permission>",
            "Grant a permission to an app",
            "pm grant com.android.package android.permission.WRITE_EXTERNAL_STORAGE",
            context));
    mCommands.add(
        new CommandItems(
            "pm hide <package>",
            "Hide an app from launcher",
            "pm hide com.android.package",
            context));

    mCommands.add(
        new CommandItems(
            "pm install <apk_path>",
            "Install an apk file",
            "pm install /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install -d <apk_path>",
            "Allow version code downgrade (debuggable packages only)",
            "pm install -d /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install -f <apk_path>",
            "Install application on internal flash",
            "pm install -f /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install -g <apk_path>",
            "Grant all runtime permissions",
            "pm install -g /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install -i <installer> <apk_path>",
            "Specify package name of installer owning the app",
            "pm install -i com.google.android.packageinstaller /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install -p <split_apk_path>",
            "Partial application install (new split on top of existing pkg)",
            "pm install -p /data/local/tmp/base.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install -R <apk_path>",
            "Update an existing apps, but disallow replacement of existing one",
            "pm install -R /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install -t <apk_path>",
            "Allow installing test packages",
            "pm install -t /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --abi <apk_path>",
            "Override the default ABI of the platform",
            "pm install --abi /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --dont-kill <apk_path>",
            "Install a new feature split without killing the running app",
            "pm install --dont-kill /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --full <apk_path>",
            "Cause the app to be installed as a non-ephemeral full app",
            "pm install --full /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --install-location <location> <apk_path>",
            "Force the install location (Options, 0=auto, 1=internal only, 2=prefer external)",
            "pm install --install-location 1 /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --install-reason <reason> <apk_path>",
            "Indicates why the app is being installed (Options, 0=unknown, 1=admin policy, 2=device restore,3=device setup, 4=user request)",
            "install --install-reason 2 /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --instant <apk_path>",
            "Cause the app to be installed as an ephemeral install app",
            "pm install --instant /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --restrict-permissions <apk_path>",
            "Don't whitelist restricted permissions at install",
            "pm install --restrict-permissions /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --pkg <package> <apk_path>",
            "Specify expected package name of app being installed",
            "pm install --pkg in.hridayan.ashell /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --user <user_id> <apk_path>",
            "Install for a given user",
            "pm install --user0 /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-abandon <session_id>",
            "Delete the given active install session",
            "pm install-abandon 01234567",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-commit <session_id>",
            "Commit the given active install session, installing the app",
            "pm install-commit 01234567",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-create", "Create an install session", "pm install-create", context));
    mCommands.add(
        new CommandItems(
            "pm install-existing",
            "Installs an existing application for a new user",
            "pm install-existing com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --full <package>",
            "Install as a full app",
            "pm install-existing --full com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --instant <package>",
            "Install as an instant app",
            "pm install-existing --instant com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --restrict-permissions <package>",
            "Don't whitelist restricted permissions",
            "pm install-existing --restrict-permissions com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --user <user_id> <package>",
            "Install for a given user",
            "pm install-existing --user 0 com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --wait <package>",
            "Wait until the package is installed",
            "pm install-existing --wait com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-remove <session_id> <split_name>",
            "Mark SPLIT(s) as removed in the given install session",
            "pm install-remove 01234567 /data/local/tmp/base.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-write <size> <session_id> <split_name> <split_path>",
            "Write an apk into the given install session",
            "pm install-write 123 01234567 base.apk /data/local/tmp/base.apk",
            context));
    mCommands.add(
        new CommandItems("pm list features", "List phone features", "pm list features", context));
    mCommands.add(
        new CommandItems(
            "pm list libraries", "List all system libraries", "pm list libraries", context));
    mCommands.add(
        new CommandItems("pm list packages", "List package names", "pm list packages", context));
    mCommands.add(
        new CommandItems(
            "pm list packages -3",
            "List only third party packages",
            "pm list packages -3",
            context));
    mCommands.add(
        new CommandItems(
            "pm list packages -a",
            "List all known packages, but excluding APEXes",
            "pm list packages -a",
            context));
    mCommands.add(
        new CommandItems(
            "pm list packages -d", "List all disabled packages", "pm list packages -d", context));
    mCommands.add(
        new CommandItems(
            "pm list packages -e", "List all enabled packages", "pm list packages -e", context));
    mCommands.add(
        new CommandItems(
            "pm list packages -f",
            "List package names along with their associated file",
            "pm list packages -f",
            context));
    mCommands.add(
        new CommandItems(
            "pm list packages -i",
            "List package names along with their installer",
            "pm list packages -i",
            context));
    mCommands.add(
        new CommandItems(
            "pm list packages -s", "List only system packages", "pm list packages -s", context));
    mCommands.add(
        new CommandItems(
            "pm list packages --show-versioncode",
            "List package names along with their version code",
            "pm list packages --show-versioncode",
            context));
    mCommands.add(
        new CommandItems(
            "pm list packages -u",
            "List package names of all apps including the uninstalled ones",
            "pm list packages -u",
            context));
    mCommands.add(
        new CommandItems(
            "pm list packages -U",
            "List package names along with their package UID",
            "pm list packages -U",
            context));
    mCommands.add(
        new CommandItems(
            "pm list permissions",
            "Print all known permission groups",
            "pm list permissions",
            context));
    mCommands.add(
        new CommandItems(
            "pm list permissions -d",
            "Print only dangerous permissions",
            "pm list permissions -d",
            context));
    mCommands.add(
        new CommandItems(
            "pm list permissions -f",
            "Print all information about the known permissions",
            "pm list permissions -f",
            context));
    mCommands.add(
        new CommandItems(
            "pm list permissions -g",
            "Organize all known permission by group",
            "pm list permissions -g",
            context));
    mCommands.add(
        new CommandItems(
            "pm list permissions -s",
            "Print a short summary about the known permissions",
            "pm list permissions -s",
            context));
    mCommands.add(
        new CommandItems(
            "pm list permissions -u",
            "Print only permissions that users will see",
            "pm list permissions -u",
            context));
    mCommands.add(
        new CommandItems("pm list users", "List all user names", "pm list users", context));
    mCommands.add(
        new CommandItems(
            "pm path <package>",
            "Show apk file path of an app",
            "pm path com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm revoke <package> <Permission>",
            "Revoke a permission from an app",
            "pm revoke com.android.package android.permission.WRITE_EXTERNAL_STORAGE",
            context));
    mCommands.add(
        new CommandItems(
            "pm reset-permissions -p <package>",
            "Reset permissions of an app",
            "pm reset-permissions -p com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm suspend <package>",
            "Suspend an app. You won't be able to use a suspended app until you unsuspend it!",
            "pm suspend com.android.package",
            context));

    mCommands.add(
        new CommandItems(
            "pm unhide <package>",
            "Unhide an app from launcher",
            "pm unhide com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm uninstall <package>",
            "Uninstall an app",
            "pm uninstall com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm uninstall -k <package>",
            "Uninstall an app but keep the data and cache untouched",
            "pm uninstall -k com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm uninstall --user <user_id> <package>",
            "Uninstall an app from a given user",
            "pm uninstall --user 0 com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm uninstall --versionCode <version_code> <package>",
            "Only uninstall if the app has the given version code",
            "pm uninstall --versionCode 123 com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm uninstall-system-updates",
            "Removes updates to all system applications and falls back to its system version",
            "pm uninstall-system-updates",
            context));
    mCommands.add(
        new CommandItems(
            "pm uninstall-system-updates <package>",
            "Removes updates to a given system application and falls back to its system version",
            "pm uninstall-system-updates com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm unsuspend <package>",
            "Unsuspend an app.",
            "pm unsuspend com.android.package",
            context));
    mCommands.add(new CommandItems("ps", "Print process status", "ps", context));
    mCommands.add(new CommandItems("pwd", "Print current working directory", "pwd", context));
    mCommands.add(new CommandItems("reboot", "Reboot device", "reboot", context));
    mCommands.add(new CommandItems("reboot -p", "Shutdown device", "reboot -p", context));
    mCommands.add(
        new CommandItems(
            "reboot recovery", "Reboot device into recovery mode", "reboot recovery", context));
    mCommands.add(
        new CommandItems(
            "reboot fastboot", "Reboot device into fastboot", "reboot fastboot", context));
    mCommands.add(
        new CommandItems(
            "reboot bootloader", "Reboot device into bootloader", "reboot bootloader", context));
    mCommands.add(
        new CommandItems("rm <file_path>", "Delete a file", "rm /sdcard/example.txt", context));
    mCommands.add(
        new CommandItems(
            "rm -r <file_path>", "Delete a file or directory", "rm -r /sdcard/abc", context));
    mCommands.add(new CommandItems("service list", "List all services", "service list", context));
    mCommands.add(
        new CommandItems("sleep <second>", "Delay for a specified time", "sleep 5", context));
    mCommands.add(
        new CommandItems("sync", "Synchronize data on disk with memory", "sync", context));
    mCommands.add(new CommandItems("top", "List processes running on the system", "top", context));
    mCommands.add(
        new CommandItems(
            "top -n <number>", "Update display <number> times, then exit", "top -n1", context));
    mCommands.add(
        new CommandItems("whoami", "Print the current user id and name", "whoami", context));
    mCommands.add(
        new CommandItems("wm density", "Displays current screen density", "wm density", context));
    mCommands.add(
        new CommandItems(
            "wm density reset", "Reset screen density to default", "wm density reset", context));
    mCommands.add(
        new CommandItems("wm size", "Displays the current screen resolution", "wm size", context));
    mCommands.add(
        new CommandItems(
            "wm size reset", "Reset screen resolution to default", "wm size reset", context));

    return mCommands;
  }

  public static List<CommandItems> getCommand(String command, Context context) {
    List<CommandItems> mCommands = new ArrayList<>();
    for (CommandItems commands : commandList(context)) {
      if (commands.getTitle().startsWith(command)) {
        mCommands.add(commands);
      }
    }
    return mCommands;
  }

  public static List<CommandItems> getPackageInfo(String command, Context context) {
    loadPackageInfo(context);
    List<CommandItems> mCommands = new ArrayList<>();
    for (CommandItems packages : mPackages) {
      if (packages.getTitle().startsWith(command)) {
        mCommands.add(packages);
      }
    }
    return mCommands;
  }

  public static void loadPackageInfo(Context context) {
    mPackages = new ArrayList<>();

    try {
      Process mProcess = Runtime.getRuntime().exec("pm list packages");
      BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
      String line;
      while ((line = mInput.readLine()) != null) {
        if (line.startsWith("package:")) {
          mPackages.add(new CommandItems(line.replace("package:", ""), null, null, context));
        }
      }
      mProcess.waitFor();
    } catch (Exception ignored) {
    }
  }
}
