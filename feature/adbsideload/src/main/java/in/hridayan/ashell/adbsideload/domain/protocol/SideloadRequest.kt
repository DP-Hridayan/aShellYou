package `in`.hridayan.ashell.adbsideload.domain.protocol

/**
 * One decoded 8-byte frame sent by recovery over the `sideload-host` stream.
 */
sealed interface SideloadRequest {
    data class Block(val index: Int) : SideloadRequest
    data object Done : SideloadRequest
    data object Failed : SideloadRequest
    data class Malformed(val frame: String) : SideloadRequest
}
