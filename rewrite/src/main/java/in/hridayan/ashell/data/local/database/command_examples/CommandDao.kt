package `in`.hridayan.ashell.data.local.database.command_examples

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {

    @Query("SELECT  COUNT(*) FROM commandExamples")
    suspend fun getCommandCount(): Int

    @Query("SELECT * FROM commandExamples WHERE command = :command LIMIT 1")
    suspend fun getCommandByName(command: String): CommandEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertPredefinedCommands(commands: List<CommandEntity>)

    suspend fun safeInsertCommand(command: CommandEntity) {
        val existingCommand = getCommandByName(command.command)
        if (existingCommand == null) {
            insertCommand(command)
        }
    }

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertCommand(command: CommandEntity)

    @Delete
    suspend fun deleteCommand(command: CommandEntity)

    @Query("SELECT * FROM commandExamples ORDER BY position ASC")
    fun getAllCommands(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commandExamples ORDER BY command ASC")
   fun getCommandsAlphabetically(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commandExamples ORDER BY command DESC")
   fun getCommandsReversedAlphabetically(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commandExamples ORDER BY description ASC")
    fun getCommandsByDescription(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commandExamples WHERE labels LIKE '%' || :label || '%'")
     fun getCommandsByLabel(label: String): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commandExamples WHERE isFavourite = 1")
     fun getCommandsByFavourite(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commandExamples ORDER BY useCount DESC")
     fun getCommandsByUseCount(): Flow<List<CommandEntity>>

     @Query("UPDATE commandExamples SET useCount = useCount + 1 WHERE id = :id")
     suspend fun incrementUseCount(id: Int)

    @Query("UPDATE commandExamples SET position = :newPosition WHERE id = :id")
    suspend fun updatePosition(id: Int, newPosition: Int)
}