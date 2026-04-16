package `in`.hridayan.ashell.qstiles.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.hridayan.ashell.qstiles.data.dao.TileLogDao
import `in`.hridayan.ashell.qstiles.data.model.TileLogEntity

@Database(
    entities = [TileLogEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TileLogDatabase : RoomDatabase() {
    abstract fun dao(): TileLogDao
}