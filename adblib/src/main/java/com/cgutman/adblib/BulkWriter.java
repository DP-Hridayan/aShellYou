package com.cgutman.adblib;

import android.util.Log;

import java.io.IOException;

/**
 * Writes a whole buffer through a bulk endpoint, riding out a peer that is briefly too busy to
 * drain it, and giving up once it has stalled for longer than a deadline.
 */
final class BulkWriter {

	/** One bulk transfer: returns the bytes moved, or a negative value when nothing moved. */
	interface Transfer {
		int transfer(byte[] buffer, int offset, int length, int timeoutMs);
	}

	interface Clock {
		long nowMillis();
	}

	interface Sleeper {
		void sleep(long millis) throws InterruptedException;
	}

	/**
	 * The peer stopped draining the endpoint for longer than the deadline. The link is no longer
	 * usable: every packet behind this one, acknowledgements included, would wait as long.
	 */
	static final class StalledException extends IOException {
		StalledException(String message) {
			super(message);
		}
	}

	private static final String TAG = "AdbUsbChannel";
	private static final int MAX_TRANSIENT_FAILURES = 5;
	private static final long TRANSIENT_BACKOFF_MS = 20;

	private final Transfer transfer;
	private final int transferTimeoutMs;
	private final long stallDeadlineMs;
	private final Clock clock;
	private final Sleeper sleeper;

	/**
	 * @param transferTimeoutMs how long one transfer may block.
	 * @param stallDeadlineMs how long a buffer may go unfinished before the write fails as stalled.
	 */
	BulkWriter(Transfer transfer, int transferTimeoutMs, long stallDeadlineMs, Clock clock, Sleeper sleeper) {
		this.transfer = transfer;
		this.transferTimeoutMs = transferTimeoutMs;
		this.stallDeadlineMs = stallDeadlineMs;
		this.clock = clock;
		this.sleeper = sleeper;
	}

	void writeFully(byte[] buffer) throws IOException {
		int offset = 0;
		int failures = 0;
		long deadline = clock.nowMillis() + stallDeadlineMs;
		while (offset < buffer.length) {
			long startedAt = clock.nowMillis();
			int transferred = transfer.transfer(buffer, offset, buffer.length - offset, transferTimeoutMs);
			if (transferred > 0) {
				offset += transferred;
				failures = 0;
				continue;
			}
			/* A peer that is busy stops draining the endpoint, so a transfer that consumed its
			 * whole timeout is retried. One that failed immediately is usually a real error, but
			 * a long transfer can hit a transient failure, so give it a few attempts first. */
			long elapsed = clock.nowMillis() - startedAt;
			Log.w(TAG, "USB write stalled: " + transferred + " after " + elapsed + " ms, at "
					+ offset + "/" + buffer.length + " bytes");
			if (transferred < 0 && elapsed < transferTimeoutMs / 2) {
				if (++failures > MAX_TRANSIENT_FAILURES) {
					throw new IOException("USB bulk transfer failed " + failures + " times in a row");
				}
				backOff();
			}
			if (clock.nowMillis() >= deadline) {
				throw new StalledException("USB bulk transfer stalled for " + stallDeadlineMs + " ms");
			}
		}
	}

	private void backOff() throws IOException {
		try {
			sleeper.sleep(TRANSIENT_BACKOFF_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted during USB write");
		}
	}
}
