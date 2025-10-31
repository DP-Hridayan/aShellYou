package com.cgutman.adblib;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.HashMap;

/**
 * This class represents an ADB connection.
 * @author Cameron Gutman
 */
public class AdbConnection implements Closeable {

    AdbChannel channel;

	/** The last allocated local stream ID. The ID
	 * chosen for the next stream will be this value + 1.
	 */
	private int lastLocalId;

	/**
	 * The backend thread that handles responding to ADB packets.
	 */
	private Thread connectionThread;
	
	/**
	 * Specifies whether a connect has been attempted
	 */
	private boolean connectAttempted;
	
	/**
	 * Specifies whether a CNXN packet has been received from the peer.
	 */
	private boolean connected;
	
	/**
	 * Specifies the maximum amount data that can be sent to the remote peer.
	 * This is only valid after connect() returns successfully.
	 */
	private int maxData;
	
	/**
	 * An initialized ADB crypto object that contains a key pair.
	 */
	private AdbCrypto crypto;

	/**
	 * Specifies whether this connection has already sent a signed token.
	 */
	private boolean sentSignature;
	
	/** 
	 * A hash map of our open streams indexed by local ID.
	 **/
	private HashMap<Integer, AdbStream> openStreams;
	
	/**
	 * Internal constructor to initialize some internal state
	 */
	private AdbConnection()
	{
		openStreams = new HashMap<Integer, AdbStream>();
		lastLocalId = 0;
		connectionThread = createConnectionThread();
	}
	
	/**
	 * Creates a AdbConnection object associated with the socket and
	 * crypto object specified.
	 * @param channel The channel that the connection will use for communcation.
	 * @param crypto The crypto object that stores the key pair for authentication.
	 * @return A new AdbConnection object.
	 * @throws java.io.IOException If there is a socket error
	 */
	public static AdbConnection create(AdbChannel channel, AdbCrypto crypto) throws IOException
	{
		AdbConnection newConn = new AdbConnection();
		newConn.crypto = crypto;
		newConn.channel = channel;
		return newConn;
	}

	/**
	 * Creates a new connection thread.
	 * @return A new connection thread.
	 */
	private Thread createConnectionThread()
	{
		@SuppressWarnings("resource")
		final AdbConnection conn = this;
		return new Thread(new Runnable() {
			@Override
			public void run() {
				while (!connectionThread.isInterrupted())
				{
					try {
						/* Read and parse a message off the socket's input stream */
						AdbMessage msg = AdbMessage.parseAdbMessage(channel);

						/* Verify magic and checksum */
						if (!AdbProtocol.validateMessage(msg))
							continue;

						switch (msg.getCommand())
						{
						/* Stream-oriented commands */
						case AdbProtocol.CMD_OKAY:
						case AdbProtocol.CMD_WRTE:
						case AdbProtocol.CMD_CLSE:
							/* We must ignore all packets when not connected */
							if (!conn.connected)
								continue;

							/* Get the stream object corresponding to the packet */
							AdbStream waitingStream = openStreams.get(msg.getArg1());
							if (waitingStream == null)
								continue;

							synchronized (waitingStream) {
								if (msg.getCommand() == AdbProtocol.CMD_OKAY)
								{
									/* We're ready for writes */
									waitingStream.updateRemoteId(msg.getArg0());
									waitingStream.readyForWrite();

									/* Unwait an open/write */
									waitingStream.notify();
								}
								else if (msg.getCommand() == AdbProtocol.CMD_WRTE)
								{
									/* Got some data from our partner */
									waitingStream.addPayload(msg.getPayload());

									/* Tell it we're ready for more */
									waitingStream.sendReady();
								}
								else if (msg.getCommand() == AdbProtocol.CMD_CLSE)
								{
									/* He doesn't like us anymore :-( */
									conn.openStreams.remove(msg.getArg1());

									/* Notify readers and writers */
									waitingStream.notifyClose();
								}
							}

							break;

						case AdbProtocol.CMD_AUTH:

							AdbMessage packet;

							if (msg.getArg0() == AdbProtocol.AUTH_TYPE_TOKEN)
							{
								/* This is an authentication challenge */
								if (conn.sentSignature)
								{
									/* We've already tried our signature, so send our public key */
									packet = AdbProtocol.generateAuth(AdbProtocol.AUTH_TYPE_RSA_PUBLIC,
                                            conn.crypto.getAdbPublicKeyPayload());
								}
								else
								{
									/* We'll sign the token */
									packet = AdbProtocol.generateAuth(AdbProtocol.AUTH_TYPE_SIGNATURE,
                                            conn.crypto.signAdbTokenPayload(msg.getPayload()));
									conn.sentSignature = true;
								}

								/* Write the AUTH reply */
								conn.channel.writex(packet);
							}
							break;

						case AdbProtocol.CMD_CNXN:
							synchronized (conn) {
								/* We need to store the max data size */
								conn.maxData = msg.getArg1();

								/* Mark us as connected and unwait anyone waiting on the connection */
								conn.connected = true;
								conn.notifyAll();
							}
							break;

						default:
							/* Unrecognized packet, just drop it */
							break;
						}
					} catch (Exception e) {
						/* The cleanup is taken care of by a combination of this thread
						 * and close() */
                        e.printStackTrace();
                        break;
					}
				}

				/* This thread takes care of cleaning up pending streams */
				synchronized (conn) {
					cleanupStreams();
					conn.notifyAll();
					conn.connectAttempted = false;
				}
			}
		});
	}

	/**
	 * Gets the max data size that the remote client supports.
	 * A connection must have been attempted before calling this routine.
	 * This routine will block if a connection is in progress.
	 * @return The maximum data size indicated in the connect packet.
	 * @throws InterruptedException If a connection cannot be waited on.
	 * @throws java.io.IOException if the connection fails
	 */
	public int getMaxData() throws InterruptedException, IOException
	{
		if (!connectAttempted)
			throw new IllegalStateException("connect() must be called first");

		synchronized (this) {
			/* Block if a connection is pending, but not yet complete */
			if (!connected)
				wait();

			if (!connected) {
				throw new IOException("Connection failed");
			}
		}

		return maxData;
	}

	/**
	 * Connects to the remote device. This routine will block until the connection
	 * completes.
	 * @throws java.io.IOException If the socket fails while connecting
	 * @throws InterruptedException If we are unable to wait for the connection to finish
	 */
	public void connect() throws IOException, InterruptedException
	{
		if (connected)
			throw new IllegalStateException("Already connected");

		/* Write the CONNECT packet */
		channel.writex(AdbProtocol.generateConnect());

		/* Start the connection thread to respond to the peer */
		connectAttempted = true;
		connectionThread.start();

		/* Wait for the connection to go live */
		synchronized (this) {
			if (!connected)
				wait();

			if (!connected) {
				throw new IOException("Connection failed");
			}
		}
	}

	/**
	 * Opens an AdbStream object corresponding to the specified destination.
	 * This routine will block until the connection completes.
	 * @param destination The destination to open on the target
	 * @return AdbStream object corresponding to the specified destination
	 * @throws java.io.UnsupportedEncodingException If the destination cannot be encoded to UTF-8
	 * @throws java.io.IOException If the stream fails while sending the packet
	 * @throws InterruptedException If we are unable to wait for the connection to finish
	 */
	public AdbStream open(String destination) throws UnsupportedEncodingException, IOException, InterruptedException
	{
		int localId = ++lastLocalId;

		if (!connectAttempted)
			throw new IllegalStateException("connect() must be called first");

		/* Wait for the connect response */
		synchronized (this) {
			if (!connected)
				wait();

			if (!connected) {
				throw new IOException("Connection failed");
			}
		}

		/* Add this stream to this list of half-open streams */
		AdbStream stream = new AdbStream(this, localId);
		openStreams.put(localId, stream);

		/* Send the open */
		channel.writex(AdbProtocol.generateOpen(localId, destination));

		/* Wait for the connection thread to receive the OKAY */
		synchronized (stream) {
			stream.wait();
		}

		/* Check if the open was rejected */
		if (stream.isClosed())
			throw new ConnectException("Stream open actively rejected by remote peer");

		/* We're fully setup now */
		return stream;
	}

	/**
	 * This function terminates all I/O on streams associated with this ADB connection
	 */
	private void cleanupStreams() {
		/* Close all streams on this connection */
		for (AdbStream s : openStreams.values()) {
			/* We handle exceptions for each close() call to avoid
			 * terminating cleanup for one failed close(). */
			try {
				s.close();
			} catch (IOException e) {}
		}

		/* No open streams anymore */
		openStreams.clear();
	}

	/** This routine closes the Adb connection and underlying socket
	 * @throws java.io.IOException if the socket fails to close
	 */
	@Override
	public void close() throws IOException {
		/* If the connection thread hasn't spawned yet, there's nothing to do */
		if (connectionThread == null)
			return;
		
		/* Closing the channel will kick the connection thread */
		channel.close();

		/* Wait for the connection thread to die */
		connectionThread.interrupt();
		try {
			connectionThread.join();
		} catch (InterruptedException e) { }
	}
}
