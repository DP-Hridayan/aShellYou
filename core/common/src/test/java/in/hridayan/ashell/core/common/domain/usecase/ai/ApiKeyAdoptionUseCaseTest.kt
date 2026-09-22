package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiKeyAdoptionUseCaseTest {

    @Test
    fun `the first key saved becomes the active provider`() = runTest {
        val settings = FakeSettingsRepository()
        val keys = FakeApiKeyRepository()

        SaveApiKeyUseCase(keys, settings).invoke(LlmProvider.Groq, "gsk-test")

        assertEquals(LlmProvider.Groq.id, settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `a second key does not steal the active slot`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Gemini.id)
        )
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Gemini.id to "existing"))

        SaveApiKeyUseCase(keys, settings).invoke(LlmProvider.OpenRouter, "sk-or-test")

        assertEquals(LlmProvider.Gemini.id, settings.peekString(SettingsKeys.AiCloudProvider))
        assertEquals("sk-or-test", keys.getKey(LlmProvider.OpenRouter))
    }

    @Test
    fun `a sole existing key is adopted even when another provider is already active`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Gemini.id)
        )
        val keys = FakeApiKeyRepository()

        SaveApiKeyUseCase(keys, settings).invoke(LlmProvider.Groq, "gsk-test")

        assertEquals(LlmProvider.Groq.id, settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `re-saving the active provider's key leaves the selection alone`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Groq.id)
        )
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Groq.id to "old"))

        SaveApiKeyUseCase(keys, settings).invoke(LlmProvider.Groq, "new")

        assertEquals(LlmProvider.Groq.id, settings.peekString(SettingsKeys.AiCloudProvider))
        assertEquals("new", keys.getKey(LlmProvider.Groq))
    }

    @Test
    fun `a blank key is not stored and does not change the selection`() = runTest {
        val settings = FakeSettingsRepository()
        val keys = FakeApiKeyRepository()

        SaveApiKeyUseCase(keys, settings).invoke(LlmProvider.Groq, "   ")

        assertNull(keys.getKey(LlmProvider.Groq))
        assertEquals("", settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `deleting the active key falls through to the next provider that has one`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Gemini.id)
        )
        val keys = FakeApiKeyRepository(
            mapOf(
                LlmProvider.Gemini.id to "a",
                LlmProvider.OpenRouter.id to "c"
            )
        )

        DeleteApiKeyUseCase(keys, settings, GetActiveLlmProviderUseCase(settings)).invoke(LlmProvider.Gemini)

        assertEquals(LlmProvider.OpenRouter.id, settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `fall-through follows declaration order when several keys remain`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.OpenRouter.id)
        )
        val keys = FakeApiKeyRepository(
            mapOf(
                LlmProvider.Gemini.id to "a",
                LlmProvider.Groq.id to "b",
                LlmProvider.OpenRouter.id to "c"
            )
        )

        DeleteApiKeyUseCase(keys, settings, GetActiveLlmProviderUseCase(settings)).invoke(LlmProvider.OpenRouter)

        assertEquals(LlmProvider.Gemini.id, settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `deleting the last key clears the active provider`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Groq.id)
        )
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Groq.id to "b"))

        DeleteApiKeyUseCase(keys, settings, GetActiveLlmProviderUseCase(settings)).invoke(LlmProvider.Groq)

        assertEquals("", settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `deleting a non-active key leaves the selection alone`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Gemini.id)
        )
        val keys = FakeApiKeyRepository(
            mapOf(
                LlmProvider.Gemini.id to "a",
                LlmProvider.Groq.id to "b"
            )
        )

        DeleteApiKeyUseCase(keys, settings, GetActiveLlmProviderUseCase(settings)).invoke(LlmProvider.Groq)

        assertEquals(LlmProvider.Gemini.id, settings.peekString(SettingsKeys.AiCloudProvider))
        assertNull(keys.getKey(LlmProvider.Groq))
    }
}
