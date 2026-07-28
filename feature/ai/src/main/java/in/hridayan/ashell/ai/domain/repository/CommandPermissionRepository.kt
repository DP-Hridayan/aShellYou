package `in`.hridayan.ashell.ai.domain.repository

interface CommandPermissionRepository {
    suspend fun isCommandAlwaysAllowed(command: String): Boolean
    suspend fun setCommandAlwaysAllowed(command: String, isAllowed: Boolean)
    suspend fun clearPermission(command: String)
}
