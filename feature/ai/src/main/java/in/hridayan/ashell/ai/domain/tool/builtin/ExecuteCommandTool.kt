package `in`.hridayan.ashell.ai.domain.tool.builtin

import `in`.hridayan.ashell.ai.domain.tool.CommandExecutionManager
import `in`.hridayan.ashell.ai.domain.tool.PermissionResult
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.SessionIdContext
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.common.domain.repository.AiConnectionStateProvider
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.domain.repository.ShellRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class ExecuteCommandTool @Inject constructor(
    private val commandExecutionManager: CommandExecutionManager,
    private val shellRepository: ShellRepository,
    private val settingsRepository: SettingsRepository,
    private val otgRepository: OtgRepository,
    private val aiConnectionStateProvider: AiConnectionStateProvider
) : AiTool {

    override val name = "execute_command"
    override val description =
        "Execute a shell command on the user's device. You MUST use this tool to perform actions requested by the user. If the user hasn't explicitly allowed the command before, they will be prompted to allow it."
    override val parametersSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "command" to ToolSchemaProperty(
                type = "STRING",
                description = "The exact shell command to run, e.g. 'pm list packages'"
            ),
            "target_mode" to ToolSchemaProperty(
                type = "STRING",
                description = "The target connection mode to run the command on: 'LOCAL', 'WIRELESS', or 'OTG'. Defaults to 'LOCAL'."
            )
        ),
        required = listOf("command")
    )

    override suspend fun execute(args: JsonObject?): String {
        val command = args?.get("command")?.jsonPrimitive?.content
            ?: return """{"error": "command is required"}"""
        val targetMode = args.get("target_mode")?.jsonPrimitive?.content?.uppercase() ?: "LOCAL"

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
            val outputFlow = when (targetMode) {
                "OTG" -> {
                    if (!otgRepository.isConnected()) return """{"error": "OTG is not connected"}"""
                    otgRepository.runOtgCommand(command)
                }

                "WIRELESS" -> {
                    val currentDevice = aiConnectionStateProvider.getWifiConnectedDeviceName()
                    if (currentDevice == null) return """{"error": "Wireless ADB is not connected"}"""
                    aiConnectionStateProvider.executeWifiCommand(command)
                }

                else -> {
                    val mode =
                        settingsRepository.getInt(SettingsKeys.LocalAdbWorkingMode).firstOrNull()
                            ?: SettingsKeys.LocalAdbWorkingMode.default
                    when (mode) {
                        LocalAdbWorkingMode.ROOT -> shellRepository.executeRootCommand(command)
                        LocalAdbWorkingMode.SHIZUKU -> shellRepository.executeShizukuCommand(command)
                        LocalAdbWorkingMode.TCPIP -> aiConnectionStateProvider.executeWifiCommand(
                            command
                        )
                        else -> shellRepository.executeBasicCommand(command)
                    }
                }
            }

            val outputBuilder = StringBuilder()

            val timeoutResult = withTimeoutOrNull(15000.milliseconds) {
                outputFlow.collect { line ->
                    if (isCancelled) {
                        when (targetMode) {
                            "OTG" -> otgRepository.stopCommand()
                            "WIRELESS" -> aiConnectionStateProvider.stopWifiCommand()
                            else -> shellRepository.stopCommand()
                        }
                        return@collect
                    }
                    outputBuilder.appendLine(line.text)
                }
                true
            }

            if (timeoutResult == null) {
                when (targetMode) {
                    "OTG" -> otgRepository.stopCommand()
                    "WIRELESS" -> aiConnectionStateProvider.stopWifiCommand()
                    else -> shellRepository.stopCommand()
                }
                outputBuilder.appendLine("\n<Command execution timed out after 15s. Process stopped.>")
            }

            if (isCancelled) {
                return """{"status": "cancelled", "output": "Task was cancelled by the user."}"""
            }

            val cleanOutput = outputBuilder.toString().replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n")
            return """{"status": "success", "output": "$cleanOutput"}"""
        } catch (e: Exception) {
            val cleanError =
                e.message?.replace("\\", "\\\\")?.replace("\"", "\\\"")?.replace("\n", "\\n")
            return """{"error": "$cleanError"}"""
        } finally {
            commandExecutionManager.removeRunningTask(taskId)
        }
    }
}
