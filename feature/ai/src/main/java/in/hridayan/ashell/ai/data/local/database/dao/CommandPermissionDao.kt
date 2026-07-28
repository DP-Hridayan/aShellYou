package `in`.hridayan.ashell.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.hridayan.ashell.ai.data.local.database.entity.CommandPermissionEntity

@Dao
interface CommandPermissionDao {
    @Query("SELECT * FROM command_permissions WHERE command = :command LIMIT 1")
    suspend fun getPermissionForCommand(command: String): CommandPermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPermission(permission: CommandPermissionEntity)
    
    @Query("DELETE FROM command_permissions WHERE command = :command")
    suspend fun clearPermission(command: String)
}
