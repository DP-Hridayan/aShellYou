package `in`.hridayan.ashell.data.local.database.command_examples

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.hridayan.ashell.data.local.model.command_examples.LabelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabel(label: LabelEntity)

    @Query("SELECT * FROM commandLabels")
    fun getAllLabels(): Flow<List<LabelEntity>>
}