package com.cgutman.adblib;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xudong on 2/21/14.
 */
public class UsbChannel implements AdbChannel {

    private static final int WRITE_TIMEOUT_MS = 5000;
    private static final int MAX_EMPTY_READS = 16;
    private static final String TAG = "AdbUsbChannel";

    /**
     * How long a write may stall before the link is declared dead, for a peer that legitimately
     * stops draining USB for minutes, such as recovery verifying a sideloaded package.
     */
    public static final long SLOW_PEER_STALL_DEADLINE_MS = 10 * 60 * 1000;

    private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointOut;
    private final UsbEndpoint mEndpointIn;
    private final UsbInterface mInterface;

    /**
     * There is one OUT endpoint, and a packet's header and payload must reach it back to back, so
     * writes are serialised. The lock is fair: writers are served in arrival order, so a stream
     * sending a lot of data cannot keep acknowledgements for the other streams waiting.
     */
    private final ReentrantLock mWriteLock = new ReentrantLock(true);

    private final BulkWriter mWriter;

    private final LinkedList<UsbRequest> mInRequestPool = new LinkedList<>();

    // return an IN request to the pool
    public void releaseInRequest(UsbRequest request) {
        synchronized (mInRequestPool) {
            mInRequestPool.add(request);
        }
    }

    // get an IN request from the pool
    public UsbRequest getInRequest() {
        synchronized (mInRequestPool) {
            if (mInRequestPool.isEmpty()) {
                UsbRequest request = new UsbRequest();
                request.initialize(mDeviceConnection, mEndpointIn);
                return request;
            } else {
                return mInRequestPool.removeFirst();
            }
        }
    }

    @Override
    public void readx(byte[] buffer, int length) throws IOException {
        ByteBuffer expected = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);

        int emptyReads = 0;
        while (expected.position() < length) {
            if (readChunk(expected, length)) {
                emptyReads = 0;
            } else if (++emptyReads > MAX_EMPTY_READS) {
                throw new IOException("USB read returned no data");
            }
        }

        expected.flip();
        expected.get(buffer, 0, length);
    }

    /** Returns false for a zero length packet, which USB uses as a transfer delimiter. */
    private boolean readChunk(ByteBuffer expected, int length) throws IOException {
        int before = expected.position();
        expected.limit(length);

        UsbRequest usbRequest = getInRequest();
        usbRequest.setClientData(expected);

        if (!usbRequest.queue(expected)) {
            releaseInRequest(usbRequest);
            throw new IOException("fail to queue read UsbRequest");
        }

        waitForRequest(usbRequest);

        return expected.position() > before;
    }

    private void waitForRequest(UsbRequest usbRequest) throws IOException {
        while (true) {
            UsbRequest completed = mDeviceConnection.requestWait();

            if (completed == null) {
                throw new IOException("Connection.requestWait returned null");
            }

            completed.setClientData(null);
            releaseInRequest(completed);

            if (completed == usbRequest) {
                return;
            }
        }
    }

    @Override
    public void writex(AdbMessage message) throws IOException {
        mWriteLock.lock();
        try {
            mWriter.writeFully(message.getMessage());
            if (message.getPayload() != null) {
                mWriter.writeFully(message.getPayload());
            }
        } catch (BulkWriter.StalledException e) {
            /* Every packet queued behind this one would wait as long, so the link is dead. Closing
             * it fails the pending read, which ends the connection and tells its owner, instead of
             * leaving every stream frozen until the next write gives up too. */
            Log.w(TAG, "Closing a USB link that stopped accepting data", e);
            mDeviceConnection.close();
            throw e;
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        mDeviceConnection.releaseInterface(mInterface);
        mDeviceConnection.close();
    }

    public UsbChannel(UsbDeviceConnection connection, UsbInterface intf) {
        this(connection, intf, SLOW_PEER_STALL_DEADLINE_MS);
    }

    /**
     * @param stallDeadlineMs how long a write may go unfinished before the link is closed as dead.
     */
    public UsbChannel(UsbDeviceConnection connection, UsbInterface intf, long stallDeadlineMs) {
        mDeviceConnection = connection;
        mInterface = intf;

        UsbEndpoint epOut = null;
        UsbEndpoint epIn = null;
        // look for our bulk endpoints
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint ep = intf.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epOut = ep;
                } else {
                    epIn = ep;
                }
            }
        }
        if (epOut == null || epIn == null) {
            throw new IllegalArgumentException("not all endpoints found");
        }
        mEndpointOut = epOut;
        mEndpointIn = epIn;
        mWriter = new BulkWriter(
                (buffer, offset, length, timeoutMs) ->
                        mDeviceConnection.bulkTransfer(mEndpointOut, buffer, offset, length, timeoutMs),
                WRITE_TIMEOUT_MS,
                stallDeadlineMs,
                System::currentTimeMillis,
                Thread::sleep);
    }

}
