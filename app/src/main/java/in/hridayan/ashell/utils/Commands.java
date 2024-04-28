package in.hridayan.ashell.utils;

import android.content.Context;
import commands.R;
import in.hridayan.ashell.utils.Preferences;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Commands {
  private static List<CommandItems> mPackages = null;

  public static List<CommandItems> commandList(Context context) {
    List<CommandItems> mCommands = new ArrayList<>();
    mCommands.add(
        new CommandItems("am force-stop <package>", "am force-stop com.android.package", context));
    mCommands.add(new CommandItems("am kill <package>", "am kill com.android.package", context));
    mCommands.add(new CommandItems("am kill-all", "am kill-all", context));
    mCommands.add(new CommandItems("cat <file_path>", "cat /system/build.prop", context));
    mCommands.add(new CommandItems("cd <directory_path>", "cd /sdcard", context));
    mCommands.add(new CommandItems("cd /", "cd /", context));
    mCommands.add(new CommandItems("cd ~", "cd ~", context));
    mCommands.add(new CommandItems("cd ..", "cd ..", context));
    mCommands.add(new CommandItems("cd -", "cd -", context));

    mCommands.add(new CommandItems("clear", "clear", context));
    mCommands.add(
        new CommandItems(
            "cp <from> <to>",
            "cp /system/build.prop /sdcard\nor\ncp /system/build.prop /sdcard/build_prop-backup.txt",
            context));
    mCommands.add(
        new CommandItems(
            "cp -r <from> <to>",
            "cp -r /system/app /sdcard\n\ncp -r /system/app /sdcard/abc",
            context));

    mCommands.add(new CommandItems("du -h", "du -h", context));
    mCommands.add(new CommandItems("dumpsys activity", "dumpsys activity", context));
    mCommands.add(new CommandItems("dumpsys battery", "dumpsys battery", context));
    mCommands.add(
        new CommandItems("dumpsys battery set level <n>", "dumpsys battery set level 95", context));

    mCommands.add(
        new CommandItems(
            "dumpsys battery set status <n>", "dumpsys battery set status 0", context));
    mCommands.add(new CommandItems("dumpsys battery reset", "dumpsys battery reset", context));
    mCommands.add(new CommandItems("dumpsys display", "dumpsys display", context));
    mCommands.add(new CommandItems("echo <message>", "echo Hello World", context));
    mCommands.add(new CommandItems("exit", "exit", context));
    mCommands.add(new CommandItems("file <file_path>", "file /system/build.prop", context));
    mCommands.add(new CommandItems("goto top", "goto top", context));

    mCommands.add(new CommandItems("goto bottom", "goto bottom", context));

    mCommands.add(new CommandItems("grep", "grep", context));
    mCommands.add(new CommandItems("kill <pid>", "kill <pid>", context));
    mCommands.add(new CommandItems("logcat", "logcat", context));
    mCommands.add(new CommandItems("logcat -g", "logcat -g", context));
    mCommands.add(new CommandItems("logcat -G <size>", "logcat -G 1M", context));
    mCommands.add(new CommandItems("logcat -c", "logcat -c", context));
    mCommands.add(new CommandItems("ls", "ls /system", context));
    mCommands.add(new CommandItems("ls -R", "ls -R /system", context));
    mCommands.add(new CommandItems("ls -s", "ls -s /system", context));
    mCommands.add(new CommandItems("mkdir <file_path>", "mkdir /sdcard/abc", context));
    mCommands.add(
        new CommandItems(
            "mv <from> <to>", "mv /system/app /sdcard\n\nmv /system/app /sdcard/abc", context));
    mCommands.add(new CommandItems("netstat", "netstat", context));
    mCommands.add(new CommandItems("ping", "ping", context));
    mCommands.add(new CommandItems("pm clear <package>", "pm clear com.android.package", context));

    mCommands.add(
        new CommandItems(
            "pm clear --cache-only <package>",
            "pm clear --cache-only com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm clear --user <user_id> <package>",
            "pm clear --user 0 com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm disable <package/component>",
            "pm disable com.android.package/com.android.package.exampleActivity",
            context));
    mCommands.add(new CommandItems("pm dump <package>", "pm dump com.android.package", context));
    mCommands.add(
        new CommandItems("pm dump package packages", "pm dump package packages", context));

    mCommands.add(
        new CommandItems(
            "pm enable <package/component>",
            "pm enable com.android.package/com.android.package.exampleActivity",
            context));
    mCommands.add(
        new CommandItems(
            "pm grant <package> <Permission>",
            "pm grant com.android.package android.permission.WRITE_EXTERNAL_STORAGE",
            context));
    mCommands.add(new CommandItems("pm hide <package>", "pm hide com.android.package", context));

    mCommands.add(
        new CommandItems(
            "pm install <apk_path>", "pm install /data/local/tmp/aShellYou.apk", context));
    mCommands.add(
        new CommandItems(
            "pm install -d <apk_path>", "pm install -d /data/local/tmp/aShellYou.apk", context));
    mCommands.add(
        new CommandItems(
            "pm install -f <apk_path>", "pm install -f /data/local/tmp/aShellYou.apk", context));
    mCommands.add(
        new CommandItems(
            "pm install -g <apk_path>", "pm install -g /data/local/tmp/aShellYou.apk", context));
    mCommands.add(
        new CommandItems(
            "pm install -i <installer> <apk_path>",
            "pm install -i com.google.android.packageinstaller /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install -p <split_apk_path>", "pm install -p /data/local/tmp/base.apk", context));
    mCommands.add(
        new CommandItems(
            "pm install -R <apk_path>", "pm install -R /data/local/tmp/aShellYou.apk", context));
    mCommands.add(
        new CommandItems(
            "pm install -t <apk_path>", "pm install -t /data/local/tmp/aShellYou.apk", context));
    mCommands.add(
        new CommandItems(
            "pm install --abi <apk_path>",
            "pm install --abi /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --dont-kill <apk_path>",
            "pm install --dont-kill /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --full <apk_path>",
            "pm install --full /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --install-location <location> <apk_path>",
            "pm install --install-location 1 /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --install-reason <reason> <apk_path>",
            "install --install-reason 2 /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --instant <apk_path>",
            "pm install --instant /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --restrict-permissions <apk_path>",
            "pm install --restrict-permissions /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --pkg <package> <apk_path>",
            "pm install --pkg in.hridayan.ashell /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install --user <user_id> <apk_path>",
            "pm install --user0 /data/local/tmp/aShellYou.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-abandon <session_id>", "pm install-abandon 01234567", context));
    mCommands.add(
        new CommandItems("pm install-commit <session_id>", "pm install-commit 01234567", context));

    mCommands.add(new CommandItems("pm install-create", "pm install-create", context));
    mCommands.add(
        new CommandItems(
            "pm install-existing", "pm install-existing com.android.package", context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --full <package>",
            "pm install-existing --full com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --instant <package>",
            "pm install-existing --instant com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --restrict-permissions <package>",
            "pm install-existing --restrict-permissions com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --user <user_id> <package>",
            "pm install-existing --user 0 com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-existing --wait <package>",
            "pm install-existing --wait com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-remove <session_id> <split_name>",
            "pm install-remove 01234567 /data/local/tmp/base.apk",
            context));
    mCommands.add(
        new CommandItems(
            "pm install-write <size> <session_id> <split_name> <split_path>",
            "pm install-write 123 01234567 base.apk /data/local/tmp/base.apk",
            context));
    mCommands.add(new CommandItems("pm list features", "pm list features", context));
    mCommands.add(new CommandItems("pm list libraries", "pm list libraries", context));
    mCommands.add(new CommandItems("pm list packages", "pm list packages", context));
    mCommands.add(new CommandItems("pm list packages -3", "pm list packages -3", context));

    mCommands.add(new CommandItems("pm list packages -a", "pm list packages -a", context));

    mCommands.add(new CommandItems("pm list packages -d", "pm list packages -d", context));
    mCommands.add(new CommandItems("pm list packages -e", "pm list packages -e", context));
    mCommands.add(new CommandItems("pm list packages -f", "pm list packages -f", context));

    mCommands.add(new CommandItems("pm list packages -i", "pm list packages -i", context));

    mCommands.add(new CommandItems("pm list packages -s", "pm list packages -s", context));
    mCommands.add(
        new CommandItems(
            "pm list packages --show-versioncode", "pm list packages --show-versioncode", context));
    mCommands.add(new CommandItems("pm list packages -u", "pm list packages -u", context));

    mCommands.add(new CommandItems("pm list packages -U", "pm list packages -U", context));

    mCommands.add(new CommandItems("pm list permissions", "pm list permissions", context));

    mCommands.add(new CommandItems("pm list permissions -d", "pm list permissions -d", context));

    mCommands.add(new CommandItems("pm list permissions -f", "pm list permissions -f", context));

    mCommands.add(new CommandItems("pm list permissions -g", "pm list permissions -g", context));
    mCommands.add(new CommandItems("pm list permissions -s", "pm list permissions -s", context));

    mCommands.add(new CommandItems("pm list permissions -u", "pm list permissions -u", context));

    mCommands.add(new CommandItems("pm list users", "pm list users", context));
    mCommands.add(new CommandItems("pm path <package>", "pm path com.android.package", context));

    mCommands.add(
        new CommandItems(
            "pm revoke <package> <Permission>",
            "pm revoke com.android.package android.permission.WRITE_EXTERNAL_STORAGE",
            context));
    mCommands.add(
        new CommandItems(
            "pm reset-permissions -p <package>",
            "pm reset-permissions -p com.android.package",
            context));
    mCommands.add(
        new CommandItems("pm suspend <package>", "pm suspend com.android.package", context));

    mCommands.add(
        new CommandItems("pm unhide <package>", "pm unhide com.android.package", context));

    mCommands.add(
        new CommandItems("pm uninstall <package>", "pm uninstall com.android.package", context));

    mCommands.add(
        new CommandItems(
            "pm uninstall -k <package>", "pm uninstall -k com.android.package", context));
    mCommands.add(
        new CommandItems(
            "pm uninstall --user <user_id> <package>",
            "pm uninstall --user 0 com.android.package",
            context));
    mCommands.add(
        new CommandItems(
            "pm uninstall --versionCode <version_code> <package>",
            "pm uninstall --versionCode 123 com.android.package",
            context));

    mCommands.add(
        new CommandItems(
            "pm uninstall-system-updates <package>",
            "pm uninstall-system-updates com.android.package",
            context));
    mCommands.add(
        new CommandItems("pm unsuspend <package>", "pm unsuspend com.android.package", context));

    mCommands.add(new CommandItems("ps", "ps", context));
    mCommands.add(new CommandItems("pwd", "pwd", context));
    mCommands.add(new CommandItems("reboot", "reboot", context));
    mCommands.add(new CommandItems("reboot -p", "reboot -p", context));
    mCommands.add(new CommandItems("reboot recovery", "reboot recovery", context));
    mCommands.add(new CommandItems("reboot fastboot", "reboot fastboot", context));
    mCommands.add(new CommandItems("reboot bootloader", "reboot bootloader", context));
    mCommands.add(new CommandItems("rm <file_path>", "rm /sdcard/example.txt", context));
    mCommands.add(new CommandItems("rm -r <file_path>", "rm -r /sdcard/abc", context));
    mCommands.add(new CommandItems("service list", "service list", context));
    mCommands.add(new CommandItems("sleep <second>", "sleep 5", context));
    mCommands.add(new CommandItems("sync", "sync", context));
    mCommands.add(new CommandItems("top", "top", context));
    mCommands.add(new CommandItems("top -n <number>", "top -n1", context));
    mCommands.add(new CommandItems("whoami", "whoami", context));
    mCommands.add(new CommandItems("wm density", "wm density", context));
    mCommands.add(new CommandItems("wm density reset", "wm density reset", context));
    mCommands.add(new CommandItems("wm size", "wm size", context));
    mCommands.add(new CommandItems("wm size reset", "wm size reset", context));

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
          mPackages.add(new CommandItems(line.replace("package:", ""), null, context));
        }
      }
      mProcess.waitFor();
    } catch (Exception ignored) {
    }
  }
}
