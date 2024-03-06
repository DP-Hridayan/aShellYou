package com.cgutman.adblib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class provides an abstraction for the ADB message format.
 * @author Cameron Gutman
 */
public class AdbMessage {
    private ByteBuffer mMessageBuffer;

    private byte[] payload;

    private AdbMessage() {}

    // sets the fields in the command header
    public AdbMessage (int command, int arg0, int arg1, byte[] data) {
        mMessageBuffer = ByteBuffer.allocate(AdbProtocol.ADB_HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        mMessageBuffer.putInt(0, command);
        mMessageBuffer.putInt(4, arg0);
        mMessageBuffer.putInt(8, arg1);
        mMessageBuffer.putInt(12, (data == null ? 0 : data.length));
        mMessageBuffer.putInt(16, (data == null ? 0 : checksum(data)));
        mMessageBuffer.putInt(20, command ^ 0xFFFFFFFF);

        payload = data;

    }

    public AdbMessage(int command, int arg0, int arg1) {
        this(command, arg0, arg1, (byte[]) null);
    }

    /**
     * Read and parse an ADB message from the supplied input stream.
     * This message is NOT validated.
     * @param in InputStream object to read data from
     * @return An AdbMessage object represented the message read
     * @throws java.io.IOException If the stream fails while reading
     */
    public static AdbMessage parseAdbMessage(AdbChannel in) throws IOException {
        AdbMessage msg = new AdbMessage();
        ByteBuffer packet = ByteBuffer.allocate(AdbProtocol.ADB_HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN);

		/* Read the header first */
        in.readx(packet.array(), AdbProtocol.ADB_HEADER_LENGTH);

        msg.mMessageBuffer = packet;

		/* If there's a payload supplied, read that too */
        if (msg.getPayloadLength() != 0) {
            msg.setPayload(new byte[msg.getPayloadLength()]);
            in.readx(msg.getPayload(), msg.getPayloadLength());
        }

        return msg;
    }

    /**
     * This function performs a checksum on the ADB payload data.
     * @param payload Payload to checksum
     * @return The checksum of the payload
     */
    public static int checksum(byte[] payload)
    {
        int checksum = 0;

        for (byte b : payload)
        {
			/* We have to manually "unsign" these bytes because Java sucks */
            if (b >= 0)
                checksum += b;
            else
                checksum += b+256;
        }

        return checksum;
    }


    /** The command field of the message */
    public int getCommand() {
        return mMessageBuffer.getInt(0);
    }

    /** The arg0 field of the message */
    public int getArg0() {
        return mMessageBuffer.getInt(4);
    }

    /** The arg1 field of the message */
    public int getArg1() {
        return mMessageBuffer.getInt(8);
    }

    /** The payload length field of the message */
    public int getPayloadLength() {
        return mMessageBuffer.getInt(12);
    }

    /** The checksum field of the message */
    public int getChecksum() {
        return mMessageBuffer.getInt(16);
    }

    /** The magic field of the message */
    public int getMagic() {
        return mMessageBuffer.getInt(20);
    }

    public byte[] getMessage() {
        return mMessageBuffer.array();
    }

    /** The payload of the message */
    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}