package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ModelFallbackExecutorTest {

    private val executor = ModelFallbackExecutor()

    @Test
    fun `returns the first successful result without trying later models`() = runTest {
        val attempted = mutableListOf<String>()

        val result = executor.execute(LlmProvider.Gemini, listOf("a", "b", "c")) { model ->
            attempted += model
            model.uppercase()
        }

        assertEquals("A", result)
        assertEquals(listOf("a"), attempted)
    }

    @Test
    fun `walks the chain in order when models fail with retryable errors`() = runTest {
        val attempted = mutableListOf<String>()

        val result = executor.execute(LlmProvider.Groq, listOf("a", "b", "c")) { model ->
            attempted += model
            if (model != "c") throw CloudNetworkException.RateLimited(null)
            model
        }

        assertEquals("c", result)
        assertEquals(listOf("a", "b", "c"), attempted)
    }

    @Test
    fun `treats a decommissioned model as retryable`() = runTest {
        val attempted = mutableListOf<String>()

        val result = executor.execute(LlmProvider.Groq, listOf("dead", "alive")) { model ->
            attempted += model
            if (model == "dead") throw CloudNetworkException.ModelUnavailable(model)
            model
        }

        assertEquals("alive", result)
        assertEquals(listOf("dead", "alive"), attempted)
    }

    @Test
    fun `retries server errors that are rate limits or upstream failures`() = runTest {
        val codes = listOf(429, 500, 503)

        codes.forEach { code ->
            val attempted = mutableListOf<String>()

            val result = executor.execute(LlmProvider.OpenRouter, listOf("a", "b")) { model ->
                attempted += model
                if (model == "a") throw CloudNetworkException.ServerError(code)
                model
            }

            assertEquals("b", result)
            assertEquals("HTTP $code should be retryable", listOf("a", "b"), attempted)
        }
    }

    @Test
    fun `stops immediately on an unauthorized error`() = runTest {
        val attempted = mutableListOf<String>()

        try {
            executor.execute(LlmProvider.Gemini, listOf("a", "b")) { model ->
                attempted += model
                throw CloudNetworkException.Unauthorized(LlmProvider.Gemini)
            }
            fail("Expected Unauthorized to propagate")
        } catch (e: CloudNetworkException.Unauthorized) {
            assertEquals(LlmProvider.Gemini, e.provider)
        }

        assertEquals(listOf("a"), attempted)
    }

    @Test
    fun `stops immediately when the account is out of credits`() = runTest {
        val attempted = mutableListOf<String>()

        try {
            executor.execute(LlmProvider.OpenRouter, listOf("a", "b")) { model ->
                attempted += model
                throw CloudNetworkException.InsufficientCredits(LlmProvider.OpenRouter)
            }
            fail("Expected InsufficientCredits to propagate")
        } catch (e: CloudNetworkException.InsufficientCredits) {
            assertEquals(LlmProvider.OpenRouter, e.provider)
        }

        assertEquals(listOf("a"), attempted)
    }

    @Test
    fun `rethrows the last retryable error when the chain is exhausted`() = runTest {
        try {
            executor.execute(LlmProvider.Groq, listOf("a", "b")) { model ->
                if (model == "a") {
                    throw CloudNetworkException.RateLimited(null)
                } else {
                    throw CloudNetworkException.ServerError(503)
                }
            }
            fail("Expected the last failure to propagate")
        } catch (e: CloudNetworkException.ServerError) {
            assertEquals(503, e.code)
        }
    }

    @Test
    fun `reports no active provider when the chain is empty`() = runTest {
        try {
            executor.execute(LlmProvider.OpenRouter, emptyList()) { it }
            fail("Expected an empty chain to fail")
        } catch (e: CloudNetworkException.ProviderNotConfigured) {
            assertEquals(LlmProvider.OpenRouter, e.provider)
        }
    }

    @Test
    fun `does not retry a client error that is not a dead model`() = runTest {
        val attempted = mutableListOf<String>()

        try {
            executor.execute(LlmProvider.Groq, listOf("a", "b")) { model ->
                attempted += model
                throw CloudNetworkException.ServerError(400)
            }
            fail("Expected HTTP 400 to propagate")
        } catch (e: CloudNetworkException.ServerError) {
            assertEquals(400, e.code)
        }

        assertTrue("HTTP 400 must not walk the chain", attempted == listOf("a"))
    }
}
