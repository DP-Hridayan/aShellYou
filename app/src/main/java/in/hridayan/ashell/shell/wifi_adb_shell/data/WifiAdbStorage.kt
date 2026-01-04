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
        // Match by serial number if available, otherwise by IP
        val existing = devices.indexOfFirst { existingDevice ->
            if (device.serialNumber != null && existingDevice.serialNumber != null) {
                existingDevice.serialNumber == device.serialNumber
            } else {
                existingDevice.ip == device.ip
            }
        }

        if (existing >= 0) {
            // Update existing device entry with new port/IP/etc
            devices[existing] = device
        } else {
            devices.add(device)
        }

        prefs.edit { putString("devices", gson.toJson(devices)) }
    }

    fun updateDevice(device: WifiAdbDevice) {
        val devices = getDevices().toMutableList()
        val existing = devices.indexOfFirst { existingDevice ->
            if (device.serialNumber != null && existingDevice.serialNumber != null) {
                existingDevice.serialNumber == device.serialNumber
            } else {
                existingDevice.ip == device.ip
            }
        }
        if (existing >= 0) {
            devices[existing] = device
            prefs.edit { putString("devices", gson.toJson(devices)) }
        }
    }

    fun removeDevice(device: WifiAdbDevice) {
        val devices = getDevices().filterNot { it.ip == device.ip }
        prefs.edit { putString("devices", gson.toJson(devices)) }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}



