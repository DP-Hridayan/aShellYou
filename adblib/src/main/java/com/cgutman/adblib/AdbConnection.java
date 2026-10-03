package com.cgutman.adblib;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * This class represents an ADB connection.
 * @author Cameron Gutman
 */
public class AdbConnection implements Closeable {

	private static final String TAG = "AdbConnection";

	/** How long an open waits for the device to accept or refuse the stream. */
	private static final long DEFAULT_OPEN_TIMEOUT_MS = 30_000;

	/**
	 * Receives a callback once the connection's reader thread has terminated,
	 * either because {@link #close()} was called or because the channel failed.
	 */
	public interface ConnectionListener {
		void onConnectionClosed(AdbConnection connection);
	}

	AdbChannel channel;

	/** The last allocated local stream ID. The ID
	 * chosen for the next stream will be this value + 1.
	 */
	private int lastLocalId;

	/**
	 * The backend thread that handles responding to ADB packets.
	 */
	private final Thread connectionThread;

	/**
	 * Specifies whether a connect has been attempted
	 */
	private volatile boolean connectAttempted;

	/**
	 * Specifies whether a CNXN packet has been received from the peer.
	 */
	private volatile boolean connected;

	/**
	 * Specifies whether the reader thread has terminated.
	 */
	private volatile boolean finished;

	/**
	 * Specifies the maximum amount data that can be sent to the remote peer.
	 * This is only valid after connect() returns successfully.
	 */
	private volatile int maxData;

	/**
	 * An initialized ADB crypto object that contains a key pair.
	 */
	private AdbCrypto crypto;

	/**
	 * Specifies whether this connection has already sent a signed token.
	 */
	private boolean sentSignature;

	/**
	 * A map of our open streams indexed by local ID.
	 **/
	private final Map<Integer, AdbStream> openStreams;

	private volatile ConnectionListener listener;

	/**
	 * The banner the peer sent with its connect packet, such as "device::ro.product.name=...".
	 * The text before the first "::" is the mode adbd is running in.
	 */
	private volatile String banner = "";

	/** Why the reader thread stopped, surfaced to streams so a failure is diagnosable. */
	private volatile String terminationReason;

	/**
	 * Sends the packets the reader thread owes the peer, such as the OKAY for each WRTE.
	 *
	 * The reader must never wait on an outgoing transfer. A device can stop draining our writes
	 * until its own writes are read; if the reader is the thread waiting to write, nothing reads
	 * them, both directions stall for good, and every stream on the connection hangs.
	 */
	private final ExecutorService readerReplies =
			Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "adblib-replies"));

	private volatile long openTimeoutMillis = DEFAULT_OPEN_TIMEOUT_MS;

	/**
	 * Internal constructor to initialize some internal state
	 */
	private AdbConnection()
	{
		openStreams = new ConcurrentHashMap<Integer, AdbStream>();
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
	 * Registers a listener that is invoked once the connection has terminated.
	 * @param listener The listener, or null to clear it.
	 */
	public void setConnectionListener(ConnectionListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Gets the banner the peer sent with its connect packet. Empty until the handshake completes.
	 * @return The banner text, without its trailing NUL.
	 */
	public String getBanner()
	{
		return banner;
	}

	/**
	 * Reports whether the connection handshake completed and the reader thread is still alive.
	 * @return True while the connection can be used to open streams.
	 */
	public boolean isConnected()
	{
		return connected && !finished;
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
				readLoop();
				terminate();
				ConnectionListener current = listener;
				if (current != null) {
					current.onConnectionClosed(conn);
				}
			}
		}, "adblib-reader");
	}

	private void readLoop()
	{
		while (!connectionThread.isInterrupted())
		{
			try {
				AdbMessage msg = AdbMessage.parseAdbMessage(channel);
				if (!AdbProtocol.validateMessage(msg)) {
					/* A bad checksum means the stream is out of step with the peer; reading on
					 * would interpret payload bytes as headers. */
					terminationReason = "Received a corrupt ADB packet";
					Log.w(TAG, terminationReason);
					break;
				}
				handleMessage(msg);
			} catch (Exception e) {
				terminationReason = describeFailure(e);
				Log.w(TAG, "ADB reader stopped: " + terminationReason, e);
				break;
			}
		}
	}

	private static String describeFailure(Exception e)
	{
		String message = e.getMessage();
		return (message == null || message.isEmpty()) ? e.getClass().getSimpleName() : message;
	}

	/**
	 * Gets why the connection terminated, or null while it is healthy.
	 * @return The failure description reported to streams when the connection died.
	 */
	public String getTerminationReason()
	{
		return terminationReason;
	}

	private void terminate()
	{
		synchronized (this) {
			finished = true;
			connected = false;
			cleanupStreams();
			notifyAll();
		}
	}

	private void handleMessage(AdbMessage msg) throws IOException, GeneralSecurityException
	{
		switch (msg.getCommand())
		{
		case AdbProtocol.CMD_OKAY:
		case AdbProtocol.CMD_WRTE:
		case AdbProtocol.CMD_CLSE:
			handleStreamMessage(msg);
			break;

		case AdbProtocol.CMD_AUTH:
			handleAuth(msg);
			break;

		case AdbProtocol.CMD_CNXN:
			handleConnect(msg);
			break;

		default:
			break;
		}
	}

	private void handleStreamMessage(AdbMessage msg) throws IOException
	{
		/* We must ignore all packets when not connected */
		if (!connected)
			return;

		AdbStream waitingStream = openStreams.get(msg.getArg1());
		if (waitingStream == null)
			return;

		synchronized (waitingStream) {
			if (msg.getCommand() == AdbProtocol.CMD_OKAY && waitingStream.isAbandoned())
			{
				/* Nobody will use this stream, but the device just opened its end. */
				openStreams.remove(msg.getArg1());
				sendFromReader(AdbProtocol.generateClose(msg.getArg1(), msg.getArg0()));
			}
			else if (msg.getCommand() == AdbProtocol.CMD_OKAY)
			{
				waitingStream.updateRemoteId(msg.getArg0());
				waitingStream.readyForWrite();
			}
			else if (msg.getCommand() == AdbProtocol.CMD_WRTE)
			{
				waitingStream.addPayload(msg.getPayload());
				waitingStream.sendReady();
			}
			else
			{
				openStreams.remove(msg.getArg1());
				waitingStream.notifyClose();
			}
		}
	}

	private void handleAuth(AdbMessage msg) throws IOException, GeneralSecurityException
	{
		if (msg.getArg0() != AdbProtocol.AUTH_TYPE_TOKEN)
			return;

		AdbMessage packet;
		if (sentSignature)
		{
			packet = AdbProtocol.generateAuth(AdbProtocol.AUTH_TYPE_RSA_PUBLIC,
					crypto.getAdbPublicKeyPayload());
		}
		else
		{
			packet = AdbProtocol.generateAuth(AdbProtocol.AUTH_TYPE_SIGNATURE,
					crypto.signAdbTokenPayload(msg.getPayload()));
			sentSignature = true;
		}
		channel.writex(packet);
	}

	private void handleConnect(AdbMessage msg)
	{
		synchronized (this) {
			maxData = msg.getArg1();
			banner = parseBanner(msg.getPayload());
			connected = true;
			notifyAll();
		}
	}

	private static String parseBanner(byte[] payload)
	{
		if (payload == null)
			return "";

		int end = payload.length;
		for (int i = 0; i < payload.length; i++) {
			if (payload[i] == 0) {
				end = i;
				break;
			}
		}
		return new String(payload, 0, end, StandardCharsets.UTF_8);
	}

	private void waitForConnection() throws InterruptedException, IOException
	{
		synchronized (this) {
			while (!connected && !finished)
				wait();

			if (!connected) {
				throw new IOException("Connection failed");
			}
		}
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

		waitForConnection();
		return maxData;
	}

	/**
	 * Gets the largest payload both peers accept: the smaller of our advertised maximum and the
	 * one the device reported in its connect packet. A write larger than this is rejected by the
	 * peer and tears down the connection, so every bulk sender must chunk by this value.
	 * @return The negotiated maximum payload in bytes.
	 * @throws InterruptedException If a connection cannot be waited on.
	 * @throws java.io.IOException if the connection fails
	 */
	public int getNegotiatedMaxPayload() throws InterruptedException, IOException
	{
		return Math.min(getMaxData(), AdbProtocol.CONNECT_MAXDATA);
	}

	/**
	 * Connects to the remote device. This routine will block until the connection
	 * completes.
	 * @throws java.io.IOException If the socket fails while connecting
	 * @throws InterruptedException If we are unable to wait for the connection to finish
	 */
	public void connect() throws IOException, InterruptedException
	{
		if (connectAttempted)
			throw new IllegalStateException("connect() already called");

		connectAttempted = true;
		channel.writex(AdbProtocol.generateConnect());
		connectionThread.start();
		waitForConnection();
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
		if (!connectAttempted)
			throw new IllegalStateException("connect() must be called first");

		waitForConnection();

		int localId = nextLocalId();
		AdbStream stream = new AdbStream(this, localId);
		openStreams.put(localId, stream);

		try {
			channel.writex(AdbProtocol.generateOpen(localId, destination));
			awaitOpenAnswer(stream);
		} catch (IOException e) {
			openStreams.remove(localId);
			throw e;
		} catch (InterruptedException e) {
			abandon(stream);
			throw e;
		}

		if (stream.isClosed()) {
			openStreams.remove(localId);
			throw new ConnectException("Stream open actively rejected by remote peer");
		}

		if (!stream.isOpen()) {
			abandon(stream);
			throw new IOException("Timed out waiting for the device to open " + destination);
		}

		return stream;
	}

	/** Overrides how long {@link #open} waits; for tests. */
	void setOpenTimeoutMillis(long timeoutMillis)
	{
		openTimeoutMillis = timeoutMillis;
	}

	/**
	 * Queues a packet on behalf of the reader thread without waiting for it to be sent.
	 * Packets are dropped once the connection is closing, when nobody can receive them anyway.
	 */
	void sendFromReader(final AdbMessage message)
	{
		try {
			readerReplies.execute(() -> {
				try {
					channel.writex(message);
				} catch (IOException e) {
					Log.w(TAG, "Could not send a reply packet", e);
				}
			});
		} catch (RejectedExecutionException ignored) {
		}
	}

	private void awaitOpenAnswer(AdbStream stream) throws InterruptedException
	{
		long deadline = System.currentTimeMillis() + openTimeoutMillis;
		synchronized (stream) {
			while (!stream.isOpen() && !stream.isClosed()) {
				long remaining = deadline - System.currentTimeMillis();
				if (remaining <= 0)
					return;
				stream.wait(remaining);
			}
		}
	}

	/**
	 * Gives up on a stream whose open was not answered in time. The OPEN is already on its way, so
	 * the device may still accept it; the stream stays registered so that a late OKAY can be
	 * answered with a CLSE instead of leaving an orphaned stream on the device.
	 */
	private void abandon(AdbStream stream) throws IOException
	{
		boolean openedMeanwhile;
		synchronized (stream) {
			openedMeanwhile = stream.isOpen();
			if (!openedMeanwhile)
				stream.markAbandoned();
		}
		if (openedMeanwhile)
			stream.close();
	}

	private synchronized int nextLocalId()
	{
		return ++lastLocalId;
	}

	/**
	 * This function terminates all I/O on streams associated with this ADB connection
	 */
	private void cleanupStreams() {
		/* The channel is already gone, so wake the streams rather than trying to send a close
		 * packet that would block until the write deadline expires. */
		for (AdbStream s : openStreams.values()) {
			s.notifyClose(terminationReason);
		}
		openStreams.clear();
	}

	/** This routine closes the Adb connection and underlying socket
	 * @throws java.io.IOException if the socket fails to close
	 */
	@Override
	public void close() throws IOException {
		connected = false;
		readerReplies.shutdownNow();

		try {
			channel.close();
		} catch (IOException ignored) {
		}

		connectionThread.interrupt();

		if (Thread.currentThread() == connectionThread || !connectionThread.isAlive())
			return;

		try {
			connectionThread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
