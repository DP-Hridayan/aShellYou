package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.usecase.ParseHttpHeadUseCase.HttpHead
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParseHttpHeadUseCaseTest {

    private val parse = ParseHttpHeadUseCase()

    @Test
    fun `status and server header`() {
        assertEquals(
            HttpHead(status = 200, server = "vite"),
            parse(listOf("HTTP/1.1 200 OK", "Content-Type: text/html", "Server: vite"))
        )
    }

    @Test
    fun `header names are case insensitive`() {
        assertEquals("nginx/1.25", parse(listOf("HTTP/1.0 404 Not Found", "server:  nginx/1.25 "))?.server)
    }

    @Test
    fun `missing server header`() {
        assertEquals(HttpHead(status = 301, server = null), parse(listOf("HTTP/1.1 301 Moved Permanently")))
    }

    @Test
    fun `http2 style status line`() {
        assertEquals(204, parse(listOf("HTTP/2 204"))?.status)
    }

    @Test
    fun `non http reply`() {
        assertNull(parse(listOf("SSH-2.0-OpenSSH_9.6")))
    }

    @Test
    fun `empty reply`() {
        assertNull(parse(emptyList()))
    }
}
