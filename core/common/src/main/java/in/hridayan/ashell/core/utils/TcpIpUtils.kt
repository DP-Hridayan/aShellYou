package `in`.hridayan.ashell.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import `in`.hridayan.ashell.core.utils.TcpIpUtils.NO_PORT

object TcpIpUtils {

    private const val PROP_SERVICE_ADB_TCP_PORT = "service.adb.tcp.port"
    private const val PROP_PERSIST_ADB_TCP_PORT = "persist.adb.tcp.port"
    private const val NO_PORT = -1

    /**
     * Reads the TCP port on which adbd is (or should be) listening.
     * Checks the runtime property first, then the persistent OEM property.
     * Returns [NO_PORT] if neither is set.
     */

    fun getAdbTcpPort(): Int {
        return try {
            @SuppressLint("PrivateApi")
            val cls = Class.forName("android.os.SystemProperties")
            val getInt = cls.getMethod("getInt", String::class.java, Int::class.java)
            var port = getInt.invoke(null, PROP_SERVICE_ADB_TCP_PORT, NO_PORT) as Int
            if (port == NO_PORT) {
                port = getInt.invoke(null, PROP_PERSIST_ADB_TCP_PORT, NO_PORT) as Int
            }
            port
        } catch (_: Exception) {
            NO_PORT
        }
    }

    fun isTcpModeAvailable(): Boolean = getAdbTcpPort() > 0

    fun isUsbDebuggingEnabled(context: Context): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0

    fun isDeveloperOptionsEnabled(context: Context): Boolean =
        Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0
}
