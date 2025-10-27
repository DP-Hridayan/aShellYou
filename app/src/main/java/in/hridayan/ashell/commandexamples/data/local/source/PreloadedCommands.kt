package `in`.hridayan.ashell.commandexamples.data.local.source

import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity

/**
 *@param preloadedCommands This is a predefined list of commands with their description and labels
 */
val preloadedCommands = listOf(
    CommandEntity(
        command = "adb shell am force-stop <package>",
        description = "Force stops a specific package, terminating its processes.",
        labels = listOf("package", "process", "kill")
    ),
    CommandEntity(
        command = "adb shell am kill <package>",
        description = "Kills the background processes of a specific package.",
        labels = listOf("package", "process", "kill")
    ),
    CommandEntity(
        command = "adb shell am kill-all",
        description = "Kills all background processes.",
        labels = listOf("process", "kill")
    ),
    CommandEntity(
        command = "adb shell cat <file_path>",
        description = "Displays the contents of a file.",
        labels = listOf("file", "read")
    ),
    CommandEntity(
        command = "adb shell cd <directory_path>",
        description = "Changes the current directory to the specified path.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "adb shell cd /",
        description = "Changes the current directory to the root directory.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "adb shell cd ~",
        description = "Changes the current directory to the home directory.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "adb shell cd ..",
        description = "Moves up one directory level.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "adb shell cd -",
        description = "Changes to the previous directory.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "adb shell clear",
        description = "Clears the terminal screen.",
        labels = listOf("terminal", "utility")
    ),
    CommandEntity(
        command = "adb shell cmd uimode night no",
        description = "Disables night mode in the system UI.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "adb shell cmd uimode night yes",
        description = "Enables night mode in the system UI.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "adb shell cp <from> <to>",
        description = "Copies a file or directory from one location to another.",
        labels = listOf("file", "copy")
    ),
    CommandEntity(
        command = "adb shell cp -r <from> <to>",
        description = "Recursively copies a directory and its contents.",
        labels = listOf("directory", "copy", "recursive")
    ),
    CommandEntity(
        command = "adb shell date",
        description = "Displays the current date and time.",
        labels = listOf("system", "time")
    ),
    CommandEntity(
        command = "adb shell df -h /system",
        description = "Displays disk usage information for the /system partition in a human-readable format.",
        labels = listOf("system", "disk")
    ),
    CommandEntity(
        command = "adb shell dmesg",
        description = "Displays kernel messages.",
        labels = listOf("system", "kernel", "log")
    ),
    CommandEntity(
        command = "adb shell du -h",
        description = "Displays disk usage of files and directories in a human-readable format.",
        labels = listOf("file", "disk")
    ),
    CommandEntity(
        command = "adb shell du -sh /system/*",
        description = "Displays a summary of disk usage for files and directories in /system.",
        labels = listOf("system", "disk")
    ),
    CommandEntity(
        command = "adb shell dumpsys activity",
        description = "Displays information about activities and their states.",
        labels = listOf("system", "activity", "process")
    ),
    CommandEntity(
        command = "adb shell dumpsys battery",
        description = "Displays battery status and information.",
        labels = listOf("system", "battery")
    ),
    CommandEntity(
        command = "adb shell dumpsys battery set level <n>",
        description = "Sets the battery level to a specific value (for testing purposes).",
        labels = listOf("system", "battery", "testing")
    ),
    CommandEntity(
        command = "adb shell dumpsys battery set status <n>",
        description = "Sets the battery status to a specific value (for testing purposes).",
        labels = listOf("system", "battery", "testing")
    ),
    CommandEntity(
        command = "adb shell dumpsys battery reset",
        description = "Resets the battery statistics.",
        labels = listOf("system", "battery")
    ),
    CommandEntity(
        command = "adb shell dumpsys display",
        description = "Displays information about the display system.",
        labels = listOf("system", "display")
    ),
    CommandEntity(
        command = "adb shell echo <message>",
        description = "Prints a message to the terminal.",
        labels = listOf("terminal", "utility", "text")
    ),
    CommandEntity(
        command = "adb shell exit",
        description = "Exits the current shell session.",
        labels = listOf("terminal", "session")
    ),
    CommandEntity(
        command = "adb shell file <file_path>",
        description = "Determines the type of a file.",
        labels = listOf("file", "utility")
    ),
    CommandEntity(
        command = "adb shell getenforce",
        description = "Displays the current SELinux mode (Enforcing, Permissive, or Disabled).",
        labels = listOf("system", "security")
    ),
    CommandEntity(
        command = "adb shell grep",
        description = "Searches for a pattern in files or input.",
        labels = listOf("file", "search", "utility")
    ),
    CommandEntity(
        command = "adb shell iptables -L",
        description = "Lists all iptables rules.",
        labels = listOf("network", "firewall", "system")
    ),
    CommandEntity(
        command = "adb shell kill <pid>",
        description = "Terminates a process with the specified process ID.",
        labels = listOf("process", "kill")
    ),
    CommandEntity(
        command = "adb shell logcat",
        description = "Displays system logs.",
        labels = listOf("system", "log")
    ),
    CommandEntity(
        command = "adb shell logcat -g",
        description = "Displays the size of the log buffer.",
        labels = listOf("system", "log")
    ),
    CommandEntity(
        command = "adb shell logcat -G <size>",
        description = "Sets the size of the log buffer.",
        labels = listOf("system", "log")
    ),
    CommandEntity(
        command = "adb shell logcat -c",
        description = "Clears the log buffer.",
        labels = listOf("system", "log", "utility")
    ),
    CommandEntity(
        command = "adb shell ls",
        description = "Lists files and directories in the specified path.",
        labels = listOf("file", "directory")
    ),
    CommandEntity(
        command = "adb shell ls -R",
        description = "Recursively lists files and directories.",
        labels = listOf("file", "directory", "recursive")
    ),
    CommandEntity(
        command = "adb shell ls -s",
        description = "Lists files and directories with their sizes.",
        labels = listOf("file", "directory", "size")
    ),
    CommandEntity(
        command = "adb shell mkdir <file_path>",
        description = "Creates a new directory.",
        labels = listOf("directory", "create")
    ),
    CommandEntity(
        command = "adb shell mv <from> <to>",
        description = "Moves or renames a file or directory.",
        labels = listOf("file", "directory", "move")
    ),
    CommandEntity(
        command = "adb shell netstat",
        description = "Displays network connections and statistics.",
        labels = listOf("network", "utility")
    ),
    CommandEntity(
        command = "adb shell ping",
        description = "Tests network connectivity to a host.",
        labels = listOf("network", "utility", "test")
    ),
    CommandEntity(
        command = "adb shell pm clear <package>",
        description = "Clears the data and cache of a specific package.",
        labels = listOf("package", "data", "cache")
    ),
    CommandEntity(
        command = "adb shell pm disable <package/component>",
        description = "Disables a specific component of a package.",
        labels = listOf("package", "disable")
    ),
    CommandEntity(
        command = "adb shell pm enable <package/component>",
        description = "Enables a specific component of a package.",
        labels = listOf("package", "enable")
    ),
    CommandEntity(
        command = "adb shell pm grant <package> <Permission>",
        description = "Grants a specific permission to a package.",
        labels = listOf("package", "permission")
    ),
    CommandEntity(
        command = "adb shell pm revoke <package> <Permission>",
        description = "Revokes a specific permission from a package.",
        labels = listOf("package", "permission")
    ),
    CommandEntity(
        command = "adb shell pm list packages",
        description = "Lists all installed packages.",
        labels = listOf("package", "list")
    ),
    CommandEntity(
        command = "adb shell pm list permissions",
        description = "Lists all permissions defined on the device.",
        labels = listOf("permission", "list")
    ),
    CommandEntity(
        command = "adb shell ps",
        description = "Displays information about running processes.",
        labels = listOf("process", "system")
    ),
    CommandEntity(
        command = "adb shell pwd",
        description = "Displays the current working directory.",
        labels = listOf("directory", "system")
    ),
    CommandEntity(
        command = "adb shell reboot",
        description = "Reboots the device.",
        labels = listOf("system", "reboot")
    ),
    CommandEntity(
        command = "adb shell rm <file_path>",
        description = "Deletes a file.",
        labels = listOf("file", "delete")
    ),
    CommandEntity(
        command = "adb shell rmdir <directory_path>",
        description = "Deletes an empty directory.",
        labels = listOf("directory", "delete")
    ),
    CommandEntity(
        command = "adb shell settings get <namespace> <key>",
        description = "Gets the value of a specific system setting.",
        labels = listOf("system", "settings")
    ),
    CommandEntity(
        command = "adb shell settings put <namespace> <key> <value>",
        description = "Sets the value of a specific system setting.",
        labels = listOf("system", "settings")
    ),
    CommandEntity(
        command = "adb shell settings delete <namespace> <key>",
        description = "Deletes a specific system setting.",
        labels = listOf("system", "settings")
    ),
    CommandEntity(
        command = "adb shell sleep <seconds>",
        description = "Pauses execution for a specified number of seconds.",
        labels = listOf("utility", "delay")
    ),
    CommandEntity(
        command = "adb shell stat <file_path>",
        description = "Displays detailed status information about a file.",
        labels = listOf("file", "utility")
    ),
    CommandEntity(
        command = "adb shell su",
        description = "Switches to superuser mode (root).",
        labels = listOf("system", "root")
    ),
    CommandEntity(
        command = "adb shell top",
        description = "Displays running processes in real-time.",
        labels = listOf("process", "system", "monitor")
    ),
    CommandEntity(
        command = "adb shell touch <file_path>",
        description = "Creates a new empty file or updates the modification timestamp.",
        labels = listOf("file", "create", "update")
    ),
    CommandEntity(
        command = "adb shell umount <mount_point>",
        description = "Unmounts a filesystem.",
        labels = listOf("system", "filesystem")
    ),
    CommandEntity(
        command = "adb shell uname -a",
        description = "Displays system information.",
        labels = listOf("system", "info")
    ),
    CommandEntity(
        command = "adb shell whoami",
        description = "Displays the current user.",
        labels = listOf("system", "user")
    ),
    CommandEntity(
        command = "adb shell wm density <dpi>",
        description = "Sets the screen density.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "adb shell wm size <width>x<height>",
        description = "Sets the screen resolution.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "adb shell wm overscan <left>,<top>,<right>,<bottom>",
        description = "Sets overscan for the display.",
        labels = listOf("system", "display", "ui")
    )
)
