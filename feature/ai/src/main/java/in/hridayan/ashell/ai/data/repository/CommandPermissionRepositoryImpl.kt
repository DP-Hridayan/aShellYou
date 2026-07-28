package `in`.hridayan.ashell.ai.data.repository

import `in`.hridayan.ashell.ai.data.local.database.dao.CommandPermissionDao
import `in`.hridayan.ashell.ai.data.local.database.entity.CommandPermissionEntity
import `in`.hridayan.ashell.ai.domain.repository.CommandPermissionRepository
import javax.inject.Inject

class CommandPermissionRepositoryImpl @Inject constructor(
    private val commandPermissionDao: CommandPermissionDao
) : CommandPermissionRepository {

    override suspend fun isCommandAlwaysAllowed(command: String): Boolean {
        val entity = commandPermissionDao.getPermissionForCommand(command)
        return entity?.isAlwaysAllowed == true
    }

    override suspend fun setCommandAlwaysAllowed(command: String, isAllowed: Boolean) {
        val entity = CommandPermissionEntity(
            command = command,
            isAlwaysAllowed = isAllowed
        )
        commandPermissionDao.setPermission(entity)
    }

    override suspend fun clearPermission(command: String) {
        commandPermissionDao.clearPermission(command)
    }
}
