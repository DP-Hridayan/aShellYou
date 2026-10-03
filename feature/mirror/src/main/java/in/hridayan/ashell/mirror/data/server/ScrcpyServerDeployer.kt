package `in`.hridayan.ashell.mirror.data.server

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

private const val SERVER_FILE_MODE = 420
private const val CLEANUP_TIMEOUT_MS = 5_000L

/** Supplies the bytes of the bundled server jar. */
fun interface ServerJarSource {
    fun read(): ByteArray
}

/** @property skipped true when the device already held an identical copy. */
data class DeployOutcome(val skipped: Boolean)

/**
 * Makes sure the bundled server is on the device, uploading it only when the device's copy is
 * missing or a different size, then deletes older versions this app left behind.
 *
 * @param remotePrefix only files starting with it are ever deleted, so nothing that another tool put
 * in the shared temp directory is touched.
 */
class ScrcpyServerDeployer(
    private val jarSource: ServerJarSource,
    private val remotePath: String,
    private val remotePrefix: String,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun deploy(channel: ExternalDeviceChannel): Result<DeployOutcome> =
        withContext(ioDispatcher) {
            try {
                Result.success(deployOrThrow(channel))
            } catch (e: TimeoutCancellationException) {
                Result.failure(e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun deployOrThrow(channel: ExternalDeviceChannel): DeployOutcome {
        val jar = jarSource.read()
        val existing = channel.stat(remotePath).getOrNull()
        if (existing?.exists == true && existing.sizeBytes == jar.size.toLong()) {
            return DeployOutcome(skipped = true)
        }

        channel.push(ByteArrayInputStream(jar), remotePath, SERVER_FILE_MODE).getOrThrow()
        removeOtherVersions(channel)
        return DeployOutcome(skipped = false)
    }

    /** Best effort: a leftover old version only costs storage. */
    private suspend fun removeOtherVersions(channel: ExternalDeviceChannel) {
        val command = "for f in $remotePrefix*.jar; do [ \"\$f\" = \"$remotePath\" ] || rm -f \"\$f\"; done"
        channel.runQuietly(command, CLEANUP_TIMEOUT_MS)
    }
}
