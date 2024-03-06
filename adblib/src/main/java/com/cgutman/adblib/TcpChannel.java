package com.cgutman.adblib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by xudong on 2/21/14.
 */
public class TcpChannel implements AdbChannel {

    /** The underlying socket that this class uses to communicate with the target device. */
    private Socket socket;

    /**
     * The input stream that this class uses to read from the socket.
     */
    private InputStream inputStream;

    /**
     * The output stream that this class uses to read from the socket.
     */
    private OutputStream outputStream;


    @Override
    public void readx(byte[] buffer, int length) throws IOException {

        int dataRead = 0;
        do {
            int bytesRead = inputStream.read(buffer, dataRead, length - dataRead);

            if (bytesRead < 0)
                throw new IOException("Stream closed");
            else
                dataRead += bytesRead;
        }
        while (dataRead < length);
    }

    private void writex(byte[] buffer) throws IOException {
        outputStream.write(buffer);
        outputStream.flush();
    }

    @Override
    public void writex(AdbMessage message) throws IOException {
        writex(message.getMessage());
        if (message.getPayload() != null) {
            writex(message.getPayload());
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public TcpChannel(Socket socket) {
        try {
            /* Disable Nagle because we're sending tiny packets */
            socket.setTcpNoDelay(true);

            this.socket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
