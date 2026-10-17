package `in`.hridayan.ashell.portforward.domain.repository

/** Finds out which local ports other apps already hold. */
interface PortAvailability {

    suspend fun isFree(port: Int): Boolean

    /** The first free port after [port], or null when none is free nearby. */
    suspend fun nextFree(port: Int): Int?
}
