package `in`.hridayan.ashell.core.shizuku.domain

/**
 * Connection state of the privileged Shizuku helper service.
 */
sealed interface ShizukuServiceState {
    data object Idle : ShizukuServiceState
    data object Binding : ShizukuServiceState
    data class Ready(val uid: Int) : ShizukuServiceState
    data class Unavailable(val error: ShizukuServiceError) : ShizukuServiceState
}
