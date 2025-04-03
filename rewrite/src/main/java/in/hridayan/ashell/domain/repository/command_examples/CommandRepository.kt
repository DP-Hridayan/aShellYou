package `in`.hridayan.ashell.domain.repository.command_examples

import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity
import kotlinx.coroutines.flow.Flow

interface CommandRepository {
    suspend fun insertCommand(command: CommandEntity)
    suspend fun deleteCommand(command: CommandEntity)
    suspend fun updateCommand(command: CommandEntity)
    suspend fun reorderCommandsAlphabetically()
    suspend fun reorderCommandsReverseAlphabetically()
    fun getAllCommands(): Flow<List<CommandEntity>>
    fun getCommandsAlphabetically(): Flow<List<CommandEntity>>
    fun getCommandsReversedAlphabetically(): Flow<List<CommandEntity>>
    fun getCommandsByFavourite(): Flow<List<CommandEntity>>
    fun getCommandsByUsage(): Flow<List<CommandEntity>>
    fun getCommandsByLabel(label: String): Flow<List<CommandEntity>>
    suspend fun incrementUseCount(id: Int)
}