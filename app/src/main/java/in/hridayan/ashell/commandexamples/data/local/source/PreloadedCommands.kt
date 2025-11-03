package `in`.hridayan.ashell.commandexamples.data.local.source

import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity

/**
 *@param preloadedCommands This is a predefined list of commands with their description and labels
 */
val preloadedCommands = listOf(
    CommandEntity(
        command = "am force-stop <package>",
        description = "Force stops a specific package, terminating its processes.",
        labels = listOf("package", "process", "kill")
    ),
    CommandEntity(
        command = "am kill <package>",
        description = "Kills the background processes of a specific package.",
        labels = listOf("package", "process", "kill")
    ),
    CommandEntity(
        command = "am kill-all",
        description = "Kills all background processes.",
        labels = listOf("process", "kill")
    ),
    CommandEntity(
        command = "cat <file_path>",
        description = "Displays the contents of a file.",
        labels = listOf("file", "read")
    ),
    CommandEntity(
        command = "cd <directory_path>",
        description = "Changes the current directory to the specified path.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "cd /",
        description = "Changes the current directory to the root directory.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "cd ~",
        description = "Changes the current directory to the home directory.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "cd ..",
        description = "Moves up one directory level.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "cd -",
        description = "Changes to the previous directory.",
        labels = listOf("directory", "navigation")
    ),
    CommandEntity(
        command = "clear",
        description = "Clears the terminal screen.",
        labels = listOf("terminal", "utility")
    ),
    CommandEntity(
        command = "cmd uimode night no",
        description = "Disables night mode in the system UI.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "cmd uimode night yes",
        description = "Enables night mode in the system UI.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "cp <from> <to>",
        description = "Copies a file or directory from one location to another.",
        labels = listOf("file", "copy")
    ),
    CommandEntity(
        command = "cp -r <from> <to>",
        description = "Recursively copies a directory and its contents.",
        labels = listOf("directory", "copy", "recursive")
    ),
    CommandEntity(
        command = "date",
        description = "Displays the current date and time.",
        labels = listOf("system", "time")
    ),
    CommandEntity(
        command = "df -h /system",
        description = "Displays disk usage information for the /system partition in a human-readable format.",
        labels = listOf("system", "disk")
    ),
    CommandEntity(
        command = "dmesg",
        description = "Displays kernel messages.",
        labels = listOf("system", "kernel", "log")
    ),
    CommandEntity(
        command = "du -h",
        description = "Displays disk usage of files and directories in a human-readable format.",
        labels = listOf("file", "disk")
    ),
    CommandEntity(
        command = "du -sh /system/*",
        description = "Displays a summary of disk usage for files and directories in /system.",
        labels = listOf("system", "disk")
    ),
    CommandEntity(
        command = "dumpsys activity",
        description = "Displays information about activities and their states.",
        labels = listOf("system", "activity", "process")
    ),
    CommandEntity(
        command = "dumpsys battery",
        description = "Displays battery status and information.",
        labels = listOf("system", "battery")
    ),
    CommandEntity(
        command = "dumpsys battery set level <n>",
        description = "Sets the battery level to a specific value (for testing purposes).",
        labels = listOf("system", "battery", "testing")
    ),
    CommandEntity(
        command = "dumpsys battery set status <n>",
        description = "Sets the battery status to a specific value (for testing purposes).",
        labels = listOf("system", "battery", "testing")
    ),
    CommandEntity(
        command = "dumpsys battery reset",
        description = "Resets the battery statistics.",
        labels = listOf("system", "battery")
    ),
    CommandEntity(
        command = "dumpsys display",
        description = "Displays information about the display system.",
        labels = listOf("system", "display")
    ),
    CommandEntity(
        command = "echo <message>",
        description = "Prints a message to the terminal.",
        labels = listOf("terminal", "utility", "text")
    ),
    CommandEntity(
        command = "exit",
        description = "Exits the current shell session.",
        labels = listOf("terminal", "session")
    ),
    CommandEntity(
        command = "file <file_path>",
        description = "Determines the type of a file.",
        labels = listOf("file", "utility")
    ),
    CommandEntity(
        command = "getenforce",
        description = "Displays the current SELinux mode (Enforcing, Permissive, or Disabled).",
        labels = listOf("system", "security")
    ),
    CommandEntity(
        command = "grep",
        description = "Searches for a pattern in files or input.",
        labels = listOf("file", "search", "utility")
    ),
    CommandEntity(
        command = "iptables -L",
        description = "Lists all iptables rules.",
        labels = listOf("network", "firewall", "system")
    ),
    CommandEntity(
        command = "kill <pid>",
        description = "Terminates a process with the specified process ID.",
        labels = listOf("process", "kill")
    ),
    CommandEntity(
        command = "logcat",
        description = "Displays system logs.",
        labels = listOf("system", "log")
    ),
    CommandEntity(
        command = "logcat -g",
        description = "Displays the size of the log buffer.",
        labels = listOf("system", "log")
    ),
    CommandEntity(
        command = "logcat -G <size>",
        description = "Sets the size of the log buffer.",
        labels = listOf("system", "log")
    ),
    CommandEntity(
        command = "logcat -c",
        description = "Clears the log buffer.",
        labels = listOf("system", "log", "utility")
    ),
    CommandEntity(
        command = "ls",
        description = "Lists files and directories in the specified path.",
        labels = listOf("file", "directory")
    ),
    CommandEntity(
        command = "ls -R",
        description = "Recursively lists files and directories.",
        labels = listOf("file", "directory", "recursive")
    ),
    CommandEntity(
        command = "ls -s",
        description = "Lists files and directories with their sizes.",
        labels = listOf("file", "directory", "size")
    ),
    CommandEntity(
        command = "mkdir <file_path>",
        description = "Creates a new directory.",
        labels = listOf("directory", "create")
    ),
    CommandEntity(
        command = "mv <from> <to>",
        description = "Moves or renames a file or directory.",
        labels = listOf("file", "directory", "move")
    ),
    CommandEntity(
        command = "netstat",
        description = "Displays network connections and statistics.",
        labels = listOf("network", "utility")
    ),
    CommandEntity(
        command = "ping",
        description = "Tests network connectivity to a host.",
        labels = listOf("network", "utility", "test")
    ),
    CommandEntity(
        command = "pm clear <package>",
        description = "Clears the data and cache of a specific package.",
        labels = listOf("package", "data", "cache")
    ),
    CommandEntity(
        command = "pm disable <package/component>",
        description = "Disables a specific component of a package.",
        labels = listOf("package", "disable")
    ),
    CommandEntity(
        command = "pm enable <package/component>",
        description = "Enables a specific component of a package.",
        labels = listOf("package", "enable")
    ),
    CommandEntity(
        command = "pm grant <package> <Permission>",
        description = "Grants a specific permission to a package.",
        labels = listOf("package", "permission")
    ),
    CommandEntity(
        command = "pm revoke <package> <Permission>",
        description = "Revokes a specific permission from a package.",
        labels = listOf("package", "permission")
    ),
    CommandEntity(
        command = "pm list packages",
        description = "Lists all installed packages.",
        labels = listOf("package", "list")
    ),
    CommandEntity(
        command = "pm list permissions",
        description = "Lists all permissions defined on the device.",
        labels = listOf("permission", "list")
    ),
    CommandEntity(
        command = "ps",
        description = "Displays information about running processes.",
        labels = listOf("process", "system")
    ),
    CommandEntity(
        command = "pwd",
        description = "Displays the current working directory.",
        labels = listOf("directory", "system")
    ),
    CommandEntity(
        command = "reboot",
        description = "Reboots the device.",
        labels = listOf("system", "reboot")
    ),
    CommandEntity(
        command = "rm <file_path>",
        description = "Deletes a file.",
        labels = listOf("file", "delete")
    ),
    CommandEntity(
        command = "rmdir <directory_path>",
        description = "Deletes an empty directory.",
        labels = listOf("directory", "delete")
    ),
    CommandEntity(
        command = "settings get <namespace> <key>",
        description = "Gets the value of a specific system setting.",
        labels = listOf("system", "settings")
    ),
    CommandEntity(
        command = "settings put <namespace> <key> <value>",
        description = "Sets the value of a specific system setting.",
        labels = listOf("system", "settings")
    ),
    CommandEntity(
        command = "settings delete <namespace> <key>",
        description = "Deletes a specific system setting.",
        labels = listOf("system", "settings")
    ),
    CommandEntity(
        command = "sleep <seconds>",
        description = "Pauses execution for a specified number of seconds.",
        labels = listOf("utility", "delay")
    ),
    CommandEntity(
        command = "stat <file_path>",
        description = "Displays detailed status information about a file.",
        labels = listOf("file", "utility")
    ),
    CommandEntity(
        command = "su",
        description = "Switches to superuser mode (root).",
        labels = listOf("system", "root")
    ),
    CommandEntity(
        command = "top",
        description = "Displays running processes in real-time.",
        labels = listOf("process", "system", "monitor")
    ),
    CommandEntity(
        command = "touch <file_path>",
        description = "Creates a new empty file or updates the modification timestamp.",
        labels = listOf("file", "create", "update")
    ),
    CommandEntity(
        command = "umount <mount_point>",
        description = "Unmounts a filesystem.",
        labels = listOf("system", "filesystem")
    ),
    CommandEntity(
        command = "uname -a",
        description = "Displays system information.",
        labels = listOf("system", "info")
    ),
    CommandEntity(
        command = "whoami",
        description = "Displays the current user.",
        labels = listOf("system", "user")
    ),
    CommandEntity(
        command = "wm density <dpi>",
        description = "Sets the screen density.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "wm size <width>x<height>",
        description = "Sets the screen resolution.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "wm overscan <left>,<top>,<right>,<bottom>",
        description = "Sets overscan for the display.",
        labels = listOf("system", "display", "ui")
    )
)
