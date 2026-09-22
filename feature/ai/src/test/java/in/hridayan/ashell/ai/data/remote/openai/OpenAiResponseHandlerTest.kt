package `in`.hridayan.ashell.ai.data.remote.openai

import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiResponseHandlerTest {

    @Test
    fun `rejected keys surface as unauthorized`() {
        listOf(401, 403).forEach { status ->
            val result = OpenAiResponseHandler.toException(LlmProvider.Groq, status, "")
            assertTrue("HTTP $status", result is CloudNetworkException.Unauthorized)
        }
    }

    @Test
    fun `payment required surfaces as insufficient credits`() {
        val result = OpenAiResponseHandler.toException(LlmProvider.OpenRouter, 402, "")

        assertTrue(result is CloudNetworkException.InsufficientCredits)
        assertEquals(
            LlmProvider.OpenRouter,
            (result as CloudNetworkException.InsufficientCredits).provider
        )
    }

    @Test
    fun `reads the retry hint from the header`() {
        val result = OpenAiResponseHandler.toException(LlmProvider.Groq, 429, "", "17")

        assertEquals(17, (result as CloudNetworkException.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `reads the retry hint from the body when no header is present`() {
        val body = """{"error":{"message":"Rate limit reached. Please try again in 7.5s."}}"""

        val result = OpenAiResponseHandler.toException(LlmProvider.Groq, 429, body)

        assertEquals(7, (result as CloudNetworkException.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `a rate limit with no hint still maps cleanly`() {
        val result = OpenAiResponseHandler.toException(LlmProvider.Groq, 429, "")

        assertNull((result as CloudNetworkException.RateLimited).retryAfterSeconds)
    }

    @Test
    fun `a not found status means the model is gone`() {
        val result = OpenAiResponseHandler.toException(LlmProvider.Groq, 404, "")

        assertTrue(result is CloudNetworkException.ModelUnavailable)
    }

    @Test
    fun `recognises each provider's dead-model dialect behind a generic status`() {
        val bodies = listOf(
            """{"error":{"code":"model_decommissioned"}}""",
            """{"error":{"code":"model_not_found"}}""",
            """{"error":{"message":"No endpoints found for qwen/dead:free"}}""",
        )

        bodies.forEach { body ->
            val result = OpenAiResponseHandler.toException(LlmProvider.OpenRouter, 400, body)
            assertTrue(body, result is CloudNetworkException.ModelUnavailable)
        }
    }

    @Test
    fun `an ordinary bad request stays a server error so it is not retried`() {
        val result = OpenAiResponseHandler.toException(
            LlmProvider.Groq,
            400,
            """{"error":{"message":"invalid tool schema"}}"""
        )

        assertEquals(400, (result as CloudNetworkException.ServerError).code)
    }

    @Test
    fun `upstream failures stay server errors`() {
        val result = OpenAiResponseHandler.toException(LlmProvider.Groq, 503, "")

        assertEquals(503, (result as CloudNetworkException.ServerError).code)
    }

    @Test
    fun `an error carried inside a successful body is still an error`() {
        val result = OpenAiResponseHandler.inBandError(LlmProvider.OpenRouter, "no credit", 402)

        assertTrue(result is CloudNetworkException.InsufficientCredits)
    }

    @Test
    fun `a body with no error member produces nothing`() {
        assertNull(OpenAiResponseHandler.inBandError(LlmProvider.OpenRouter, null, null))
    }

    @Test
    fun `carries the provider's own wording on a rejected key`() {
        val body = """{"error":{"message":"User not found.","code":401}}"""

        val result = OpenAiResponseHandler.toException(LlmProvider.OpenRouter, 401, body)

        assertEquals("User not found.", (result as CloudNetworkException.Unauthorized).detail)
    }

    @Test
    fun `carries the provider's own wording on a server error`() {
        val body = """{"error":{"message":"tools[0].type is required"}}"""

        val result = OpenAiResponseHandler.toException(LlmProvider.Groq, 400, body)

        assertEquals("tools[0].type is required", (result as CloudNetworkException.ServerError).detail)
    }

    @Test
    fun `an unreachable model reported as forbidden stays retryable`() {
        val body = """{"error":{"message":"No endpoints found matching your data policy"}}"""

        val result = OpenAiResponseHandler.toException(LlmProvider.OpenRouter, 403, body)

        assertTrue(
            "a model the account cannot reach must not be mistaken for a bad key",
            result is CloudNetworkException.ModelUnavailable
        )
    }
}
