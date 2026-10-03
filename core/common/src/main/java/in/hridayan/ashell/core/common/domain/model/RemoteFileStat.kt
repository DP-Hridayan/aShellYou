package `in`.hridayan.ashell.core.common.domain.model

/**
 * Metadata of a path on another device, as reported by the ADB `sync:` protocol.
 *
 * @property sizeBytes the file size, meaningful only when [exists] is true.
 * @property exists false when the device reported no entry at that path.
 */
data class RemoteFileStat(
    val sizeBytes: Long,
    val exists: Boolean
)
