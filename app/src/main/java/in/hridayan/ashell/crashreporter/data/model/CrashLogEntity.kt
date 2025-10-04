package `in`.hridayan.ashell.crashreporter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crash_logs")
data class CrashLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val deviceName: String,
    val manufacturer: String,
    val osVersion: String,
    val stackTrace: String
)
