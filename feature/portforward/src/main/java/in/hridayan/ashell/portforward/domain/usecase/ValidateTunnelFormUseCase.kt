package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import javax.inject.Inject

/**
 * Checks the fields of the tunnel editor.
 *
 * Whether another app already holds the local port is not checked here, because that needs a trial
 * bind; only conflicts with the user's own tunnels are.
 */
class ValidateTunnelFormUseCase @Inject constructor() {

    /**
     * @property portsInUse local ports of the user's other tunnels, mapped to their names.
     */
    data class Input(
        val host: String,
        val targetPort: String,
        val localPort: String,
        val portsInUse: Map<Int, String> = emptyMap()
    )

    /**
     * @property endpointValue the port, socket name, socket path or process id, as typed.
     */
    data class DeviceInput(
        val endpointType: DeviceEndpointType,
        val endpointValue: String,
        val localPort: String,
        val portsInUse: Map<Int, String> = emptyMap()
    )

    sealed interface FieldError {
        data object Required : FieldError
        data object Invalid : FieldError
        data object Privileged : FieldError
        data class UsedByTunnel(val tunnelName: String) : FieldError
    }

    data class Result(
        val host: FieldError?,
        val targetPort: FieldError?,
        val localPort: FieldError?
    ) {
        val isValid: Boolean
            get() = host == null && targetPort == null && localPort == null
    }

    operator fun invoke(input: Input): Result = Result(
        host = hostError(input.host),
        targetPort = portError(input.targetPort),
        localPort = localPortError(input.localPort, input.portsInUse)
    )

    /** Checks a device tunnel; its endpoint error is reported as [Result.targetPort]. */
    fun device(input: DeviceInput): Result = Result(
        host = null,
        targetPort = endpointError(input.endpointType, input.endpointValue),
        localPort = localPortError(input.localPort, input.portsInUse)
    )

    private fun endpointError(type: DeviceEndpointType, value: String): FieldError? = when (type) {
        DeviceEndpointType.TCP -> portError(value)
        DeviceEndpointType.JDWP -> processIdError(value)
        DeviceEndpointType.ABSTRACT, DeviceEndpointType.FILESYSTEM -> socketNameError(value)
    }

    private fun processIdError(value: String): FieldError? = when {
        value.isBlank() -> FieldError.Required
        (value.trim().toIntOrNull() ?: 0) <= 0 -> FieldError.Invalid
        else -> null
    }

    private fun socketNameError(value: String): FieldError? = when {
        value.isBlank() -> FieldError.Required
        value.trim().any { it.isWhitespace() } -> FieldError.Invalid
        else -> null
    }

    private fun hostError(host: String): FieldError? = when {
        host.isBlank() -> FieldError.Required
        host.trim().any { it.isWhitespace() || it in FORBIDDEN_HOST_CHARACTERS } -> FieldError.Invalid
        else -> null
    }

    private fun portError(text: String): FieldError? = when {
        text.isBlank() -> FieldError.Required
        text.trim().toIntOrNull()?.takeIf { it in MIN_PORT..MAX_PORT } == null -> FieldError.Invalid
        else -> null
    }

    private fun localPortError(text: String, portsInUse: Map<Int, String>): FieldError? {
        portError(text)?.let { return it }
        val port = text.trim().toInt()
        return when {
            port < FIRST_UNPRIVILEGED_PORT -> FieldError.Privileged
            port in portsInUse -> FieldError.UsedByTunnel(portsInUse.getValue(port))
            else -> null
        }
    }

    private companion object {
        const val MIN_PORT = 1
        const val MAX_PORT = 65535
        const val FIRST_UNPRIVILEGED_PORT = 1024
        val FORBIDDEN_HOST_CHARACTERS = setOf('/', '?', '#', '@')
    }
}
