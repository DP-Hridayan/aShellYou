package `in`.hridayan.ashell.adbsideload.domain.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SideloadRequestParserTest {

    private val parser = SideloadRequestParser()

    @Test
    fun `single frame yields one block request`() {
        assertEquals(listOf(SideloadRequest.Block(12)), parser.feed("00000012".ascii()))
    }

    @Test
    fun `two frames in one packet yield two requests in order`() {
        assertEquals(
            listOf(SideloadRequest.Block(3), SideloadRequest.Block(0)),
            parser.feed("0000000300000000".ascii())
        )
    }

    @Test
    fun `frame split across packets is reassembled`() {
        assertTrue(parser.feed("0000".ascii()).isEmpty())
        assertEquals(listOf(SideloadRequest.Block(7)), parser.feed("0007".ascii()))
    }

    @Test
    fun `trailing partial frame is kept for the next packet`() {
        assertEquals(listOf(SideloadRequest.Block(1)), parser.feed("00000001000".ascii()))
        assertEquals(listOf(SideloadRequest.Block(2)), parser.feed("00002".ascii()))
    }

    @Test
    fun `done frame yields Done`() {
        assertEquals(listOf(SideloadRequest.Done), parser.feed("DONEDONE".ascii()))
    }

    @Test
    fun `fail frame yields Failed`() {
        assertEquals(listOf(SideloadRequest.Failed), parser.feed("FAILFAIL".ascii()))
    }

    @Test
    fun `non numeric frame yields Malformed`() {
        assertEquals(listOf(SideloadRequest.Malformed("abcdefgh")), parser.feed("abcdefgh".ascii()))
    }

    @Test
    fun `block frame followed by done frame in one packet`() {
        assertEquals(
            listOf(SideloadRequest.Block(5), SideloadRequest.Done),
            parser.feed("00000005DONEDONE".ascii())
        )
    }

    private fun String.ascii(): ByteArray = toByteArray(Charsets.US_ASCII)
}
