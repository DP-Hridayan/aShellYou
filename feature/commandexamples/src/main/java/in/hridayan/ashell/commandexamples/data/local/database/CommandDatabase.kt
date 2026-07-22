package `in`.hridayan.ashell.commandexamples.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.hridayan.ashell.core.common.converters.StringListConverter
import `in`.hridayan.ashell.core.domain.model.CommandEntity

@Database(entities = [CommandEntity::class], version = 2)
@TypeConverters(StringListConverter::class)
abstract class CommandDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}
