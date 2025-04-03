package `in`.hridayan.ashell.data.local.repository.command_examples

import `in`.hridayan.ashell.data.local.database.command_examples.CommandDao
import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity
import `in`.hridayan.ashell.domain.repository.command_examples.CommandRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CommandRepositoryImpl @Inject constructor(
    private val commandDao: CommandDao

) : CommandRepository {

    override suspend fun insertCommand(command: CommandEntity) {
        if (command.labels.size > 3) {
            throw IllegalArgumentException("A command cannot have more than 3 labels")
        }
        commandDao.safeInsertCommand(command)
    }

    override suspend fun deleteCommand(command: CommandEntity) {
        commandDao.deleteCommand(command)
    }

    override suspend fun updateCommand(command: CommandEntity) {
        if (command.labels.size > 3) {
            throw IllegalArgumentException("A command cannot have more than 3 labels")
        }
        commandDao.insertCommand(command)
    }

    override fun getAllCommands(): Flow<List<CommandEntity>> =
        commandDao.getAllCommands()

    override fun getCommandsAlphabetically(): Flow<List<CommandEntity>> =
        commandDao.getCommandsAlphabetically()

    override fun getCommandsReversedAlphabetically(): Flow<List<CommandEntity>> =
        commandDao.getCommandsReversedAlphabetically()

    override fun getCommandsByFavourite(): Flow<List<CommandEntity>> =
        commandDao.getCommandsByFavourite()

    override fun getCommandsByUsage(): Flow<List<CommandEntity>> =
        commandDao.getCommandsByUseCount()

    override fun getCommandsByLabel(label: String): Flow<List<CommandEntity>> =
        commandDao.getCommandsByLabel(label)

    override suspend fun incrementUseCount(id: Int) {
        commandDao.incrementUseCount(id)
    }

    override suspend fun reorderCommandsAlphabetically() {
        val commands = commandDao.getAllCommands().first()
        val sortedCommands = commands.sortedBy { it.command }

        sortedCommands.forEachIndexed { index, command ->
            commandDao.updatePosition(command.id, index)
        }
    }

    override suspend fun reorderCommandsReverseAlphabetically() {
        val commands = commandDao.getAllCommands().first()
        val sortedCommands = commands.sortedByDescending { it.command }

        sortedCommands.forEachIndexed { index, command ->
            commandDao.updatePosition(command.id, index)
        }
    }
}
