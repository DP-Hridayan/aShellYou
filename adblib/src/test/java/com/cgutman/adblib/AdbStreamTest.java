package com.cgutman.adblib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.IOException;

public class AdbStreamTest {

	@Test
	public void readsEverythingQueuedBeforeACloseArrived() throws Exception {
		AdbStream stream = new AdbStream(null, 0);
		stream.addPayload(new byte[]{1, 2});
		stream.addPayload(new byte[]{3});

		stream.notifyClose();

		assertArrayEquals(new byte[]{1, 2}, stream.read());
		assertArrayEquals(new byte[]{3}, stream.read());
		assertFalse(stream.isClosed());

		try {
			stream.read();
			fail("Expected a drained stream to report itself closed");
		} catch (IOException expected) {
			assertTrue(stream.isClosed());
		}
	}

	@Test
	public void closesStraightAwayWhenNothingWasQueued() throws Exception {
		AdbStream stream = new AdbStream(null, 0);

		stream.notifyClose();

		assertTrue(stream.isClosed());
		try {
			stream.read();
			fail("Expected a read on a closed stream to fail");
		} catch (IOException expected) {
			assertTrue(stream.isClosed());
		}
	}

	@Test
	public void closingAfterAPeerCloseSendsNothingBack() throws Exception {
		AdbStream stream = new AdbStream(null, 0);
		stream.addPayload(new byte[]{1});
		stream.notifyClose();

		/* The null connection is the assertion: answering the peer would dereference it. */
		stream.close();

		assertTrue(stream.isClosed());
	}
}
