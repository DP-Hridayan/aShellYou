package `in`.hridayan.ashell.qstiles.domain.executor

interface CommandExecutor {
    suspend fun execute(command: String): CommandResult
}
