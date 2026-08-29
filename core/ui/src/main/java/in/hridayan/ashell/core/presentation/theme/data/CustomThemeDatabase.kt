package `in`.hridayan.ashell.core.presentation.theme.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserGeneratedColorSchemeEntity::class],
    version = 4,
    exportSchema = false
)
abstract class CustomThemeDatabase : RoomDatabase() {
    abstract val customColorSchemeDao: CustomColorSchemeDao

    companion object {
        const val DATABASE_NAME = "custom_theme_db"
    }
}
