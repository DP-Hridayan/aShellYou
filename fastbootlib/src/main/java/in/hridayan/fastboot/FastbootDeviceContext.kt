package `in`.hridayan.fastboot

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.util.Log
import java.nio.charset.StandardCharsets

/**
 * Provides an active session with a connected fastboot device.
 * Commands are sent as ASCII strings over USB bulk OUT and responses
 * are read from USB bulk IN.
 *
 * The fastboot protocol is simple:
 * - Host sends command (ASCII string, max 4096 bytes) via bulk OUT
 * - Device responds with 4-byte status prefix (OKAY/FAIL/DATA/INFO) + optional data via bulk IN
 * - For DATA responses, host sends raw bytes, then device responds with final OKAY/FAIL
 */
class FastbootDeviceContext constructor(
    private val connection: UsbDeviceConnection,
    private val usbInterface: UsbInterface,
    private val inEndpoint: UsbEndpoint,
    private val outEndpoint: UsbEndpoint
) {
    companion object {
        private const val TAG = "FastbootDeviceContext"
        private const val RESPONSE_TIMEOUT_MS = 5000
        private const val DATA_TRANSFER_TIMEOUT_MS = 30000
        private const val MAX_RESPONSE_SIZE = 256
        private const val STATUS_PREFIX_LENGTH = 4
    }

    /**
     * Send a fastboot command and return the response.
     * For commands that trigger INFO messages, all INFO messages are collected
     * and the final OKAY/FAIL response is returned with accumulated info.
     *
     * @param command The FastbootCommand to send
     * @return FastbootResponse containing the status and data
     * @throws FastbootException if communication fails
     */
    fun sendCommand(command: FastbootCommand): FastbootResponse {
        // Handle commands with data payload (flash, boot)
        if (command.data != null) {
            return sendCommandWithData(command)
        }

        // Send the command string
        val cmdBytes = command.command.toByteArray(StandardCharsets.UTF_8)
        val sent = connection.bulkTransfer(outEndpoint, cmdBytes, cmdBytes.size, RESPONSE_TIMEOUT_MS)
        if (sent < 0) {
            throw FastbootException("Failed to send command: ${command.command}")
        }

        // Read response(s) - collect INFO messages, return on OKAY/FAIL
        return readResponse()
    }

    /**
     * Send a command that includes a data payload (e.g., flash, boot).
     * Protocol: send "download:<hex_size>" -> wait for DATA -> send raw bytes -> wait for OKAY
     *         then send actual command (e.g., "flash:boot") -> wait for OKAY
     */
    private fun sendCommandWithData(command: FastbootCommand): FastbootResponse {
        return sendCommandWithData(command, null)
    }

    /**
     * Send a command with data payload and optional progress reporting.
     * @param onProgress Called with (bytesSent, totalBytes) during data transfer
     */
    private fun sendCommandWithData(
        command: FastbootCommand,
        onProgress: ((Long, Long) -> Unit)?
    ): FastbootResponse {
        val data = command.data!!
        val hexSize = String.format("%08x", data.size)

        // Step 1: Send download command
        val downloadCmd = "download:$hexSize".toByteArray(StandardCharsets.UTF_8)
        val sent = connection.bulkTransfer(outEndpoint, downloadCmd, downloadCmd.size, RESPONSE_TIMEOUT_MS)
        if (sent < 0) {
            throw FastbootException("Failed to send download command")
        }

        // Step 2: Wait for DATA response
        val dataResponse = readSingleResponse()
        if (dataResponse.status != ResponseStatus.DATA) {
            return dataResponse // Error - device rejected download
        }

        // Step 3: Send the actual data in chunks with progress
        val chunkSize = outEndpoint.maxPacketSize
        var offset = 0
        val totalSize = data.size.toLong()
        while (offset < data.size) {
            val remaining = data.size - offset
            val transferSize = minOf(remaining, chunkSize)
            val chunk = data.copyOfRange(offset, offset + transferSize)
            val transferred = connection.bulkTransfer(outEndpoint, chunk, chunk.size, DATA_TRANSFER_TIMEOUT_MS)
            if (transferred < 0) {
                throw FastbootException("Failed to send data at offset $offset")
            }
            offset += transferred
            onProgress?.invoke(offset.toLong(), totalSize)
        }

        // Step 4: Wait for download confirmation
        val downloadConfirm = readResponse()
        if (!downloadConfirm.isOkay) {
            return downloadConfirm
        }

        // Step 5: Send the actual command (e.g., "flash:boot")
        val cmdBytes = command.command.toByteArray(StandardCharsets.UTF_8)
        val cmdSent = connection.bulkTransfer(outEndpoint, cmdBytes, cmdBytes.size, RESPONSE_TIMEOUT_MS)
        if (cmdSent < 0) {
            throw FastbootException("Failed to send flash command: ${command.command}")
        }

        // Step 6: Wait for final response
        return readResponse()
    }

    /**
     * Send a fastboot command with progress reporting.
     * Use this for flash/boot commands that transfer large data payloads.
     *
     * @param command The FastbootCommand to send
     * @param onProgress Called with (bytesSent, totalBytes) during data transfer. May be null.
     * @return FastbootResponse containing the status and data
     * @throws FastbootException if communication fails
     */
    fun sendCommand(
        command: FastbootCommand,
        onProgress: ((Long, Long) -> Unit)?
    ): FastbootResponse {
        if (command.data != null && onProgress != null) {
            return sendCommandWithData(command, onProgress)
        }
        return sendCommand(command)
    }

    /**
     * Read responses from the device, collecting INFO messages.
     * Returns the final OKAY or FAIL response with accumulated info text.
     */
    private fun readResponse(): FastbootResponse {
        val infoMessages = mutableListOf<String>()

        while (true) {
            val response = readSingleResponse()
            when (response.status) {
                ResponseStatus.INFO -> {
                    infoMessages.add(response.data)
                }
                else -> {
                    // Final response - include accumulated INFO messages
                    val fullData = if (infoMessages.isNotEmpty()) {
                        (infoMessages + response.data).filter { it.isNotEmpty() }.joinToString("\n")
                    } else {
                        response.data
                    }
                    return FastbootResponse(response.status, fullData)
                }
            }
        }
    }

    /**
     * Read a single response packet from the device.
     */
    private fun readSingleResponse(): FastbootResponse {
        val buffer = ByteArray(MAX_RESPONSE_SIZE)
        val received = connection.bulkTransfer(
            inEndpoint, buffer, buffer.size, RESPONSE_TIMEOUT_MS
        )

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

    /**
     * Close this device context and release USB resources.
     */
    fun close() {
        try {
            connection.releaseInterface(usbInterface)
            connection.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing device context", e)
        }
    }
}

/**
 * Exception thrown when fastboot communication fails.
 */
class FastbootException(message: String, cause: Throwable? = null) : Exception(message, cause)
