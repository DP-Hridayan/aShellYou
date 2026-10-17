package com.cgutman.adblib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class BulkWriterTest {

	private static final int TIMEOUT_MS = 5_000;
	private static final long DEADLINE_MS = 20_000;

	/** Answers each transfer from a script and advances a fake clock by how long it "took". */
	private static final class ScriptedTransfer implements BulkWriter.Transfer {
		final Deque<int[]> script = new ArrayDeque<>();
		long now;
		int written;

		/** @param result bytes moved, or a negative error; @param tookMs how long the call blocked */
		ScriptedTransfer then(int result, long tookMs) {
			script.add(new int[]{result, (int) tookMs});
			return this;
		}

		@Override
		public int transfer(byte[] buffer, int offset, int length, int timeoutMs) {
			int[] step = script.isEmpty() ? new int[]{-1, timeoutMs} : script.poll();
			now += step[1];
			if (step[0] > 0) written += step[0];
			return step[0];
		}
	}

	private BulkWriter writer(ScriptedTransfer transfer) {
		return new BulkWriter(transfer, TIMEOUT_MS, DEADLINE_MS, () -> transfer.now, ms -> transfer.now += ms);
	}

	@Test
	public void partialTransfersAreContinuedUntilEverythingIsWritten() throws IOException {
		ScriptedTransfer transfer = new ScriptedTransfer().then(400, 1).then(600, 1);

		writer(transfer).writeFully(new byte[1000]);

		assertEquals(1000, transfer.written);
	}

	@Test
	public void aShortStallIsRiddenOutWhileTheDeviceIsBusy() throws IOException {
		ScriptedTransfer transfer = new ScriptedTransfer()
				.then(-1, TIMEOUT_MS)
				.then(-1, TIMEOUT_MS)
				.then(1000, 1);

		writer(transfer).writeFully(new byte[1000]);

		assertEquals(1000, transfer.written);
	}

	@Test
	public void aStallPastTheDeadlineFailsAsStalled() {
		ScriptedTransfer transfer = new ScriptedTransfer();

		try {
			writer(transfer).writeFully(new byte[1000]);
			fail("Expected the write to give up");
		} catch (BulkWriter.StalledException expected) {
			assertTrue(transfer.now >= DEADLINE_MS);
			assertTrue(transfer.now < DEADLINE_MS + TIMEOUT_MS + 1);
		} catch (IOException other) {
			fail("Expected a stall, got " + other);
		}
	}

	@Test
	public void repeatedImmediateFailuresAreARealError() {
		ScriptedTransfer transfer = new ScriptedTransfer();
		for (int i = 0; i < 10; i++) transfer.then(-1, 1);

		try {
			writer(transfer).writeFully(new byte[1000]);
			fail("Expected the write to fail");
		} catch (BulkWriter.StalledException stalled) {
			fail("An immediate failure is not a stall");
		} catch (IOException expected) {
			assertTrue(transfer.now < TIMEOUT_MS);
		}
	}
}
