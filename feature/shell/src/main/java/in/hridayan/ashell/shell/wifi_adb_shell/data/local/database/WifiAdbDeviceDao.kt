package `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.model.WifiAdbDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiAdbDeviceDao {
    
    @Query("SELECT * FROM wifi_adb_devices ORDER BY lastConnected DESC")
    fun getAllDevices(): Flow<List<WifiAdbDeviceEntity>>
    
    @Query("SELECT * FROM wifi_adb_devices ORDER BY lastConnected DESC")
    suspend fun getAllDevicesOnce(): List<WifiAdbDeviceEntity>
    
    @Query("SELECT * FROM wifi_adb_devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: String): WifiAdbDeviceEntity?
    
    @Query("SELECT * FROM wifi_adb_devices WHERE ip = :ip")
    suspend fun getDeviceByIp(ip: String): WifiAdbDeviceEntity?
    
    @Query("SELECT * FROM wifi_adb_devices WHERE serialNumber = :serialNumber")
    suspend fun getDeviceBySerialNumber(serialNumber: String): WifiAdbDeviceEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: WifiAdbDeviceEntity)
    
    @Update
    suspend fun updateDevice(device: WifiAdbDeviceEntity)
    
    @Delete
    suspend fun deleteDevice(device: WifiAdbDeviceEntity)
    
    @Query("DELETE FROM wifi_adb_devices WHERE id = :deviceId")
    suspend fun deleteDeviceById(deviceId: String)
    
    @Query("DELETE FROM wifi_adb_devices")
    suspend fun deleteAllDevices()
    
    @Query("SELECT COUNT(*) FROM wifi_adb_devices")
    suspend fun getDeviceCount(): Int
}
