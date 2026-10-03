package `in`.hridayan.ashell.mirror.domain.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerLogClassifierTest {

    @Test
    fun `missing inject permission means view only`() {
        val line = "java.lang.SecurityException: Injecting input events requires the caller " +
            "(or the source of the instrumentation, if any) to have the INJECT_EVENTS permission."

        assertEquals(ServerLogSignal.INJECTION_DENIED, ServerLogClassifier.classify(line))
    }

    @Test
    fun `version mismatch is recognised`() {
        val line = "[server] ERROR: The server version (4.0) does not match the client (4.1)"

        assertEquals(ServerLogSignal.VERSION_MISMATCH, ServerLogClassifier.classify(line))
    }

    @Test
    fun `an encoder that cannot be created is an encoder failure`() {
        listOf(
            "[server] ERROR: Could not create default video encoder for h264",
            "[server] ERROR: Could not create video encoder 'c2.qti.avc.encoder' for h264",
            "[server] ERROR: Video encoder 'x' for h264 not found"
        ).forEach { line ->
            assertEquals(line, ServerLogSignal.ENCODER_FAILED, ServerLogClassifier.classify(line))
        }
    }

    @Test
    fun `an error while streaming is not blamed on the encoder`() {
        listOf(
            "[server] ERROR: Video encoding error",
            "[server] ERROR: Capture/encoding error: java.io.IOException: Connection reset by peer"
        ).forEach { line ->
            assertNull(line, ServerLogClassifier.classify(line))
        }
    }

    @Test
    fun `ordinary output is ignored`() {
        assertNull(ServerLogClassifier.classify("[server] INFO: Device: [Google] google Pixel 7"))
    }
}
