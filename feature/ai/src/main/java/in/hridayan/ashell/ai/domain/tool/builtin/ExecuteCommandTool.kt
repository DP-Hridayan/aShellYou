package `in`.hridayan.ashell.ai.domain.tool.builtin

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.ai.domain.tool.CommandExecutionManager
import `in`.hridayan.ashell.ai.domain.tool.PermissionResult
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.domain.repository.ShellRepository
import `in`.hridayan.ashell.core.common.domain.model.ai.SessionIdContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecuteCommandTool @Inject constructor(
    private val commandExecutionManager: CommandExecutionManager,
    private val shellRepository: ShellRepository,
    private val settingsRepository: SettingsRepository
) : AiTool {

    override val name = "execute_command"
    override val description = "Execute a shell command on the user's device. You MUST use this tool to perform actions requested by the user. If the user hasn't explicitly allowed the command before, they will be prompted to allow it."
    override val parametersSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "command" to ToolSchemaProperty(
                type = "STRING",
                description = "The exact shell command to run, e.g. 'pm list packages'"
            )
        ),
        required = listOf("command")
    )

    override suspend fun execute(args: JsonObject?): String {
        val command = args?.get("command")?.jsonPrimitive?.content
            ?: return """{"error": "command is required"}"""

        // Request permission
        val permissionResult = commandExecutionManager.requestPermission(command)
        if (permissionResult == PermissionResult.DENIED) {
            return """{"error": "User denied permission to execute the command"}"""
        }

        // Add task to running tasks
        var isCancelled = false
        val sessionId = currentCoroutineContext()[SessionIdContext]?.sessionId
        val taskId = commandExecutionManager.addRunningTask(command, sessionId) {
            isCancelled = true
        }

        try {
            val mode = settingsRepository.getInt(SettingsKeys.LocalAdbWorkingMode).firstOrNull() ?: SettingsKeys.LocalAdbWorkingMode.default
            val outputFlow = when (mode) {
                `in`.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode.ROOT -> shellRepository.executeRootCommand(command)
                `in`.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode.SHIZUKU -> shellRepository.executeShizukuCommand(command)
                else -> shellRepository.executeBasicCommand(command)
            }
            
            val outputBuilder = StringBuilder()
            
            val timeoutResult = withTimeoutOrNull(15000) {
                outputFlow.collect { line ->
                    if (isCancelled) {
                        shellRepository.stopCommand()
                        return@collect
                    }
                    outputBuilder.appendLine(line.text)
                }
                true
            }
            
            if (timeoutResult == null) {
                shellRepository.stopCommand()
                outputBuilder.appendLine("\n<Command execution timed out after 15s. Process stopped.>")
            }
            
            if (isCancelled) {
                return """{"status": "cancelled", "output": "Task was cancelled by the user."}"""
            }
            
            val cleanOutput = outputBuilder.toString().replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
            return """{"status": "success", "output": "$cleanOutput"}"""
        } catch (e: Exception) {
            val cleanError = e.message?.replace("\\", "\\\\")?.replace("\"", "\\\"")?.replace("\n", "\\n")
            return """{"error": "$cleanError"}"""
        } finally {
            commandExecutionManager.removeRunningTask(taskId)
        }
    }
}
