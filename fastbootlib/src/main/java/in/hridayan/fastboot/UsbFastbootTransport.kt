package `in`.hridayan.fastboot

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.util.Log

/**
 * Carries the fastboot byte stream over a claimed USB interface.
 *
 * A bootloader that is busy erasing or writing stops servicing its endpoints for as long as the
 * operation takes, so a transfer that consumes its whole timeout is retried until an overall
 * deadline rather than treated as a failure.
 */
class UsbFastbootTransport(
    private val connection: UsbDeviceConnection,
    private val usbInterface: UsbInterface,
    private val inEndpoint: UsbEndpoint,
    private val outEndpoint: UsbEndpoint,
) : FastbootTransport {

    override fun writeFully(data: ByteArray, offset: Int, length: Int) {
        var sent = 0
        var failures = 0
        val deadline = System.currentTimeMillis() + TRANSFER_DEADLINE_MS
        while (sent < length) {
            val wanted = minOf(length - sent, MAX_TRANSFER_BYTES)
            val startedAt = System.currentTimeMillis()
            val transferred = connection.bulkTransfer(
                outEndpoint,
                data,
                offset + sent,
                wanted,
                TRANSFER_TIMEOUT_MS
            )
            if (transferred > 0) {
                sent += transferred
                failures = 0
                continue
            }
            failures = onFailedTransfer(transferred, startedAt, failures, deadline)
        }
    }

    override fun readPacket(buffer: ByteArray): Int {
        val deadline = System.currentTimeMillis() + RESPONSE_DEADLINE_MS
        while (true) {
            val startedAt = System.currentTimeMillis()
            val received = connection.bulkTransfer(inEndpoint, buffer, buffer.size, TRANSFER_TIMEOUT_MS)
            if (received > 0) return received
            val elapsed = System.currentTimeMillis() - startedAt
            if (received < 0 && elapsed < FAILURE_THRESHOLD_MS) {
                throw FastbootException("USB read failed; the device may have been disconnected")
            }
            if (System.currentTimeMillis() >= deadline) {
                throw FastbootException("Timed out waiting for a response from the device")
            }
        }
    }

    override fun close() {
        try {
            connection.releaseInterface(usbInterface)
            connection.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing fastboot transport", e)
        }
    }

    private fun onFailedTransfer(transferred: Int, startedAt: Long, failures: Int, deadline: Long): Int {
        val elapsed = System.currentTimeMillis() - startedAt
        if (transferred < 0 && elapsed < FAILURE_THRESHOLD_MS) {
            val attempts = failures + 1
            if (attempts > MAX_TRANSIENT_FAILURES) {
                throw FastbootException("USB write failed $attempts times in a row")
            }
            backOff()
            return attempts
        }
        if (System.currentTimeMillis() >= deadline) {
            throw FastbootException("Timed out sending data to the device")
        }
        return failures
    }

    private fun backOff() {
        try {
            Thread.sleep(TRANSIENT_BACKOFF_MS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw FastbootException("Interrupted while sending data", e)
        }
    }

    private companion object {
        const val TAG = "UsbFastbootTransport"
        const val TRANSFER_TIMEOUT_MS = 5_000
        const val TRANSFER_DEADLINE_MS = 10L * 60L * 1_000L
        const val RESPONSE_DEADLINE_MS = 10L * 60L * 1_000L
        const val FAILURE_THRESHOLD_MS = TRANSFER_TIMEOUT_MS / 2
        const val MAX_TRANSFER_BYTES = 256 * 1024
        const val MAX_TRANSIENT_FAILURES = 5
        const val TRANSIENT_BACKOFF_MS = 20L
    }
}
