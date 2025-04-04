package `in`.hridayan.ashell.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.hridayan.ashell.data.local.database.command_examples.CommandDao
import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity


@Database(
    entities = [
        CommandEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}
