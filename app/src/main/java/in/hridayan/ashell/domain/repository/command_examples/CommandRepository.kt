package `in`.hridayan.ashell.domain.repository.command_examples

import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity
import kotlinx.coroutines.flow.Flow

interface CommandRepository {
    suspend fun insertCommand(command: CommandEntity)
    suspend fun updateCommand(command: CommandEntity)
    suspend fun deleteCommand(command: CommandEntity)
    fun getCommandCount(): Int
    suspend fun getCommandById(id: Int): CommandEntity?
    fun getCommandsAlphabetically(): Flow<List<CommandEntity>>
    fun getCommandsReverseAlphabetically(): Flow<List<CommandEntity>>
    fun getMostUsedCommands(): Flow<List<CommandEntity>>
    fun getLeastUsedCommands(): Flow<List<CommandEntity>>
    fun getFavoriteCommands(): Flow<List<CommandEntity>>
    fun searchCommands(query: String): Flow<List<CommandEntity>>
    suspend fun incrementUseCount(commandId: Int)
    suspend fun updateFavoriteStatus(commandId: Int, isFavourite: Boolean)
}