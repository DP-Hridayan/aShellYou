package com.cgutman.adblib;

import java.io.Closeable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class abstracts the underlying ADB streams
 *
 * @author Cameron Gutman
 */
public class AdbStream implements Closeable {

	/** The AdbConnection object that the stream communicates over */
	private final AdbConnection adbConn;

	/** The local ID of the stream */
	private final int localId;

	/** The remote ID of the stream */
	private volatile int remoteId;

	/** Indicates whether a write is currently allowed */
	private final AtomicBoolean writeReady;

	/** A queue of data from the target's write packets */
	private final Queue<byte[]> readQueue;

	/** Indicates whether the connection is closed already */
	private volatile boolean isClosed;

	/** Indicates whether the peer has acknowledged the OPEN for this stream */
	private volatile boolean isOpen;

	/** Why the stream closed, when the connection reported a reason */
	private volatile String closeReason;

	/** How long a write waits for the peer to acknowledge the previous one. A peer that is busy
	 * installing legitimately takes minutes, so this only breaks a genuine hang. */
	private static final long WRITE_READY_TIMEOUT_MS = 10 * 60 * 1000;

	/**
	 * Creates a new AdbStream object on the specified AdbConnection
	 * with the given local ID.
	 *
	 * @param adbConn AdbConnection that this stream is running on
	 * @param localId Local ID of the stream
	 */
	public AdbStream(AdbConnection adbConn, int localId) {
		this.adbConn = adbConn;
		this.localId = localId;
		this.readQueue = new ConcurrentLinkedQueue<byte[]>();
		this.writeReady = new AtomicBoolean(false);
		this.isClosed = false;
		this.isOpen = false;
	}

	/**
	 * Called by the connection thread to indicate newly received data.
	 *
	 * @param payload Data inside the write message
	 */
	void addPayload(byte[] payload) {
		synchronized (readQueue) {
			readQueue.add(payload);
			readQueue.notifyAll();
		}
	}

	/**
	 * Called by the connection thread to send an OKAY packet, allowing the
	 * other side to continue transmission.
	 *
	 * @throws java.io.IOException If the connection fails while sending the packet
	 */
	void sendReady() throws IOException {
		adbConn.channel.writex(AdbProtocol.generateReady(localId, remoteId));
	}

	/**
	 * Called by the connection thread to update the remote ID for this stream
	 *
	 * @param remoteId New remote ID
	 */
	void updateRemoteId(int remoteId) {
		this.remoteId = remoteId;
	}

	/**
	 * Called by the connection thread to indicate the stream is okay to send data.
	 * The first call also marks the stream as open, which unblocks a pending open().
	 */
	void readyForWrite() {
		writeReady.set(true);
		synchronized (this) {
			isOpen = true;
			notifyAll();
		}
	}

	/**
	 * Called by the connection thread to notify that the stream was closed by the
	 * peer.
	 */
	void notifyClose() {
		notifyClose(null);
	}

	/**
	 * Called by the connection thread to notify that the stream was closed, recording why so
	 * that blocked readers and writers fail with something diagnosable.
	 *
	 * @param reason Description of the failure, or null when the peer closed the stream normally
	 */
	void notifyClose(String reason) {
		/* We don't call close() because it sends another CLOSE */
		if (reason != null) {
			closeReason = reason;
		}
		isClosed = true;

		/* Unwait readers and writers */
		synchronized (this) {
			notifyAll();
		}
		synchronized (readQueue) {
			readQueue.notifyAll();
		}
	}

	/**
	 * Reads a pending write payload from the other side.
	 *
	 * @return Byte array containing the payload of the write
	 * @throws InterruptedException If we are unable to wait for data
	 * @throws java.io.IOException  If the stream fails while waiting
	 */
	public byte[] read() throws InterruptedException, IOException {
		byte[] data = null;
		synchronized (readQueue) {
			/* Wait for the connection to close or data to be received */
			while (!isClosed && (data = readQueue.poll()) == null) {
				readQueue.wait();
			}

			if (data == null) {
				throw closedException();
			}
		}
		return data;
	}

	/**
	 * Sends a write packet with a given String payload.
	 *
	 * @param payload Payload in the form of a String
	 * @throws java.io.IOException  If the stream fails while sending data
	 * @throws InterruptedException If we are unable to wait to send data
	 */
	public void write(String payload) throws IOException, InterruptedException {
		/* ADB needs null-terminated strings */
		write((payload + "\0").getBytes("UTF-8"));
	}

	/**
	 * Sends a write packet with a given byte array payload.
	 *
	 * @param payload Payload in the form of a byte array
	 * @throws java.io.IOException  If the stream fails while sending data
	 * @throws InterruptedException If we are unable to wait to send data
	 */
	public void write(byte[] payload) throws IOException, InterruptedException {
		synchronized (this) {
			/* Make sure we're ready for a write */
			long deadline = System.currentTimeMillis() + WRITE_READY_TIMEOUT_MS;
			while (!isClosed && !writeReady.compareAndSet(true, false)) {
				long remaining = deadline - System.currentTimeMillis();
				if (remaining <= 0) {
					throw new IOException("Timed out waiting for the peer to accept data");
				}
				wait(remaining);
			}

			if (isClosed) {
				throw closedException();
			}
		}

		/* Generate a WRITE packet and send it */
		adbConn.channel.writex(AdbProtocol.generateWrite(localId, remoteId, payload));
	}

	private IOException closedException() {
		return new IOException(closeReason != null ? closeReason : "Stream closed");
	}

	/**
	 * Closes the stream. This sends a close message to the peer.
	 *
	 * @throws java.io.IOException If the stream fails while sending the close
	 *                             message.
	 */
	@Override
	public void close() throws IOException {
		synchronized (this) {
			/* This may already be closed by the remote host */
			if (isClosed)
				return;

			/* Notify readers/writers that we've closed */
			notifyClose();
		}

		adbConn.channel.writex(AdbProtocol.generateClose(localId, remoteId));
	}

	/**
	 * Retrieves whether the stream is closed or not
	 *
	 * @return True if the stream is closed, false if not
	 */
	public boolean isClosed() {
		return isClosed;
	}

	/**
	 * Retrieves whether the peer has acknowledged the stream open
	 *
	 * @return True once an OKAY has been received for this stream
	 */
	public boolean isOpen() {
		return isOpen;
	}
}
