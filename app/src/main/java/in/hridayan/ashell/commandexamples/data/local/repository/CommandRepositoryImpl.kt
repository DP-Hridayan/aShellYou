package `in`.hridayan.ashell.commandexamples.data.local.repository

import `in`.hridayan.ashell.commandexamples.data.local.database.CommandDao
import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CommandRepositoryImpl @Inject constructor(
    private val commandDao: CommandDao,
) : CommandRepository {

    override suspend fun insertCommand(command: CommandEntity) {
        commandDao.insertCommand(command)
    }

    override suspend fun updateCommand(command: CommandEntity) {
        commandDao.updateCommand(command)
    }

    override suspend fun deleteCommand(id: Int) {
        commandDao.deleteCommand(id)
    }

    override fun getCommandCount(): Int {
        return commandDao.getCommandCount()
    }

    override suspend fun getCommandById(id: Int): CommandEntity? {
        return commandDao.getCommandById(id)
    }

    override fun getCommandsAlphabetically(): Flow<List<CommandEntity>> {
        return commandDao.getCommandsAlphabetically()
    }

    override fun getCommandsReverseAlphabetically(): Flow<List<CommandEntity>> {
        return commandDao.getCommandsReverseAlphabetically()
    }

    override fun getMostUsedCommands(): Flow<List<CommandEntity>> {
        return commandDao.getMostUsedCommands()
    }

    override fun getLeastUsedCommands(): Flow<List<CommandEntity>> {
        return commandDao.getLeastUsedCommands()
    }

    override fun getFavoriteCommands(): Flow<List<CommandEntity>> {
        return commandDao.getFavoriteCommands()
    }

    override fun searchCommands(query: String): Flow<List<CommandEntity>> {
        return commandDao.searchCommands(query)
    }

    override suspend fun incrementUseCount(commandId: Int) {
        commandDao.incrementUseCount(commandId)
    }

    override suspend fun updateFavoriteStatus(commandId: Int, isFavourite: Boolean) {
        commandDao.updateFavoriteStatus(commandId, isFavourite)
    }
}