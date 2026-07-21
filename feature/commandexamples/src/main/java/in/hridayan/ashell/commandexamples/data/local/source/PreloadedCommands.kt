package `in`.hridayan.ashell.commandexamples.data.local.source

import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity

/**
 *@param preloadedCommands This is a predefined list of commands with their description and labels
 */
val preloadedCommands = listOf(
    CommandEntity(
        command = "am broadcast -a <action>",
        description = "Sends a broadcast intent with the specified action.",
        labels = listOf("intent", "broadcast", "system")
    ),
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
        command = "am start -n <package>/<activity>",
        description = "Starts a specific activity of an application.",
        labels = listOf("activity", "intent", "launch")
    ),
    CommandEntity(
        command = "am start -a android.intent.action.VIEW -d <uri>",
        description = "Opens a URI using the VIEW intent action.",
        labels = listOf("intent", "launch", "uri")
    ),
    CommandEntity(
        command = "am startservice <package>/<service>",
        description = "Starts a specific service of an application.",
        labels = listOf("service", "intent", "launch")
    ),
    CommandEntity(
        command = "am stopservice <package>/<service>",
        description = "Stops a specific running service of an application.",
        labels = listOf("service", "intent", "stop")
    ),
    CommandEntity(
        command = "appops get <package>",
        description = "Gets the app operations (permissions state) for a specific package.",
        labels = listOf("package", "permission", "appops")
    ),
    CommandEntity(
        command = "appops set <package> <operation> <mode>",
        description = "Sets an app operation mode (allow/deny/ignore) for a specific package.",
        labels = listOf("package", "permission", "appops")
    ),
    CommandEntity(
        command = "appops reset <package>",
        description = "Resets all app operations for a specific package to defaults.",
        labels = listOf("package", "permission", "appops")
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
        command = "cmd activity",
        description = "Interacts with the activity manager service.",
        labels = listOf("system", "activity", "service")
    ),
    CommandEntity(
        command = "cmd appops set <package> <op> <mode>",
        description = "Sets an app operation using the cmd interface.",
        labels = listOf("package", "permission", "appops")
    ),
    CommandEntity(
        command = "cmd bluetooth_manager enable",
        description = "Enables Bluetooth.",
        labels = listOf("system", "bluetooth", "connectivity")
    ),
    CommandEntity(
        command = "cmd bluetooth_manager disable",
        description = "Disables Bluetooth.",
        labels = listOf("system", "bluetooth", "connectivity")
    ),
    CommandEntity(
        command = "cmd notification",
        description = "Interacts with the notification manager service.",
        labels = listOf("system", "notification")
    ),
    CommandEntity(
        command = "cmd package compile -m speed -f <package>",
        description = "Force compiles a package with speed optimization profile.",
        labels = listOf("package", "optimize", "compile")
    ),
    CommandEntity(
        command = "cmd statusbar expand-notifications",
        description = "Expands the notification shade.",
        labels = listOf("system", "ui", "notification")
    ),
    CommandEntity(
        command = "cmd statusbar expand-settings",
        description = "Expands the quick settings panel.",
        labels = listOf("system", "ui", "settings")
    ),
    CommandEntity(
        command = "cmd statusbar collapse",
        description = "Collapses the status bar.",
        labels = listOf("system", "ui")
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
        command = "content query --uri content://settings/system",
        description = "Queries system settings using the content provider.",
        labels = listOf("system", "settings", "content")
    ),
    CommandEntity(
        command = "content insert --uri <uri> --bind <key>:<type>:<value>",
        description = "Inserts a value into a content provider.",
        labels = listOf("system", "content", "database")
    ),
    CommandEntity(
        command = "content delete --uri <uri>",
        description = "Deletes entries from a content provider.",
        labels = listOf("system", "content", "database")
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
        command = "device_config list <namespace>",
        description = "Lists all device configuration flags in a namespace.",
        labels = listOf("system", "config", "flags")
    ),
    CommandEntity(
        command = "device_config get <namespace> <key>",
        description = "Gets a specific device configuration flag value.",
        labels = listOf("system", "config", "flags")
    ),
    CommandEntity(
        command = "device_config put <namespace> <key> <value>",
        description = "Sets a device configuration flag value.",
        labels = listOf("system", "config", "flags")
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
        command = "dumpsys activity top",
        description = "Displays the currently running top activity.",
        labels = listOf("system", "activity", "process")
    ),
    CommandEntity(
        command = "dumpsys alarm",
        description = "Displays information about all pending alarms.",
        labels = listOf("system", "alarm")
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
        command = "dumpsys connectivity",
        description = "Displays information about network connectivity.",
        labels = listOf("system", "network", "connectivity")
    ),
    CommandEntity(
        command = "dumpsys cpuinfo",
        description = "Displays CPU usage information.",
        labels = listOf("system", "cpu", "performance")
    ),
    CommandEntity(
        command = "dumpsys display",
        description = "Displays information about the display system.",
        labels = listOf("system", "display")
    ),
    CommandEntity(
        command = "dumpsys input",
        description = "Displays information about input devices and events.",
        labels = listOf("system", "input", "device")
    ),
    CommandEntity(
        command = "dumpsys meminfo",
        description = "Displays memory usage information for all processes.",
        labels = listOf("system", "memory", "performance")
    ),
    CommandEntity(
        command = "dumpsys meminfo <package>",
        description = "Displays detailed memory usage for a specific package.",
        labels = listOf("system", "memory", "package")
    ),
    CommandEntity(
        command = "dumpsys netstats",
        description = "Displays network usage statistics.",
        labels = listOf("system", "network", "stats")
    ),
    CommandEntity(
        command = "dumpsys notification",
        description = "Displays information about notifications.",
        labels = listOf("system", "notification")
    ),
    CommandEntity(
        command = "dumpsys package <package>",
        description = "Displays detailed information about a specific package.",
        labels = listOf("system", "package", "info")
    ),
    CommandEntity(
        command = "dumpsys power",
        description = "Displays power management information and wake locks.",
        labels = listOf("system", "power", "battery")
    ),
    CommandEntity(
        command = "dumpsys usagestats",
        description = "Displays app usage statistics.",
        labels = listOf("system", "usage", "stats")
    ),
    CommandEntity(
        command = "dumpsys wifi",
        description = "Displays Wi-Fi status and information.",
        labels = listOf("system", "wifi", "network")
    ),
    CommandEntity(
        command = "dumpsys window",
        description = "Displays information about the window manager.",
        labels = listOf("system", "window", "display")
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
        command = "find <path> -name <pattern>",
        description = "Searches for files matching a name pattern in the specified path.",
        labels = listOf("file", "search", "utility")
    ),
    CommandEntity(
        command = "getenforce",
        description = "Displays the current SELinux mode (Enforcing, Permissive, or Disabled).",
        labels = listOf("system", "security")
    ),
    CommandEntity(
        command = "getprop",
        description = "Lists all system properties.",
        labels = listOf("system", "property", "info")
    ),
    CommandEntity(
        command = "getprop <property>",
        description = "Gets the value of a specific system property.",
        labels = listOf("system", "property", "info")
    ),
    CommandEntity(
        command = "getprop ro.build.version.sdk",
        description = "Displays the Android SDK version number.",
        labels = listOf("system", "property", "version")
    ),
    CommandEntity(
        command = "getprop ro.build.display.id",
        description = "Displays the build display ID (firmware version).",
        labels = listOf("system", "property", "version")
    ),
    CommandEntity(
        command = "getprop ro.product.model",
        description = "Displays the device model name.",
        labels = listOf("system", "property", "device")
    ),
    CommandEntity(
        command = "getprop ro.product.manufacturer",
        description = "Displays the device manufacturer.",
        labels = listOf("system", "property", "device")
    ),
    CommandEntity(
        command = "getprop ro.serialno",
        description = "Displays the device serial number.",
        labels = listOf("system", "property", "device")
    ),
    CommandEntity(
        command = "grep",
        description = "Searches for a pattern in files or input.",
        labels = listOf("file", "search", "utility")
    ),
    CommandEntity(
        command = "id",
        description = "Displays the current user ID, group ID, and security context.",
        labels = listOf("system", "user", "security")
    ),
    CommandEntity(
        command = "ifconfig",
        description = "Displays network interface configurations.",
        labels = listOf("network", "interface", "info")
    ),
    CommandEntity(
        command = "input keyevent <keycode>",
        description = "Simulates pressing a hardware key (e.g., 3=Home, 4=Back, 26=Power, 187=Recents).",
        labels = listOf("input", "key", "simulate")
    ),
    CommandEntity(
        command = "input keyevent 26",
        description = "Simulates pressing the Power button.",
        labels = listOf("input", "key", "power")
    ),
    CommandEntity(
        command = "input keyevent 3",
        description = "Simulates pressing the Home button.",
        labels = listOf("input", "key", "navigation")
    ),
    CommandEntity(
        command = "input keyevent 4",
        description = "Simulates pressing the Back button.",
        labels = listOf("input", "key", "navigation")
    ),
    CommandEntity(
        command = "input keyevent 24",
        description = "Simulates pressing Volume Up.",
        labels = listOf("input", "key", "volume")
    ),
    CommandEntity(
        command = "input keyevent 25",
        description = "Simulates pressing Volume Down.",
        labels = listOf("input", "key", "volume")
    ),
    CommandEntity(
        command = "input keyevent 187",
        description = "Simulates pressing the Recents (app switcher) button.",
        labels = listOf("input", "key", "navigation")
    ),
    CommandEntity(
        command = "input keyevent 223",
        description = "Puts the device to sleep.",
        labels = listOf("input", "key", "power")
    ),
    CommandEntity(
        command = "input keyevent 224",
        description = "Wakes the device up.",
        labels = listOf("input", "key", "power")
    ),
    CommandEntity(
        command = "input keyevent 82",
        description = "Opens the menu / locks/unlocks the device.",
        labels = listOf("input", "key", "ui")
    ),
    CommandEntity(
        command = "input tap <x> <y>",
        description = "Simulates a screen tap at the specified coordinates.",
        labels = listOf("input", "touch", "simulate")
    ),
    CommandEntity(
        command = "input swipe <x1> <y1> <x2> <y2>",
        description = "Simulates a swipe gesture from one point to another.",
        labels = listOf("input", "touch", "simulate")
    ),
    CommandEntity(
        command = "input swipe <x1> <y1> <x2> <y2> <duration_ms>",
        description = "Simulates a swipe gesture with a specified duration in milliseconds.",
        labels = listOf("input", "touch", "simulate")
    ),
    CommandEntity(
        command = "input text <text>",
        description = "Types the specified text on the focused input field.",
        labels = listOf("input", "text", "simulate")
    ),
    CommandEntity(
        command = "ip addr",
        description = "Displays IP addresses assigned to all network interfaces.",
        labels = listOf("network", "ip", "info")
    ),
    CommandEntity(
        command = "ip route",
        description = "Displays the routing table.",
        labels = listOf("network", "route", "info")
    ),
    CommandEntity(
        command = "ip rule",
        description = "Displays the routing policy rules.",
        labels = listOf("network", "route", "info")
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
        command = "ls -la",
        description = "Lists all files including hidden ones with detailed information.",
        labels = listOf("file", "directory", "detail")
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
        command = "md5sum <file_path>",
        description = "Computes the MD5 hash of a file.",
        labels = listOf("file", "hash", "utility")
    ),
    CommandEntity(
        command = "mkdir <file_path>",
        description = "Creates a new directory.",
        labels = listOf("directory", "create")
    ),
    CommandEntity(
        command = "mkdir -p <path>",
        description = "Creates a directory along with any necessary parent directories.",
        labels = listOf("directory", "create")
    ),
    CommandEntity(
        command = "monkey -p <package> -v <count>",
        description = "Generates pseudo-random user events for stress testing an app.",
        labels = listOf("testing", "package", "stress")
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
        command = "pm disable-user --user 0 <package>",
        description = "Disables a package for the current user without uninstalling it.",
        labels = listOf("package", "disable", "user")
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
        command = "pm hide <package>",
        description = "Hides a package from the launcher.",
        labels = listOf("package", "hide")
    ),
    CommandEntity(
        command = "pm unhide <package>",
        description = "Unhides a previously hidden package.",
        labels = listOf("package", "hide")
    ),
    CommandEntity(
        command = "pm install <apk_path>",
        description = "Installs an APK from the specified path.",
        labels = listOf("package", "install")
    ),
    CommandEntity(
        command = "pm install -r <apk_path>",
        description = "Reinstalls an existing app, keeping its data.",
        labels = listOf("package", "install", "update")
    ),
    CommandEntity(
        command = "pm install -d <apk_path>",
        description = "Allows version code downgrade when installing an APK.",
        labels = listOf("package", "install", "downgrade")
    ),
    CommandEntity(
        command = "pm install -g <apk_path>",
        description = "Installs an APK and grants all runtime permissions.",
        labels = listOf("package", "install", "permission")
    ),
    CommandEntity(
        command = "pm list features",
        description = "Lists all hardware and software features of the device.",
        labels = listOf("system", "feature", "list")
    ),
    CommandEntity(
        command = "pm list instrumentation",
        description = "Lists all test instrumentation packages.",
        labels = listOf("package", "testing", "list")
    ),
    CommandEntity(
        command = "pm list libraries",
        description = "Lists all shared libraries on the device.",
        labels = listOf("system", "library", "list")
    ),
    CommandEntity(
        command = "pm list packages",
        description = "Lists all installed packages.",
        labels = listOf("package", "list")
    ),
    CommandEntity(
        command = "pm list packages -3",
        description = "Lists only third-party (user-installed) packages.",
        labels = listOf("package", "list", "user")
    ),
    CommandEntity(
        command = "pm list packages -s",
        description = "Lists only system packages.",
        labels = listOf("package", "list", "system")
    ),
    CommandEntity(
        command = "pm list packages -d",
        description = "Lists only disabled packages.",
        labels = listOf("package", "list", "disable")
    ),
    CommandEntity(
        command = "pm list packages -e",
        description = "Lists only enabled packages.",
        labels = listOf("package", "list", "enable")
    ),
    CommandEntity(
        command = "pm list packages -f",
        description = "Lists all packages with their associated APK file paths.",
        labels = listOf("package", "list", "file")
    ),
    CommandEntity(
        command = "pm list permissions",
        description = "Lists all permissions defined on the device.",
        labels = listOf("permission", "list")
    ),
    CommandEntity(
        command = "pm list users",
        description = "Lists all user profiles on the device.",
        labels = listOf("user", "list")
    ),
    CommandEntity(
        command = "pm path <package>",
        description = "Displays the APK file path of a package.",
        labels = listOf("package", "file", "path")
    ),
    CommandEntity(
        command = "pm revoke <package> <Permission>",
        description = "Revokes a specific permission from a package.",
        labels = listOf("package", "permission")
    ),
    CommandEntity(
        command = "pm set-install-location <location>",
        description = "Sets the default install location (0=auto, 1=internal, 2=external).",
        labels = listOf("package", "install", "storage")
    ),
    CommandEntity(
        command = "pm suspend <package>",
        description = "Suspends a package, making it unusable.",
        labels = listOf("package", "suspend")
    ),
    CommandEntity(
        command = "pm unsuspend <package>",
        description = "Unsuspends a previously suspended package.",
        labels = listOf("package", "suspend")
    ),
    CommandEntity(
        command = "pm trim-caches <desired_free_space>",
        description = "Trims cache files to reach the desired free space.",
        labels = listOf("package", "cache", "storage")
    ),
    CommandEntity(
        command = "pm uninstall <package>",
        description = "Fully uninstalls a package from the device.",
        labels = listOf("package", "uninstall")
    ),
    CommandEntity(
        command = "pm uninstall -k <package>",
        description = "Uninstalls a package but keeps its data and cache.",
        labels = listOf("package", "uninstall", "data")
    ),
    CommandEntity(
        command = "pm uninstall --user 0 <package>",
        description = "Uninstalls a package only for the current user (useful for removing bloatware without root).",
        labels = listOf("package", "uninstall", "user", "bloatware")
    ),
    CommandEntity(
        command = "pm uninstall -k --user 0 <package>",
        description = "Uninstalls a package for the current user while keeping its data.",
        labels = listOf("package", "uninstall", "user", "data")
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
        command = "reboot bootloader",
        description = "Reboots the device into the bootloader (fastboot) mode.",
        labels = listOf("system", "reboot", "bootloader")
    ),
    CommandEntity(
        command = "reboot recovery",
        description = "Reboots the device into recovery mode.",
        labels = listOf("system", "reboot", "recovery")
    ),
    CommandEntity(
        command = "reboot -p",
        description = "Powers off the device.",
        labels = listOf("system", "power", "shutdown")
    ),
    CommandEntity(
        command = "rm <file_path>",
        description = "Deletes a file.",
        labels = listOf("file", "delete")
    ),
    CommandEntity(
        command = "rm -rf <path>",
        description = "Recursively and forcefully deletes a file or directory.",
        labels = listOf("file", "directory", "delete")
    ),
    CommandEntity(
        command = "rmdir <directory_path>",
        description = "Deletes an empty directory.",
        labels = listOf("directory", "delete")
    ),
    CommandEntity(
        command = "screencap <file_path>",
        description = "Captures a screenshot and saves it to the specified file path.",
        labels = listOf("screen", "capture", "utility")
    ),
    CommandEntity(
        command = "screencap -p /sdcard/screenshot.png",
        description = "Captures a screenshot in PNG format and saves to sdcard.",
        labels = listOf("screen", "capture", "utility")
    ),
    CommandEntity(
        command = "screenrecord /sdcard/recording.mp4",
        description = "Records the device screen and saves as MP4 video.",
        labels = listOf("screen", "record", "video")
    ),
    CommandEntity(
        command = "screenrecord --time-limit <seconds> /sdcard/recording.mp4",
        description = "Records the screen for a specified duration in seconds.",
        labels = listOf("screen", "record", "video")
    ),
    CommandEntity(
        command = "screenrecord --size <width>x<height> /sdcard/recording.mp4",
        description = "Records the screen at a specific resolution.",
        labels = listOf("screen", "record", "video")
    ),
    CommandEntity(
        command = "service list",
        description = "Lists all running system services.",
        labels = listOf("system", "service", "list")
    ),
    CommandEntity(
        command = "setprop <property> <value>",
        description = "Sets a system property to the specified value.",
        labels = listOf("system", "property", "config")
    ),
    CommandEntity(
        command = "settings get <namespace> <key>",
        description = "Gets the value of a specific system setting.",
        labels = listOf("system", "settings")
    ),
    CommandEntity(
        command = "settings list <namespace>",
        description = "Lists all settings in a specific namespace (system/secure/global).",
        labels = listOf("system", "settings", "list")
    ),
    CommandEntity(
        command = "settings put <namespace> <key> <value>",
        description = "Sets the value of a specific system setting.",
        labels = listOf("system", "settings")
    ),
    CommandEntity(
        command = "settings put global animator_duration_scale <value>",
        description = "Sets the animator duration scale (0=off, 0.5x, 1x, etc.).",
        labels = listOf("system", "settings", "animation", "ui")
    ),
    CommandEntity(
        command = "settings put global transition_animation_scale <value>",
        description = "Sets the transition animation scale (0=off, 0.5x, 1x, etc.).",
        labels = listOf("system", "settings", "animation", "ui")
    ),
    CommandEntity(
        command = "settings put global window_animation_scale <value>",
        description = "Sets the window animation scale (0=off, 0.5x, 1x, etc.).",
        labels = listOf("system", "settings", "animation", "ui")
    ),
    CommandEntity(
        command = "settings put system screen_brightness <0-255>",
        description = "Sets the screen brightness (0=darkest, 255=brightest).",
        labels = listOf("system", "settings", "display")
    ),
    CommandEntity(
        command = "settings put system screen_off_timeout <ms>",
        description = "Sets the screen timeout in milliseconds.",
        labels = listOf("system", "settings", "display")
    ),
    CommandEntity(
        command = "settings put secure enabled_accessibility_services <service>",
        description = "Enables a specific accessibility service.",
        labels = listOf("system", "settings", "accessibility")
    ),
    CommandEntity(
        command = "settings put global adb_enabled <0|1>",
        description = "Enables or disables ADB (Android Debug Bridge).",
        labels = listOf("system", "settings", "adb")
    ),
    CommandEntity(
        command = "settings put global development_settings_enabled <0|1>",
        description = "Enables or disables developer options.",
        labels = listOf("system", "settings", "developer")
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
        command = "svc bluetooth enable",
        description = "Enables Bluetooth.",
        labels = listOf("system", "bluetooth", "connectivity")
    ),
    CommandEntity(
        command = "svc bluetooth disable",
        description = "Disables Bluetooth.",
        labels = listOf("system", "bluetooth", "connectivity")
    ),
    CommandEntity(
        command = "svc data enable",
        description = "Enables mobile data.",
        labels = listOf("system", "data", "connectivity")
    ),
    CommandEntity(
        command = "svc data disable",
        description = "Disables mobile data.",
        labels = listOf("system", "data", "connectivity")
    ),
    CommandEntity(
        command = "svc nfc enable",
        description = "Enables NFC.",
        labels = listOf("system", "nfc", "connectivity")
    ),
    CommandEntity(
        command = "svc nfc disable",
        description = "Disables NFC.",
        labels = listOf("system", "nfc", "connectivity")
    ),
    CommandEntity(
        command = "svc power stayon true",
        description = "Keeps the screen always on while connected.",
        labels = listOf("system", "power", "display")
    ),
    CommandEntity(
        command = "svc power stayon false",
        description = "Restores normal screen timeout behavior.",
        labels = listOf("system", "power", "display")
    ),
    CommandEntity(
        command = "svc power reboot",
        description = "Reboots the device via the power service.",
        labels = listOf("system", "power", "reboot")
    ),
    CommandEntity(
        command = "svc power shutdown",
        description = "Shuts down the device.",
        labels = listOf("system", "power", "shutdown")
    ),
    CommandEntity(
        command = "svc usb setFunctions <function>",
        description = "Sets USB mode (mtp, ptp, rndis, midi, etc.).",
        labels = listOf("system", "usb", "connectivity")
    ),
    CommandEntity(
        command = "svc wifi enable",
        description = "Enables Wi-Fi.",
        labels = listOf("system", "wifi", "connectivity")
    ),
    CommandEntity(
        command = "svc wifi disable",
        description = "Disables Wi-Fi.",
        labels = listOf("system", "wifi", "connectivity")
    ),
    CommandEntity(
        command = "top",
        description = "Displays running processes in real-time.",
        labels = listOf("process", "system", "monitor")
    ),
    CommandEntity(
        command = "top -n 1",
        description = "Displays a single snapshot of running processes.",
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
        command = "uptime",
        description = "Displays how long the device has been running since last reboot.",
        labels = listOf("system", "info", "time")
    ),
    CommandEntity(
        command = "wc -l <file_path>",
        description = "Counts the number of lines in a file.",
        labels = listOf("file", "utility", "count")
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
        command = "wm density reset",
        description = "Resets the screen density to default.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "wm size <width>x<height>",
        description = "Sets the screen resolution.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "wm size reset",
        description = "Resets the screen resolution to default.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "wm overscan <left>,<top>,<right>,<bottom>",
        description = "Sets overscan for the display.",
        labels = listOf("system", "display", "ui")
    ),
    CommandEntity(
        command = "wm overscan reset",
        description = "Resets overscan to default.",
        labels = listOf("system", "display", "ui")
    )
)
