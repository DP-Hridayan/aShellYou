package `in`.hridayan.ashell.shell.wifi_adb_shell.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice

class WifiAdbStorage(context: Context) {
    private val prefs = context.getSharedPreferences("wifi_adb_devices", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getDevices(): MutableList<WifiAdbDevice> {
        val json = prefs.getString("devices", "[]")
        val type = object : TypeToken<MutableList<WifiAdbDevice>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveDevice(device: WifiAdbDevice) {
        val devices = getDevices().toMutableList()
        val existing = devices.indexOfFirst { it.ip == device.ip && it.port == device.port }

        if (existing >= 0) {
            devices[existing] = device
        } else {
            devices.add(device)
        }

        prefs.edit { putString("devices", gson.toJson(devices)) }
    }

    fun removeDevice(ip: String, port: Int) {
        val devices = getDevices().filterNot { it.ip == ip && it.port == port }
        prefs.edit { putString("devices", gson.toJson(devices)) }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}
