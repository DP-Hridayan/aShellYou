package `in`.hridayan.ashell.ai.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderErrorBodyTest {

    @Test
    fun `reads the provider's wording out of its error envelope`() {
        val body = """{"error":{"message":"User not found.","code":401}}"""

        assertEquals("User not found.", ProviderErrorBody.detail(body))
    }

    @Test
    fun `reads groq's envelope`() {
        val body =
            """{"error":{"message":"Invalid API Key","type":"invalid_request_error","code":"invalid_api_key"}}"""

        assertEquals("Invalid API Key", ProviderErrorBody.detail(body))
    }

    @Test
    fun `reads gemini's envelope`() {
        val body = """{"error":{"code":400,"message":"API key not valid.","status":"INVALID"}}"""

        assertEquals("API key not valid.", ProviderErrorBody.detail(body))
    }

    @Test
    fun `falls back to the raw body when the shape is unfamiliar`() {
        assertEquals("upstream exploded", ProviderErrorBody.detail("upstream exploded"))
    }

    @Test
    fun `ignores an empty body`() {
        assertNull(ProviderErrorBody.detail(""))
        assertNull(ProviderErrorBody.detail("   "))
    }

    @Test
    fun `caps a runaway body so it stays presentable`() {
        val detail = ProviderErrorBody.detail("x".repeat(5000))

        assertTrue("was ${detail?.length}", (detail?.length ?: 0) <= 300)
    }

    @Test
    fun `survives a truncated json body`() {
        assertEquals("""{"error":{"message":""", ProviderErrorBody.detail("""{"error":{"message":"""))
    }
}
