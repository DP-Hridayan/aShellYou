package `in`.hridayan.ashell.qstiles.data.provider

import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.qstiles.data.model.TileIcon

object TileIconProvider {
    val icons = listOf(
        TileIcon("add", R.drawable.ts_add, listOf("add", "create", "new", "plus")),
        TileIcon(
            "analytics",
            R.drawable.ts_analytics,
            listOf("analytics", "stats", "usage", "monitor")
        ),
        TileIcon("android", R.drawable.ts_android, listOf("android", "system", "os")),
        TileIcon(
            "battery",
            R.drawable.ts_android_battery_full,
            listOf("battery", "power", "charge")
        ),
        TileIcon("apk_install", R.drawable.ts_apk_install, listOf("install", "apk", "package")),
        TileIcon("apps", R.drawable.ts_apps, listOf("apps", "applications", "launcher")),
        TileIcon(
            "aspect_ratio",
            R.drawable.ts_aspect_ratio,
            listOf("display", "screen", "resolution")
        ),
        TileIcon("bluetooth", R.drawable.ts_bluetooth, listOf("bluetooth", "bt", "connectivity")),
        TileIcon(
            "bluetooth_off",
            R.drawable.ts_bluetooth_disabled,
            listOf("bluetooth", "bt", "disconnect", "off")
        ),
        TileIcon("bolt", R.drawable.ts_bolt, listOf("power", "fast", "energy")),
        TileIcon("boost", R.drawable.ts_bolt_boost, listOf("boost", "performance", "speed")),
        TileIcon(
            "brightness",
            R.drawable.ts_brightness_6,
            listOf("brightness", "display", "light")
        ),
        TileIcon("bug", R.drawable.ts_bug_report, listOf("bug", "debug", "log", "error")),
        TileIcon("build", R.drawable.ts_build, listOf("build", "compile", "dev")),
        TileIcon("cancel", R.drawable.ts_cancel, listOf("cancel", "stop", "close")),
        TileIcon(
            "network",
            R.drawable.ts_cell_tower,
            listOf("network", "signal", "mobile", "data")
        ),
        TileIcon("close", R.drawable.ts_close, listOf("close", "exit")),
        TileIcon("code", R.drawable.ts_code, listOf("code", "dev", "command", "adb")),
        TileIcon(
            "folder_create",
            R.drawable.ts_create_new_folder,
            listOf("folder", "create", "directory")
        ),
        TileIcon("dark_mode", R.drawable.ts_dark_mode, listOf("dark", "night", "theme")),
        TileIcon("delete", R.drawable.ts_delete, listOf("delete", "remove")),
        TileIcon(
            "delete_forever",
            R.drawable.ts_delete_forever,
            listOf("delete", "remove", "force")
        ),
        TileIcon("description", R.drawable.ts_description, listOf("file", "document", "text")),
        TileIcon(
            "display_settings",
            R.drawable.ts_display_settings,
            listOf("display", "settings", "screen")
        ),
        TileIcon("dns", R.drawable.ts_dns, listOf("dns", "hostname", "dns_mode")),
        TileIcon("folder", R.drawable.ts_folder, listOf("folder", "directory")),
        TileIcon("folder_open", R.drawable.ts_folder_open, listOf("folder", "open", "directory")),
        TileIcon("gamepad", R.drawable.ts_gamepad, listOf("game", "input", "controller")),
        TileIcon("inventory", R.drawable.ts_inventory_2, listOf("storage", "items", "list")),
        TileIcon("keyboard", R.drawable.ts_keyboard, listOf("keyboard", "input", "text")),
        TileIcon("lan", R.drawable.ts_lan, listOf("network", "lan", "ethernet")),
        TileIcon("light_mode", R.drawable.ts_light_mode, listOf("light", "day", "theme")),
        TileIcon("lock", R.drawable.ts_lock, listOf("lock", "security")),
        TileIcon("search_manage", R.drawable.ts_manage_search, listOf("search", "manage", "find")),
        TileIcon("memory", R.drawable.ts_memory, listOf("memory", "ram", "performance")),
        TileIcon("package", R.drawable.ts_package, listOf("package", "app", "apk")),
        TileIcon("camera", R.drawable.ts_photo_camera, listOf("camera", "photo", "capture")),
        TileIcon("power", R.drawable.ts_power_settings_new, listOf("power", "shutdown", "restart")),
        TileIcon("restart", R.drawable.ts_restart_alt, listOf("restart", "reboot")),
        TileIcon("schedule", R.drawable.ts_schedule, listOf("time", "schedule", "timer")),
        TileIcon(
            "screenshot",
            R.drawable.ts_screenshot_tablet,
            listOf("screenshot", "screen", "capture")
        ),
        TileIcon("search", R.drawable.ts_search, listOf("search", "find")),
        TileIcon(
            "security",
            R.drawable.ts_security,
            listOf("security", "permission", "auth", "root")
        ),
        TileIcon("settings", R.drawable.ts_settings, listOf("settings", "config", "system")),
        TileIcon(
            "app_settings",
            R.drawable.ts_settings_applications,
            listOf("apps", "settings", "manage")
        ),
        TileIcon("speed", R.drawable.ts_speed, listOf("speed", "performance", "fast")),
        TileIcon("storage", R.drawable.ts_storage, listOf("storage", "disk", "space")),
        TileIcon("terminal", R.drawable.ts_terminal, listOf("terminal", "shell", "command")),
        TileIcon("touch", R.drawable.ts_touch_app, listOf("touch", "input", "gesture")),
        TileIcon("tune", R.drawable.ts_tune, listOf("tune", "adjust", "settings")),
        TileIcon("video", R.drawable.ts_videocam, listOf("video", "record", "screenrecord")),
        TileIcon("wifi", R.drawable.ts_wifi, listOf("wifi", "network", "internet")),
        TileIcon(
            "wifi_off",
            R.drawable.ts_wifi_off,
            listOf("wifi", "network", "disconnect", "off")
        ),
        TileIcon(
            "wifi_tethering_on",
            R.drawable.ts_wifi_tethering,
            listOf("wifi", "network", "tether", "connect", "on")
        ),
        TileIcon(
            "wifi_tethering_off",
            R.drawable.ts_portable_wifi_off,
            listOf("wifi", "network", "tether", "disconnect", "off")
        ),
        TileIcon(
            "5g",
            R.drawable.ts_5g,
            listOf("5g", "network", "NR", "internet", "cellular")
        ),
        TileIcon(
            "4g",
            R.drawable.ts_4g_mobiledata,
            listOf("4g", "network", "LTE", "internet", "cellular")
        ),
    )

    val iconById: Map<String, TileIcon> =
        icons.associateBy { it.id }

    val iconsByKeyword: Map<String, List<TileIcon>> =
        icons
            .flatMap { icon ->
                icon.keywords.map { keyword -> keyword to icon }
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )

    fun getIconRes(id: String): Int {
        return iconById[id]?.resId ?: R.drawable.ic_adb
    }
}

