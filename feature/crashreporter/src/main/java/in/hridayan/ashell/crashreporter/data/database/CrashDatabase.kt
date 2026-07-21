package `in`.hridayan.ashell.crashreporter.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.hridayan.ashell.crashreporter.data.model.CrashLogEntity

@Database(entities = [CrashLogEntity::class], version = 1)
abstract class CrashDatabase : RoomDatabase() {
    abstract fun crashLogDao(): CrashLogDao
}
