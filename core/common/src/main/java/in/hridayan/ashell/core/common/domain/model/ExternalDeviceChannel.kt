package `in`.hridayan.ashell.core.common.domain.model

import java.io.InputStream

/**
 * Raw ADB access to the device on the other end of one [ExternalDeviceTransport].
 *
 * Implementations live in :feature:shell, which owns the connections, and are provided through Hilt
 * `@Named` bindings so other features can open services and push files without depending on it.
 * Every failure is returned as a [Result], never thrown.
 */
interface ExternalDeviceChannel {

    /** True only while another device, never this one, is connected on this transport. */
    val isConnected: Boolean

    suspend fun openStream(service: String): Result<AdbDuplexStream>

    suspend fun stat(remotePath: String): Result<RemoteFileStat>

    /**
     * Streams [source] to [remotePath] over the `sync:` protocol. [mode] is the Unix permission bits
     * the file is created with.
     */
    suspend fun push(source: InputStream, remotePath: String, mode: Int): Result<Unit>
}
