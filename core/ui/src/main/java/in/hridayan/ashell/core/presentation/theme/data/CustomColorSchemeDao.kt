package `in`.hridayan.ashell.core.presentation.theme.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomColorSchemeDao {
    @Query("SELECT * FROM custom_themes ORDER BY createdAt DESC")
    fun getAllColorSchemes(): Flow<List<UserGeneratedColorSchemeEntity>>

    @Query("SELECT * FROM custom_themes WHERE id = :id LIMIT 1")
    suspend fun getColorSchemeById(id: Int): UserGeneratedColorSchemeEntity?

    @Query("SELECT * FROM custom_themes WHERE id = :id LIMIT 1")
    fun getColorSchemeByIdFlow(id: Int): Flow<UserGeneratedColorSchemeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColorScheme(scheme: UserGeneratedColorSchemeEntity): Long

    @Update
    suspend fun updateColorScheme(scheme: UserGeneratedColorSchemeEntity)

    @Delete
    suspend fun deleteColorScheme(scheme: UserGeneratedColorSchemeEntity)
}
