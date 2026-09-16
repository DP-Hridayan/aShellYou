package `in`.hridayan.fastboot

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.util.Log
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Provides an active session with a connected fastboot device.
 * Commands are sent as ASCII strings over USB bulk OUT and responses
 * are read from USB bulk IN.
 *
 * The fastboot protocol is strictly request/response, so commands are serialised on a lock:
 * concurrent callers queue instead of interleaving their bytes on the endpoints.
 *
 * Long-running commands (erase, flash writes) may take minutes before the device answers.
 * Reads are therefore polled in short slices until [RESPONSE_DEADLINE_MS] elapses, and can be
 * interrupted through [abort] or [close].
 */
class FastbootDeviceContext constructor(
    private val connection: UsbDeviceConnection,
    private val usbInterface: UsbInterface,
    private val inEndpoint: UsbEndpoint,
    private val outEndpoint: UsbEndpoint
) {
    companion object {
        private const val TAG = "FastbootDeviceContext"
        private const val RESPONSE_POLL_TIMEOUT_MS = 5_000
        private const val RESPONSE_DEADLINE_MS = 10L * 60L * 1_000L
        private const val READ_FAILURE_THRESHOLD_MS = RESPONSE_POLL_TIMEOUT_MS / 2
        private const val DATA_TRANSFER_TIMEOUT_MS = 30_000
        private const val DATA_CHUNK_SIZE = 1024 * 1024
        private const val MAX_RESPONSE_SIZE = 256
        private const val STATUS_PREFIX_LENGTH = 4
    }

    private val commandLock = ReentrantLock()

    @Volatile
    private var aborted = false

    @Volatile
    private var closed = false

    /** False once [close] has been called. */
    val isOpen: Boolean get() = !closed

    /**
     * Send a fastboot command and return the response.
     * For commands that trigger INFO messages, all INFO messages are collected
     * and the final OKAY/FAIL response is returned with accumulated info.
     *
     * @throws FastbootException if communication fails, the command is aborted, or the context is closed
     */
    fun sendCommand(command: FastbootCommand): FastbootResponse = sendCommand(command, null)

    /**
     * Send a fastboot command with progress reporting for data payloads (flash, boot).
     *
     * @param onProgress Called with (bytesSent, totalBytes) during data transfer. May be null.
     */
    fun sendCommand(
        command: FastbootCommand,
        onProgress: ((Long, Long) -> Unit)?
    ): FastbootResponse = commandLock.withLock {
        aborted = false
        ensureUsable()
        val data = command.data
        if (data != null) {
            sendCommandWithData(command.command, data, onProgress)
        } else {
            writeCommand(command.command)
            readResponse()
        }
    }

    /**
     * Interrupts the command currently in flight; its caller receives a [FastbootException].
     * The device may be left in the middle of a transfer, so the context should be closed and
     * reopened before sending further commands.
     */
    fun abort() {
        aborted = true
    }

    /**
     * Close this device context and release USB resources. Any command in flight fails.
     */
    fun close() {
        closed = true
        try {
            connection.releaseInterface(usbInterface)
            connection.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing device context", e)
        }
    }

    private fun writeCommand(command: String) {
        val bytes = command.toByteArray(StandardCharsets.UTF_8)
        val sent = connection.bulkTransfer(outEndpoint, bytes, bytes.size, RESPONSE_POLL_TIMEOUT_MS)
        if (sent < 0) {
            throw FastbootException("Failed to send command: $command")
        }
    }

    private fun sendCommandWithData(
        command: String,
        data: ByteArray,
        onProgress: ((Long, Long) -> Unit)?
    ): FastbootResponse {
        writeCommand("download:" + String.format(Locale.US, "%08x", data.size))

        val dataResponse = readSingleResponse()
        if (dataResponse.status != ResponseStatus.DATA) {
            return dataResponse
        }

        writeData(data, onProgress)

        val downloadConfirm = readResponse()
        if (!downloadConfirm.isOkay) {
            return downloadConfirm
        }

        writeCommand(command)
        return readResponse()
    }

    private fun writeData(data: ByteArray, onProgress: ((Long, Long) -> Unit)?) {
        val total = data.size.toLong()
        var offset = 0
        while (offset < data.size) {
            ensureUsable()
            val length = minOf(data.size - offset, DATA_CHUNK_SIZE)
            val transferred = connection.bulkTransfer(
                outEndpoint,
                data,
                offset,
                length,
                DATA_TRANSFER_TIMEOUT_MS
            )
            if (transferred <= 0) {
                throw FastbootException("Failed to send data at offset $offset")
            }
            offset += transferred
            onProgress?.invoke(offset.toLong(), total)
        }
    }

    private fun readResponse(): FastbootResponse {
        val infoMessages = mutableListOf<String>()

        while (true) {
            val response = readSingleResponse()
            if (response.status == ResponseStatus.INFO) {
                infoMessages.add(response.data)
                continue
            }
            val fullData = if (infoMessages.isNotEmpty()) {
                (infoMessages + response.data).filter { it.isNotEmpty() }.joinToString("\n")
            } else {
                response.data
            }
            return FastbootResponse(response.status, fullData)
        }
    }

    private fun readSingleResponse(): FastbootResponse {
        val buffer = ByteArray(MAX_RESPONSE_SIZE)
        val received = awaitResponse(buffer)

        if (received < STATUS_PREFIX_LENGTH) {
            throw FastbootException("Invalid response: received $received bytes (need at least $STATUS_PREFIX_LENGTH)")
        }

        val prefix = String(buffer, 0, STATUS_PREFIX_LENGTH, StandardCharsets.UTF_8)
        val status = ResponseStatus.fromPrefix(prefix)
            ?: throw FastbootException("Unknown response status: $prefix")

        val data = if (received > STATUS_PREFIX_LENGTH) {
            String(buffer, STATUS_PREFIX_LENGTH, received - STATUS_PREFIX_LENGTH, StandardCharsets.UTF_8)
        } else {
            ""
        }

        Log.d(TAG, "Response: $prefix$data")
        return FastbootResponse(status, data)
    }

    private fun awaitResponse(buffer: ByteArray): Int {
        val deadline = System.currentTimeMillis() + RESPONSE_DEADLINE_MS
        while (true) {
            ensureUsable()
            val startedAt = System.currentTimeMillis()
            val received = connection.bulkTransfer(inEndpoint, buffer, buffer.size, RESPONSE_POLL_TIMEOUT_MS)
            if (received > 0) {
                return received
            }
            val elapsed = System.currentTimeMillis() - startedAt
            if (received < 0 && elapsed < READ_FAILURE_THRESHOLD_MS) {
                throw FastbootException("USB read failed; the device may have been disconnected")
            }
            if (System.currentTimeMillis() >= deadline) {
                throw FastbootException("Timed out waiting for a response from the device")
            }
        }
    }

    private fun ensureUsable() {
        if (closed) throw FastbootException("Device connection is closed")
        if (aborted) throw FastbootException("Command aborted")
    }
}

/**
 * Exception thrown when fastboot communication fails.
 */
class FastbootException(message: String, cause: Throwable? = null) : Exception(message, cause)
