package `in`.hridayan.ashell.shell.fastboot.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import `in`.hridayan.ashell.shell.fastboot.data.file.FastbootImageSource
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootCommandResult
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootError
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import `in`.hridayan.fastboot.FastbootCommand
import `in`.hridayan.fastboot.FastbootDeviceContext
import `in`.hridayan.fastboot.FastbootException
import `in`.hridayan.fastboot.FastbootResponse
import `in`.hridayan.fastboot.FastbootStage
import `in`.hridayan.fastboot.FastbootTransferListener
import `in`.hridayan.fastboot.ResponseStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.util.Locale

/**
 * Runs the operations that act on a partition: flashing an image, booting one, and erasing.
 *
 * The image is streamed from storage rather than read into memory, because a partition image can
 * be larger than the heap the app is allowed.
 */
class FastbootImageFlasher(private val context: Context) {

    fun flash(
        deviceContext: FastbootDeviceContext?,
        partition: String,
        imageUri: Uri,
        onProgress: (FlashOperation) -> Unit,
    ): Flow<FastbootCommandResult> = imageOperation(
        deviceContext = deviceContext,
        request = ImageRequest(FastbootCommand.flash(partition), "flash:$partition", partition),
        imageUri = imageUri,
        onProgress = onProgress,
    )

    fun boot(
        deviceContext: FastbootDeviceContext?,
        imageUri: Uri,
        onProgress: (FlashOperation) -> Unit,
    ): Flow<FastbootCommandResult> = imageOperation(
        deviceContext = deviceContext,
        request = ImageRequest(FastbootCommand.boot(), BOOT_COMMAND, BOOT_PARTITION),
        imageUri = imageUri,
        onProgress = onProgress,
    )

    fun erase(
        deviceContext: FastbootDeviceContext?,
        partition: String,
        onProgress: (FlashOperation) -> Unit,
    ): Flow<FastbootCommandResult> = flow {
        val commandText = "erase:$partition"
        if (deviceContext == null) {
            emitFailure(commandText, Target(partition), FastbootError.NO_DEVICE_CONNECTED, null, onProgress)
            return@flow
        }
        onProgress(FlashOperation(partition = partition, status = FlashStatus.ERASING))
        runCommand(deviceContext, FastbootCommand.erase(partition), commandText, Target(partition), onProgress)
    }.flowOn(Dispatchers.IO)

    private fun imageOperation(
        deviceContext: FastbootDeviceContext?,
        request: ImageRequest,
        imageUri: Uri,
        onProgress: (FlashOperation) -> Unit,
    ): Flow<FastbootCommandResult> = flow {
        val target = Target(request.partition)
        if (deviceContext == null) {
            emitFailure(request.commandText, target, FastbootError.NO_DEVICE_CONNECTED, null, onProgress)
            return@flow
        }
        onProgress(FlashOperation(partition = request.partition, status = FlashStatus.READING_FILE))
        val source = FastbootImageSource.from(context, imageUri)
        if (source == null) {
            emitFailure(request.commandText, target, FastbootError.FILE_OPEN_FAILED, null, onProgress)
            return@flow
        }
        transferImage(deviceContext, request, source, onProgress)
    }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<FastbootCommandResult>.transferImage(
        deviceContext: FastbootDeviceContext,
        request: ImageRequest,
        source: FastbootImageSource,
        onProgress: (FlashOperation) -> Unit,
    ) {
        val target = Target(request.partition, source.name)
        val limit = maxDownloadSize(deviceContext)
        if (limit != null && source.length > limit) {
            emitFailure(request.commandText, target, FastbootError.IMAGE_TOO_LARGE, megabytes(limit), onProgress)
            return
        }
        val listener = FlashProgressListener(request.partition, source.name, source.length, onProgress)
        runCommand(deviceContext, request.command, request.commandText, target, onProgress, source, listener)
    }

    private suspend fun FlowCollector<FastbootCommandResult>.runCommand(
        deviceContext: FastbootDeviceContext,
        command: FastbootCommand,
        commandText: String,
        target: Target,
        onProgress: (FlashOperation) -> Unit,
        source: FastbootImageSource? = null,
        listener: FlashProgressListener? = null,
    ) {
        try {
            val response = if (source == null) {
                deviceContext.sendCommand(command)
            } else {
                deviceContext.sendCommand(command, source, listener)
            }
            onProgress(outcome(target, response))
            emit(FastbootCommandResult(commandText, response.status, response.data))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "$commandText failed", e)
            emitFailure(commandText, target, errorFor(e), e.message, onProgress)
        }
    }

    private suspend fun FlowCollector<FastbootCommandResult>.emitFailure(
        commandText: String,
        target: Target,
        error: FastbootError,
        detail: String?,
        onProgress: (FlashOperation) -> Unit,
    ) {
        onProgress(
            FlashOperation(
                partition = target.partition,
                fileName = target.fileName,
                status = FlashStatus.ERROR,
                error = error,
                errorDetail = detail,
            )
        )
        emit(FastbootCommandResult(commandText, ResponseStatus.FAIL, detail ?: error.name))
    }

    private fun errorFor(failure: Exception): FastbootError = when (failure) {
        is FastbootException -> FastbootError.TRANSFER_FAILED
        is IOException -> FastbootError.FILE_READ_FAILED
        else -> FastbootError.UNKNOWN
    }

    private fun outcome(target: Target, response: FastbootResponse): FlashOperation = if (response.isOkay) {
        FlashOperation(
            partition = target.partition,
            fileName = target.fileName,
            progress = 1f,
            status = FlashStatus.COMPLETE,
        )
    } else {
        FlashOperation(
            partition = target.partition,
            fileName = target.fileName,
            status = FlashStatus.ERROR,
            error = FastbootError.DEVICE_REJECTED,
            errorDetail = response.data,
        )
    }

    /**
     * The bootloader refuses a download larger than its buffer, so the image is measured against
     * the limit first and reported clearly instead of failing with the device's terse message.
     */
    private fun maxDownloadSize(deviceContext: FastbootDeviceContext): Long? = runCatching {
        val response = deviceContext.sendCommand(FastbootCommand.getVar(MAX_DOWNLOAD_SIZE_VAR))
        if (response.isOkay) parseSize(response.data) else null
    }.getOrNull()

    private fun parseSize(raw: String): Long? {
        val text = raw.lines().lastOrNull { it.isNotBlank() }?.trim()?.lowercase(Locale.US) ?: return null
        return if (text.startsWith(HEX_PREFIX)) {
            text.removePrefix(HEX_PREFIX).toLongOrNull(HEX_RADIX)
        } else {
            text.toLongOrNull()
        }
    }

    private fun megabytes(bytes: Long): String =
        String.format(Locale.getDefault(), "%.1f", bytes / BYTES_PER_MEGABYTE)

    private data class ImageRequest(
        val command: FastbootCommand,
        val commandText: String,
        val partition: String,
    )

    private data class Target(val partition: String, val fileName: String = "")

    private class FlashProgressListener(
        private val partition: String,
        private val fileName: String,
        private val totalBytes: Long,
        private val publish: (FlashOperation) -> Unit,
    ) : FastbootTransferListener {

        override fun onStage(stage: FastbootStage) {
            val writing = stage == FastbootStage.WRITING
            val status = if (writing) FlashStatus.WRITING else FlashStatus.DOWNLOADING
            publish(operation(status, if (writing) totalBytes else 0L))
        }

        override fun onProgress(bytesSent: Long, totalBytes: Long) {
            publish(operation(FlashStatus.DOWNLOADING, bytesSent))
        }

        private fun operation(status: FlashStatus, sent: Long): FlashOperation = FlashOperation(
            partition = partition,
            fileName = fileName,
            status = status,
            progress = if (totalBytes <= 0L) 0f else sent.toFloat() / totalBytes.toFloat(),
            bytesSent = sent,
            totalBytes = totalBytes,
        )
    }

    private companion object {
        const val TAG = "FastbootImageFlasher"
        const val BOOT_PARTITION = "boot"
        const val BOOT_COMMAND = "boot"
        const val MAX_DOWNLOAD_SIZE_VAR = "max-download-size"
        const val HEX_PREFIX = "0x"
        const val HEX_RADIX = 16
        const val BYTES_PER_MEGABYTE = 1024.0 * 1024.0
    }
}
