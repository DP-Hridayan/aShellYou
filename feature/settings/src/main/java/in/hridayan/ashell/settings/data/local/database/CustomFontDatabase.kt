package `in`.hridayan.ashell.settings.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.hridayan.ashell.settings.domain.model.CustomFontEntity

@Database(entities = [CustomFontEntity::class], version = 1, exportSchema = false)
abstract class CustomFontDatabase : RoomDatabase() {

    abstract fun customFontDao(): CustomFontDao

    companion object {
        const val DATABASE_NAME = "custom_fonts_db"
    }
}
