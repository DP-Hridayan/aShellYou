package `in`.hridayan.fastboot

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * An active session with a connected fastboot device.
 *
 * The protocol is strictly one command at a time, so commands are serialised on a lock: concurrent
 * callers queue instead of interleaving their bytes on the endpoints.
 *
 * [abort] permanently poisons the context rather than cancelling a single command. A device
 * abandoned part way through a download is still expecting the rest of it, so the only safe move
 * is to stop using the connection and open a new one.
 */
class FastbootDeviceContext(private val transport: FastbootTransport) {

    constructor(
        connection: UsbDeviceConnection,
        usbInterface: UsbInterface,
        inEndpoint: UsbEndpoint,
        outEndpoint: UsbEndpoint,
    ) : this(UsbFastbootTransport(connection, usbInterface, inEndpoint, outEndpoint))

    private val commandLock = ReentrantLock()
    private val session = FastbootSession(transport, ::ensureUsable)

    @Volatile
    private var aborted = false

    @Volatile
    private var closed = false

    /** False once [close] has been called. */
    val isOpen: Boolean get() = !closed && !aborted

    /**
     * Sends a command that carries no image data.
     *
     * @throws FastbootException if communication fails, or the context was aborted or closed
     */
    fun sendCommand(command: FastbootCommand): FastbootResponse = commandLock.withLock {
        ensureUsable()
        session.send(command.command)
    }

    /**
     * Downloads [source] to the device and then runs [command] against the downloaded image.
     *
     * @param listener notified as each stage begins and as bytes are sent
     * @throws FastbootException if communication fails, or the context was aborted or closed
     */
    fun sendCommand(
        command: FastbootCommand,
        source: FastbootDataSource,
        listener: FastbootTransferListener?,
    ): FastbootResponse = commandLock.withLock {
        ensureUsable()
        session.sendWithData(command.command, source, listener)
    }

    /**
     * Stops the exchange in flight and refuses further commands. The device is left mid transfer,
     * so the caller must close this context and establish a new connection.
     */
    fun abort() {
        aborted = true
    }

    /** Closes the context and releases the USB resources. Any command in flight fails. */
    fun close() {
        closed = true
        transport.close()
    }

    private fun ensureUsable() {
        if (closed) throw FastbootException("Device connection is closed")
        if (aborted) throw FastbootException("Operation was cancelled")
    }
}
