package `in`.hridayan.ashell.qstiles.data.provider

object TileIconProvider {

    private const val CLOUD_ICON_PREFIX = "material:"

    private val LEGACY_ID_MAP = mapOf(
        "battery" to "battery_full",
        "bluetooth_off" to "bluetooth_disabled",
        "boost" to "bolt",
        "brightness" to "brightness_6",
        "bug" to "bug_report",
        "network" to "cell_tower",
        "folder_create" to "create_new_folder",
        "search_manage" to "manage_search",
        "camera" to "photo_camera",
        "power" to "power_settings_new",
        "restart" to "restart_alt",
        "screenshot" to "screenshot_tablet",
        "app_settings" to "settings_applications",
        "touch" to "touch_app",
        "video" to "videocam",
        "wifi_tethering_on" to "wifi_tethering",
        "wifi_tethering_off" to "portable_wifi_off",
        "5g" to "5g",
        "4g" to "4g_mobiledata",
        "apk_install" to "install_mobile",
        "inventory" to "inventory_2",
    )

    fun migrateIconId(id: String): String {
        if (id.startsWith(CLOUD_ICON_PREFIX)) return id.removePrefix(CLOUD_ICON_PREFIX)
        return LEGACY_ID_MAP[id] ?: id
    }
}
