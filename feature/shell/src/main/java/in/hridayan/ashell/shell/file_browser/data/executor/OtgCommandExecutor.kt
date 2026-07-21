package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.util.Log
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtgCommandExecutor @Inject constructor(
    private val otgRepository: OtgRepository
) : AdbCommandExecutor {

    companion object {
        const val TAG = "OtgCommandExecutor"
    }

    override fun isConnected(): Boolean = otgRepository.isConnected()

    override fun supportsSyncTransfer(): Boolean = false

    override suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        if (!isConnected()) return@withContext null

        val adbConnection = otgRepository.getAdbConnection() ?: return@withContext null

        withTimeoutOrNull(15000L) {
            val stream = adbConnection.open("shell:$command")
            try {
                val output = StringBuilder()
                while (true) {
                    val data = stream.read()
                    if (data == null || data.isEmpty()) break
                    output.append(String(data, Charsets.UTF_8))
                    if (output.contains("__END__")) break
                }
                output.toString()
            } finally {
                try {
                    stream.close()
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun openCommandStream(command: String): CommandStream? {
        if (!isConnected()) return null
        val adbConnection = otgRepository.getAdbConnection() ?: return null

        return try {
            val stream = adbConnection.open(command)
            val output = ByteArrayOutputStream()

            while (true) {
                val data = stream.read() ?: break
                if (data.isEmpty()) break
                output.write(data)
            }

            val resultStream = ByteArrayInputStream(output.toByteArray())
            CommandStream(
                inputStream = resultStream,
                close = {
                    try {
                        stream.close()
                    } catch (_: Exception) {
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "openCommandStream failed", e)
            null
        }
    }

    override fun openReadStream(command: String): FileTransferStream? = null

    override fun openWriteStream(command: String): FileTransferStream? = null
}
