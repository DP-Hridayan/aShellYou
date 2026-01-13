package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiAdbCommandExecutor @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AdbCommandExecutor {

    companion object{
        const val TAG = "WifiAdbExecutor"
    }

    private fun getAdbManager(): AbsAdbConnectionManager =
        AdbConnectionManager.getInstance(context)

    override fun isConnected(): Boolean = try {
        getAdbManager().isConnected
    } catch (e: Exception) {
        Log.e(TAG, "Error checking ADB connection", e)
        false
    }

    override suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        var stream: io.github.muntashirakon.adb.AdbStream? = null
        try {
            val adbManager = getAdbManager()
            if (!adbManager.isConnected) return@withContext null

            stream = adbManager.openStream("shell:$command")
            val inputStream = stream.openInputStream()

            val buffer = ByteArray(4096)
            val output = StringBuilder()
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.append(String(buffer, 0, bytesRead, Charsets.UTF_8))
                if (output.contains("__END__")) break
            }

            try { inputStream.close() } catch (_: Exception) {}
            try { stream.close() } catch (_: Exception) {}

            output.toString()
        } catch (e: Exception) {
            Log.e(TAG, "executeCommand failed: $command - ${e.message}")
            try { stream?.close() } catch (_: Exception) {}
            null
        }
    }

    override fun openCommandStream(command: String): CommandStream? = try {
        val adbManager = getAdbManager()
        if (!adbManager.isConnected) return null

        val stream = adbManager.openStream(command)
        val inputStream = stream.openInputStream()

        CommandStream(
            inputStream = inputStream,
            close = {
                try { inputStream.close() } catch (_: Exception) {}
                try { stream.close() } catch (_: Exception) {}
            }
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error opening command stream: $command", e)
        null
    }

    override fun openReadStream(command: String): FileTransferStream? = try {
        val adbManager = getAdbManager()
        if (!adbManager.isConnected) return null

        val stream = adbManager.openStream(command)
        val inputStream = stream.openInputStream().buffered(65536)

        FileTransferStream(
            inputStream = inputStream,
            close = {
                try { inputStream.close() } catch (_: Exception) {}
                try { stream.close() } catch (_: Exception) {}
            }
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error opening read stream: $command", e)
        null
    }

    override fun openWriteStream(command: String): FileTransferStream? = try {
        val adbManager = getAdbManager()
        if (!adbManager.isConnected) return null

        val stream = adbManager.openStream(command)
        val outputStream = stream.openOutputStream().buffered(65536)

        FileTransferStream(
            outputStream = outputStream,
            close = {
                try { outputStream.flush() } catch (_: Exception) {}
                try { outputStream.close() } catch (_: Exception) {}
                try { stream.close() } catch (_: Exception) {}
            }
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error opening write stream: $command", e)
        null
    }
}
