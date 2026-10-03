package com.cgutman.adblib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AdbConnectionFlowTest {

	private static final int REMOTE_A = 101;
	private static final int REMOTE_B = 102;
	private static final long STEP_TIMEOUT_MS = 2_000;

	private ScriptedChannel channel;
	private AdbConnection connection;
	private ExecutorService threads;

	@Before
	public void connect() throws Exception {
		threads = Executors.newCachedThreadPool();
		channel = new ScriptedChannel();
		connection = AdbConnection.create(channel, null);
		channel.deliver(AdbProtocol.generateConnect());
		connection.connect();
	}

	@After
	public void tearDown() throws Exception {
		channel.releaseWrites();
		connection.close();
		threads.shutdownNow();
	}

	/*
	 * The reader thread must keep draining incoming packets while an outgoing write is stuck. If
	 * it waits on the write to acknowledge a packet, a device that only drains our writes once its
	 * own writes are read never makes progress again, and every stream on the connection hangs.
	 */
	@Test
	public void incomingDataKeepsFlowingWhileAnAcknowledgementIsStuck() throws Exception {
		AdbStream first = open("shell:first", REMOTE_A);
		AdbStream second = open("shell:second", REMOTE_B);

		channel.blockWrites();
		channel.deliver(AdbProtocol.generateWrite(REMOTE_A, localIdOf(1), new byte[]{1}));
		channel.deliver(AdbProtocol.generateWrite(REMOTE_B, localIdOf(2), new byte[]{2}));

		assertArrayEquals(new byte[]{1}, readWithin(first));
		assertArrayEquals(new byte[]{2}, readWithin(second));
	}

	@Test
	public void anInterruptedOpenClosesTheStreamOnceTheDeviceAcceptsIt() throws Exception {
		CountDownLatch openSent = channel.awaitCommand(AdbProtocol.CMD_OPEN);
		Future<?> opening = threads.submit(() -> {
			connection.open("localabstract:late");
			return null;
		});
		assertTrue(openSent.await(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS));

		opening.cancel(true);
		CountDownLatch closeSent = channel.awaitCommand(AdbProtocol.CMD_CLSE);
		channel.deliver(AdbProtocol.generateReady(REMOTE_A, localIdOf(1)));

		assertTrue("Expected a CLSE for the abandoned stream", closeSent.await(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS));
	}

	@Test
	public void anOpenTheDeviceNeverAnswersTimesOut() throws Exception {
		connection.setOpenTimeoutMillis(200);

		try {
			connection.open("localabstract:silent");
			fail("Expected the open to time out");
		} catch (IOException expected) {
			assertTrue(expected.getMessage().contains("localabstract:silent"));
		}
	}

	private AdbStream open(String destination, int remoteId) throws Exception {
		int localId = channel.openedStreams() + 1;
		CountDownLatch openSent = channel.awaitCommand(AdbProtocol.CMD_OPEN);
		Future<AdbStream> opening = threads.submit(() -> connection.open(destination));
		assertTrue(openSent.await(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS));
		channel.deliver(AdbProtocol.generateReady(remoteId, localId));
		return opening.get(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
	}

	private static int localIdOf(int nth) {
		return nth;
	}

	private byte[] readWithin(AdbStream stream) throws Exception {
		Future<byte[]> reading = threads.submit(stream::read);
		try {
			return reading.get(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			fail("The reader stopped delivering data while a write was blocked");
			return null;
		}
	}

	/**
	 * Plays the device: packets queued with {@link #deliver} are what the connection reads, and
	 * writes can be held, like an endpoint the device has stopped draining.
	 */
	private static final class ScriptedChannel implements AdbChannel {

		private final BlockingQueue<byte[]> incoming = new LinkedBlockingQueue<>();
		private final List<AdbMessage> written = new CopyOnWriteArrayList<>();
		private final List<CommandWaiter> waiters = new CopyOnWriteArrayList<>();
		private volatile CountDownLatch writeGate;
		private ByteBuffer current = ByteBuffer.allocate(0);

		void deliver(AdbMessage message) {
			byte[] header = message.getMessage();
			byte[] payload = message.getPayload();
			int payloadLength = payload == null ? 0 : payload.length;
			ByteBuffer packet = ByteBuffer.allocate(header.length + payloadLength);
			packet.put(header);
			if (payload != null) {
				packet.put(payload);
			}
			incoming.add(packet.array());
		}

		void blockWrites() {
			writeGate = new CountDownLatch(1);
		}

		void releaseWrites() {
			CountDownLatch gate = writeGate;
			if (gate != null) {
				gate.countDown();
			}
		}

		int openedStreams() {
			int count = 0;
			for (AdbMessage message : written) {
				if (message.getCommand() == AdbProtocol.CMD_OPEN) {
					count++;
				}
			}
			return count;
		}

		CountDownLatch awaitCommand(int command) {
			CommandWaiter waiter = new CommandWaiter(command);
			waiters.add(waiter);
			return waiter.latch;
		}

		@Override
		public void readx(byte[] buffer, int length) throws IOException {
			int filled = 0;
			while (filled < length) {
				if (!current.hasRemaining()) {
					try {
						current = ByteBuffer.wrap(incoming.take());
					} catch (InterruptedException e) {
						throw new IOException("Channel closed");
					}
				}
				int chunk = Math.min(length - filled, current.remaining());
				current.get(buffer, filled, chunk);
				filled += chunk;
			}
		}

		@Override
		public void writex(AdbMessage message) throws IOException {
			CountDownLatch gate = writeGate;
			if (gate != null) {
				try {
					gate.await();
				} catch (InterruptedException e) {
					throw new IOException("Write interrupted");
				}
			}
			written.add(message);
			for (CommandWaiter waiter : waiters) {
				if (waiter.command == message.getCommand()) {
					waiter.latch.countDown();
				}
			}
		}

		@Override
		public void close() {
		}

		private static final class CommandWaiter {
			final int command;
			final CountDownLatch latch = new CountDownLatch(1);

			CommandWaiter(int command) {
				this.command = command;
			}
		}
	}
}
