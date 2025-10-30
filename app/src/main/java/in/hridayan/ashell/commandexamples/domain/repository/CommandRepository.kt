package `in`.hridayan.ashell.commandexamples.domain.repository

import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import kotlinx.coroutines.flow.Flow

interface CommandRepository {
    suspend fun insertCommand(command: CommandEntity)
    suspend fun insertAllCommands(commands: List<CommandEntity>)
    fun loadDefaultCommandsWithProgress(): Flow<Float>
    suspend fun updateCommand(command: CommandEntity)
    suspend fun deleteCommand(id: Int)
    suspend fun deleteAllCommands()
    fun getCommandCount(): Int
    suspend fun getAllCommandsOnce(): List<CommandEntity>
    suspend fun getCommandById(id: Int): CommandEntity?
    fun getSortedCommands(sortType: Int) :  Flow<List<CommandEntity>>
    fun getFavoriteCommands(): Flow<List<CommandEntity>>
    fun searchCommands(query: String): Flow<List<CommandEntity>>
    suspend fun incrementUseCount(commandId: Int)
    suspend fun updateFavoriteStatus(commandId: Int, isFavourite: Boolean)
}