package `in`.hridayan.ashell.commandexamples.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertCommand(command: CommandEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCommands(commands: List<CommandEntity>)

    @Update
    suspend fun updateCommand(command: CommandEntity)

    @Query("DELETE FROM commands WHERE id = :commandId")
    suspend fun deleteCommand(commandId: Int)

    @Query("DELETE FROM commands")
    suspend fun deleteAllCommands()

    @Query("SELECT COUNT(*) FROM commands")
    fun getCommandCount(): Int

    @Query("SELECT * FROM commands")
    suspend fun getAllCommandsOnce(): List<CommandEntity>

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