package `in`.hridayan.ashell.commandexamples.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import `in`.hridayan.ashell.core.common.converters.StringListConverter

@Database(
    entities = [
        CommandEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class CommandDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}