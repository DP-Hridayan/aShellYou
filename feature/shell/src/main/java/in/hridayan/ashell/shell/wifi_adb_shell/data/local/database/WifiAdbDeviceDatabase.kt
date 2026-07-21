package `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.model.WifiAdbDeviceEntity

@Database(
    entities = [WifiAdbDeviceEntity::class],
    version = 1,
    exportSchema = true
)
abstract class WifiAdbDeviceDatabase : RoomDatabase() {
    abstract fun wifiAdbDeviceDao(): WifiAdbDeviceDao
}
