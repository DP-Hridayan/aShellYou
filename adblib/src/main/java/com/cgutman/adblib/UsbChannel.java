package com.cgutman.adblib;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

/**
 * Created by xudong on 2/21/14.
 */
public class UsbChannel implements AdbChannel {

    private static final int WRITE_TIMEOUT_MS = 5000;
    private static final long WRITE_DEADLINE_MS = 10 * 60 * 1000;
    private static final int MAX_EMPTY_READS = 16;
    private static final int MAX_TRANSIENT_FAILURES = 5;
    private static final long TRANSIENT_BACKOFF_MS = 20;

    private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointOut;
    private final UsbEndpoint mEndpointIn;
    private final UsbInterface mInterface;

    private final Object mWriteLock = new Object();

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

    private void writeFully(byte[] buffer) throws IOException {
        int offset = 0;
        int failures = 0;
        long deadline = System.currentTimeMillis() + WRITE_DEADLINE_MS;
        while (offset < buffer.length) {
            long startedAt = System.currentTimeMillis();
            int transferred = mDeviceConnection.bulkTransfer(
                    mEndpointOut, buffer, offset, buffer.length - offset, WRITE_TIMEOUT_MS);
            if (transferred > 0) {
                offset += transferred;
                failures = 0;
                continue;
            }
            /* A peer that is busy stops draining the endpoint, so a transfer that consumed its
             * whole timeout is retried. One that failed immediately is usually a real error, but
             * a long transfer can hit a transient failure, so give it a few attempts first. */
            long elapsed = System.currentTimeMillis() - startedAt;
            if (transferred < 0 && elapsed < WRITE_TIMEOUT_MS / 2) {
                if (++failures > MAX_TRANSIENT_FAILURES) {
                    throw new IOException("USB bulk transfer failed " + failures + " times in a row");
                }
                backOff();
            }
            if (System.currentTimeMillis() >= deadline) {
                throw new IOException("USB bulk transfer timed out");
            }
        }
    }

    private static void backOff() throws IOException {
        try {
            Thread.sleep(TRANSIENT_BACKOFF_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during USB write");
        }
    }

    @Override
    public void writex(AdbMessage message) throws IOException {
        synchronized (mWriteLock) {
            writeFully(message.getMessage());
            if (message.getPayload() != null) {
                writeFully(message.getPayload());
            }
        }
    }

    @Override
    public void close() throws IOException {
        mDeviceConnection.releaseInterface(mInterface);
        mDeviceConnection.close();
    }

    public UsbChannel(UsbDeviceConnection connection, UsbInterface intf) {
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
    }

}
