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

    private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointOut;
    private final UsbEndpoint mEndpointIn;
    private final UsbInterface mInterface;

    private final int defaultTimeout = 1000;

    private final LinkedList<UsbRequest> mInRequestPool = new LinkedList<UsbRequest>();

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

        UsbRequest usbRequest = getInRequest();

        ByteBuffer expected = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        usbRequest.setClientData(expected);

        if (!usbRequest.queue(expected, length)) {
            throw new IOException("fail to queue read UsbRequest");
        }

        while (true) {
            UsbRequest wait = mDeviceConnection.requestWait();

            if (wait == null) {
                throw new IOException("Connection.requestWait return null");
            }

            ByteBuffer clientData = (ByteBuffer) wait.getClientData();
            wait.setClientData(null);

            if (wait.getEndpoint() == mEndpointOut) {
                // a write UsbRequest complete, just ignore
            } else if (expected == clientData) {
                releaseInRequest(wait);
                break;

            } else {
                throw new IOException("unexpected behavior");
            }
        }
        expected.flip();
        expected.get(buffer);
    }

    // API LEVEL 18 is needed to invoke bulkTransfer(mEndpointOut, buffer, offset, buffer.length - offset, defaultTimeout)
//    @Override
//    public void writex(byte[] buffer) throws IOException{
//
//        int offset = 0;
//        int transferred = 0;
//
//        while ((transferred = mDeviceConnection.bulkTransfer(mEndpointOut, buffer, offset, buffer.length - offset, defaultTimeout)) >= 0) {
//            offset += transferred;
//            if (offset >= buffer.length) {
//                break;
//            }
//        }
//        if (transferred < 0) {
//            throw new IOException("bulk transfer fail");
//        }
//    }

    // A dirty solution, only API level 12 is needed, not 18
    private void writex(byte[] buffer) throws IOException{

        int offset = 0;
        int transferred = 0;

        byte[] tmp = new byte[buffer.length];
        System.arraycopy(buffer, 0, tmp, 0, buffer.length);

        while ((transferred = mDeviceConnection.bulkTransfer(mEndpointOut, tmp, buffer.length - offset, defaultTimeout)) >= 0) {
            offset += transferred;
            if (offset >= buffer.length) {
                break;
            } else {
                System.arraycopy(buffer, offset, tmp, 0, buffer.length - offset);
            }
        }
        if (transferred < 0) {
            throw new IOException("bulk transfer fail");
        }
    }

    @Override
    public void writex(AdbMessage message) throws IOException {
        // TODO: here is the weirdest thing
        // write (message.head + message.payload) is totally different with write(message.head) + write(head.payload)
        writex(message.getMessage());
        if (message.getPayload() != null) {
            writex(message.getPayload());
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
