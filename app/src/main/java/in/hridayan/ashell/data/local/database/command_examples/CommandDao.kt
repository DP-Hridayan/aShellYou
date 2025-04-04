package `in`.hridayan.ashell.data.local.database.command_examples

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: CommandEntity)

    @Update
    suspend fun updateCommand(command: CommandEntity)

    @Delete
    suspend fun deleteCommand(command: CommandEntity)

    @Query("SELECT COUNT(*) FROM commands")
    fun getCommandCount(): Int

    @Query("SELECT * FROM commands WHERE id = :commandId")
    suspend fun getCommandById(commandId: Int): CommandEntity?

    @Query("SELECT * FROM commands ORDER BY command ASC")
    fun getCommandsAlphabetically(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commands ORDER BY command DESC")
    fun getCommandsReverseAlphabetically(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commands WHERE isFavourite = 1 ORDER BY command ASC")
    fun getFavoriteCommands(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commands ORDER BY useCount DESC , command ASC ")
    fun getMostUsedCommands(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commands ORDER BY useCount ASC , command ASC ")
    fun getLeastUsedCommands(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commands WHERE command LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchCommands(query: String): Flow<List<CommandEntity>>

    @Query("UPDATE commands SET useCount = useCount + 1 WHERE id = :commandId")
    suspend fun incrementUseCount(commandId: Int)

    @Query("UPDATE commands SET isFavourite = :isFavourite WHERE id = :commandId")
    suspend fun updateFavoriteStatus(commandId: Int, isFavourite: Boolean)
}