package `in`.hridayan.ashell.settings.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.hridayan.ashell.settings.domain.model.CustomFontEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFontDao {

    @Query("SELECT * FROM custom_fonts ORDER BY importedAt DESC")
    fun getAllCustomFonts(): Flow<List<CustomFontEntity>>

    @Query("SELECT * FROM custom_fonts WHERE id = :id LIMIT 1")
    suspend fun getCustomFontById(id: Int): CustomFontEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomFont(entity: CustomFontEntity): Long

    @Delete
    suspend fun deleteCustomFont(entity: CustomFontEntity)
}
