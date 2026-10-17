// SPDX-License-Identifier: BSD-3-Clause AND (GPL-3.0-or-later OR Apache-2.0)

package io.github.muntashirakon.adb;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class abstracts the underlying ADB streams
 */
// Copyright 2013 Cameron Gutman
public class AdbStream implements Closeable {

    /**
     * The AdbConnection object that the stream communicates over
     */
    private final AdbConnection mAdbConnection;

    /**
     * The local ID of the stream
     */
    private final int mLocalId;

    /**
     * The remote ID of the stream
     */
    private volatile int mRemoteId;

    /**
     * Indicates whether WRTE is currently allowed
     */
    private final AtomicBoolean mWriteReady;

    /**
     * A queue of data from the target's WRTE packets
     */
    private final Queue<byte[]> mReadQueue;

    /**
     * Store data received from the first WRTE packet in order to support buffering.
     */
    private final ByteBuffer mReadBuffer;

    /**
     * Indicates whether the connection is closed already
     */
    private volatile boolean mIsClosed;

    /**
     * Whether the remote peer has closed but we still have unread data in the queue
     */
    private volatile boolean mPendingClose;

    /**
     * Whether the peer has acknowledged the OPEN. Unlike {@link #mWriteReady}, writes never reset it.
     */
    private volatile boolean mIsOpen;

    /**
     * The opener gave up before the peer answered, so a late OKAY must be met with a CLSE.
     */
    private volatile boolean mAbandoned;

    /**
     * Creates a new AdbStream object on the specified AdbConnection
     * with the given local ID.
     *
     * @param adbConnection AdbConnection that this stream is running on
     * @param localId       Local ID of the stream
     */
    AdbStream(AdbConnection adbConnection, int localId)
            throws IOException, InterruptedException, AdbPairingRequiredException {
        this.mAdbConnection = adbConnection;
        this.mLocalId = localId;
        this.mReadQueue = new ConcurrentLinkedQueue<>();
        this.mReadBuffer = (ByteBuffer) ByteBuffer.allocate(adbConnection.getMaxData()).flip();
        this.mWriteReady = new AtomicBoolean(false);
        this.mIsClosed = false;
    }

    public AdbInputStream openInputStream() {
        return new AdbInputStream(this);
    }

    public AdbOutputStream openOutputStream() {
        return new AdbOutputStream(this);
    }

    /**
     * Called by the connection thread to indicate newly received data.
     *
     * @param payload Data inside the WRTE message
     */
    void addPayload(byte[] payload) {
        synchronized (mReadQueue) {
            mReadQueue.add(payload);
            mReadQueue.notifyAll();
        }
    }

    /**
     * Called by the connection thread to send an OKAY packet, allowing the
     * other side to continue transmission.
     *
     * @throws IOException If the connection fails while sending the packet
     */
    void sendReady() {
        // Queue an OKAY packet; the reader thread must not block on the connection's write lock
        mAdbConnection.sendFromReader(AdbProtocol.generateReady(mLocalId, mRemoteId));
    }

    /**
     * Called by the connection thread to update the remote ID for this stream
     *
     * @param remoteId New remote ID
     */
    void updateRemoteId(int remoteId) {
        this.mRemoteId = remoteId;
    }

    /**
     * Called by the connection thread to indicate the stream is okay to send data.
     */
    void readyForWrite() {
        mWriteReady.set(true);
        mIsOpen = true;
    }

    boolean isOpen() {
        return mIsOpen;
    }

    void markAbandoned() {
        mAbandoned = true;
    }

    boolean isAbandoned() {
        return mAbandoned;
    }

    /**
     * Called by the connection thread to notify that the stream was closed by the peer.
     */
    void notifyClose(boolean closedByPeer) {
        // We don't call close() because it sends another CLSE
        if (closedByPeer && !mReadQueue.isEmpty()) {
            // The remote peer closed the stream, but we haven't finished reading the remaining data
            mPendingClose = true;
        } else {
            mIsClosed = true;
        }

        // Notify readers and writers
        synchronized (this) {
            notifyAll();
        }
        synchronized (mReadQueue) {
            mReadQueue.notifyAll();
        }
    }

    /**
     * Read bytes from the ADB daemon.
     *
     * @return the next byte of data, or {@code -1} if the end of the stream is reached.
     * @throws IOException If the stream fails while waiting
     */
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (mReadBuffer.hasRemaining()) {
            return readBuffer(bytes, offset, length);
        }
        // Buffer has no data, grab from the queue
        synchronized (mReadQueue) {
            byte[] data;
            // Wait for the connection to close or data to be received. A peer close that arrived
            // behind queued data ends the wait too once the queue drains, since nothing else will
            // notify this reader again.
            while ((data = mReadQueue.poll()) == null && !mIsClosed && !mPendingClose) {
                try {
                    mReadQueue.wait();
                } catch (InterruptedException e) {
                    //noinspection UnnecessaryInitCause
                    throw (IOException) new IOException().initCause(e);
                }
            }
            // Add data to the buffer
            if (data != null) {
                mReadBuffer.clear();
                mReadBuffer.put(data);
                mReadBuffer.flip();
                if (mReadBuffer.hasRemaining()) {
                    return readBuffer(bytes, offset, length);
                }
            }

            if (mIsClosed) {
                throw new IOException("Stream closed.");
            }

            if (mPendingClose && mReadQueue.isEmpty()) {
                // The peer closed the stream, and we've finished reading the stream data, so this stream is finished
                mIsClosed = true;
            }
        }

        return -1;
    }

    private int readBuffer(byte[] bytes, int offset, int length) {
        int count = Math.min(length, mReadBuffer.remaining());
        mReadBuffer.get(bytes, offset, count);
        return count;
    }

    /**
     * Sends a WRTE packet with a given byte array payload. It does not flush the stream.
     *
     * @param bytes Payload in the form of a byte array
     * @throws IOException If the stream fails while sending data
     */
    public void write(byte[] bytes, int offset, int length) throws IOException {
        int maxData;
        try {
            maxData = mAdbConnection.getMaxData();
        } catch (InterruptedException | AdbPairingRequiredException e) {
            //noinspection UnnecessaryInitCause
            throw (IOException) new IOException().initCause(e);
        }
        // A WRITE may only follow a READY, so every chunk waits for the acknowledgement of the one
        // before it. Sending them back to back makes adbd close the connection.
        while (length != 0) {
            int chunk = Math.min(length, maxData);
            awaitWriteReady();
            mAdbConnection.sendPacket(AdbProtocol.generateWrite(mLocalId, mRemoteId, bytes, offset, chunk));
            offset = offset + chunk;
            length = length - chunk;
        }
    }

    private void awaitWriteReady() throws IOException {
        synchronized (this) {
            while (!mIsClosed && !mWriteReady.compareAndSet(true, false)) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    //noinspection UnnecessaryInitCause
                    throw (IOException) new IOException().initCause(e);
                }
            }

            if (mIsClosed) {
                throw new IOException("Stream closed");
            }
        }
    }

    public void flush() throws IOException {
        if (mIsClosed) {
            throw new IOException("Stream closed");
        }
        mAdbConnection.flushPacket();
    }

    /**
     * Closes the stream. This sends a close message to the peer.
     *
     * @throws IOException If the stream fails while sending the close message.
     */
    @Override
    public void close() throws IOException {
        synchronized (this) {
            // This may already be closed by the remote host
            if (mIsClosed)
                return;

            // Notify readers/writers that we've closed
            notifyClose(false);
        }

        mAdbConnection.sendPacket(AdbProtocol.generateClose(mLocalId, mRemoteId));
    }

    /**
     * Returns whether the stream is closed or not
     *
     * @return True if the stream is close, false if not
     */
    public boolean isClosed() {
        return mIsClosed;
    }

    /**
     * Returns an estimate of available data.
     *
     * @return an estimate of the number of bytes that can be read from this stream without blocking.
     * @throws IOException if the stream is close.
     */
    public int available() throws IOException {
        synchronized (this) {
            if (mIsClosed) {
                throw new IOException("Stream closed.");
            }
            if (mReadBuffer.hasRemaining()) {
                return mReadBuffer.remaining();
            }
            byte[] data = mReadQueue.peek();
            return data == null ? 0 : data.length;
        }
    }
}
