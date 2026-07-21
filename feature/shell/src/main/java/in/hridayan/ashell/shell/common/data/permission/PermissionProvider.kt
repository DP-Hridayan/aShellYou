package `in`.hridayan.ashell.shell.common.data.permission

object PermissionProvider {

    val adbPermissions: List<String> = listOf(
        // Location
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",

        // Camera & Microphone
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",

        // Contacts & Calendar
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR",

        // Phone
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_PHONE_NUMBERS",
        "android.permission.CALL_PHONE",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.ANSWER_PHONE_CALLS",

        // SMS
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_WAP_PUSH",
        "android.permission.RECEIVE_MMS",

        // Storage
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.MANAGE_EXTERNAL_STORAGE",
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO",

        // Body Sensors
        "android.permission.BODY_SENSORS",
        "android.permission.BODY_SENSORS_BACKGROUND",
        "android.permission.ACTIVITY_RECOGNITION",

        // Bluetooth
        "android.permission.BLUETOOTH_CONNECT",
        "android.permission.BLUETOOTH_SCAN",
        "android.permission.BLUETOOTH_ADVERTISE",

        // Notifications
        "android.permission.POST_NOTIFICATIONS",

        // Nearby Devices
        "android.permission.NEARBY_WIFI_DEVICES",
        "android.permission.UWB_RANGING",

        // System
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.WRITE_SETTINGS",
        "android.permission.REQUEST_INSTALL_PACKAGES",
        "android.permission.REQUEST_DELETE_PACKAGES",
        "android.permission.PACKAGE_USAGE_STATS",
        "android.permission.ACCESS_NOTIFICATION_POLICY",

        // Accessibility
        "android.permission.BIND_ACCESSIBILITY_SERVICE",

        // Device Admin
        "android.permission.BIND_DEVICE_ADMIN",

        // Network
        "android.permission.CHANGE_WIFI_STATE",
        "android.permission.ACCESS_WIFI_STATE",
        "android.permission.CHANGE_NETWORK_STATE",
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.INTERNET",

        // Misc
        "android.permission.VIBRATE",
        "android.permission.WAKE_LOCK",
        "android.permission.FOREGROUND_SERVICE",
        "android.permission.RECEIVE_BOOT_COMPLETED",
        "android.permission.USE_BIOMETRIC",
        "android.permission.USE_FINGERPRINT",

        // Special
        "android.permission.DUMP",
        "android.permission.WRITE_SECURE_SETTINGS",
        "android.permission.READ_LOGS",
        "android.permission.INTERACT_ACROSS_USERS",
        "android.permission.INTERACT_ACROSS_USERS_FULL"
    )
}
