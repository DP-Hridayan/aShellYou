package `in`.hridayan.ashell.ai.domain.tool

import `in`.hridayan.ashell.ai.domain.repository.CommandPermissionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class PermissionResult {
    GRANTED,
    DENIED
}

data class PermissionRequest(
    val command: String,
    val baseCommand: String,
    val deferredResult: CompletableDeferred<PermissionResult>
)

data class RunningTask(
    val id: String,
    val name: String,
    val sessionId: String? = null,
    // Add logic to cancel the task
    val onCancel: () -> Unit
)

@Singleton
class CommandExecutionManager @Inject constructor(
    private val permissionRepository: CommandPermissionRepository
) {
    private val _permissionRequest = MutableStateFlow<PermissionRequest?>(null)
    val permissionRequest: StateFlow<PermissionRequest?> = _permissionRequest.asStateFlow()

    private val _runningTasks = MutableStateFlow<List<RunningTask>>(emptyList())
    val runningTasks: StateFlow<List<RunningTask>> = _runningTasks.asStateFlow()

    private val safeCommands = setOf(
        "ls", "cat", "grep", "find", "dumpsys", "logcat", "getprop", 
        "ps", "pwd", "date", "whoami", "echo", "which", "whereis", 
        "uptime", "free", "top", "df", "du", "stat", "file", 
        "uname", "dmesg", "ping", "netstat", "ip", "ifconfig", 
        "id", "groups", "help"
    )

    suspend fun requestPermission(command: String): PermissionResult {
        // Extract base command (first word)
        val baseCommand = command.trim().substringBefore(" ")
        
        // Auto-allow completely safe read-only commands
        if (baseCommand.isNotBlank() && safeCommands.contains(baseCommand)) {
            return PermissionResult.GRANTED
        }

        // Check if exact command is always allowed
        if (permissionRepository.isCommandAlwaysAllowed(command)) {
            return PermissionResult.GRANTED
        }
        
        // Check if base command is always allowed
        if (baseCommand.isNotBlank() && permissionRepository.isCommandAlwaysAllowed(baseCommand)) {
            return PermissionResult.GRANTED
        }

        // Suspend and wait for user response
        val deferred = CompletableDeferred<PermissionResult>()
        _permissionRequest.value = PermissionRequest(command, baseCommand, deferred)
        
        val result = deferred.await()
        _permissionRequest.value = null // clear prompt
        
        return result
    }

    suspend fun handlePermissionResponse(command: String, isAllowed: Boolean, alwaysAllowExact: Boolean, alwaysAllowBase: Boolean = false) {
        val baseCommand = command.trim().substringBefore(" ")
        if (isAllowed) {
            if (alwaysAllowExact) {
                permissionRepository.setCommandAlwaysAllowed(command, true)
            }
            if (alwaysAllowBase && baseCommand.isNotBlank()) {
                permissionRepository.setCommandAlwaysAllowed(baseCommand, true)
            }
        }
        
        _permissionRequest.value?.let { request ->
            if (request.command == command) {
                request.deferredResult.complete(if (isAllowed) PermissionResult.GRANTED else PermissionResult.DENIED)
            }
        }
    }

    fun addRunningTask(name: String, sessionId: String? = null, onCancel: () -> Unit): String {
        val id = UUID.randomUUID().toString()
        val task = RunningTask(id, name, sessionId, onCancel)
        _runningTasks.update { it + task }
        return id
    }

    fun removeRunningTask(id: String) {
        _runningTasks.update { tasks -> tasks.filter { it.id != id } }
    }
    
    fun cancelTask(id: String) {
        _runningTasks.value.find { it.id == id }?.onCancel?.invoke()
        removeRunningTask(id)
    }
}
