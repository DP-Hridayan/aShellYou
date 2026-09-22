package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveLlmProviderUseCaseTest {

    @Test
    fun `resolves to null when no provider has been chosen`() = runTest {
        val settings = FakeSettingsRepository()

        val active = GetActiveLlmProviderUseCase(settings).invoke()

        assertNull(active)
    }

    @Test
    fun `resolves a stored provider id`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Groq.id)
        )

        val active = GetActiveLlmProviderUseCase(settings).invoke()

        assertEquals(LlmProvider.Groq, active)
    }

    @Test
    fun `resolves to null when the stored id is unknown`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to "a-provider-that-was-removed")
        )

        val active = GetActiveLlmProviderUseCase(settings).invoke()

        assertNull(active)
    }

    @Test
    fun `saving is refused when the chosen provider has no key`() = runTest {
        val settings = FakeSettingsRepository()
        val keys = FakeApiKeyRepository()
        val useCase = SetActiveLlmProviderUseCase(settings, keys)

        val outcome = useCase(LlmProvider.OpenRouter)

        assertEquals(SetActiveProviderOutcome.NoKeyFor(LlmProvider.OpenRouter), outcome)
        assertEquals("", settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `saving is refused when the stored key is blank`() = runTest {
        val settings = FakeSettingsRepository()
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Groq.id to "   "))
        val useCase = SetActiveLlmProviderUseCase(settings, keys)

        val outcome = useCase(LlmProvider.Groq)

        assertEquals(SetActiveProviderOutcome.NoKeyFor(LlmProvider.Groq), outcome)
        assertEquals("", settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `saving succeeds when the chosen provider has a key`() = runTest {
        val settings = FakeSettingsRepository()
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Groq.id to "gsk-test"))
        val useCase = SetActiveLlmProviderUseCase(settings, keys)

        val outcome = useCase(LlmProvider.Groq)

        assertEquals(SetActiveProviderOutcome.Saved, outcome)
        assertEquals(LlmProvider.Groq.id, settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `selecting none always succeeds and clears the stored id`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Gemini.id)
        )
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Gemini.id to "key"))
        val useCase = SetActiveLlmProviderUseCase(settings, keys)

        val outcome = useCase(null)

        assertEquals(SetActiveProviderOutcome.Saved, outcome)
        assertEquals("", settings.peekString(SettingsKeys.AiCloudProvider))
    }

    @Test
    fun `require reports no provider selected when active is none`() = runTest {
        val settings = FakeSettingsRepository()
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Gemini.id to "key"))

        val outcome = RequireActiveProviderKeyUseCase(
            GetActiveLlmProviderUseCase(settings),
            keys
        ).invoke()

        assertEquals(ActiveProviderKeyStatus.NoProviderSelected, outcome)
    }

    @Test
    fun `require reports the missing key for the active provider`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.OpenRouter.id)
        )
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Gemini.id to "key"))

        val outcome = RequireActiveProviderKeyUseCase(
            GetActiveLlmProviderUseCase(settings),
            keys
        ).invoke()

        assertEquals(ActiveProviderKeyStatus.NoKeyFor(LlmProvider.OpenRouter), outcome)
    }

    @Test
    fun `require allows a configured active provider`() = runTest {
        val settings = FakeSettingsRepository(
            mapOf(SettingsKeys.AiCloudProvider.name to LlmProvider.Gemini.id)
        )
        val keys = FakeApiKeyRepository(mapOf(LlmProvider.Gemini.id to "key"))

        val outcome = RequireActiveProviderKeyUseCase(
            GetActiveLlmProviderUseCase(settings),
            keys
        ).invoke()

        assertTrue(outcome is ActiveProviderKeyStatus.Allowed)
        assertEquals(LlmProvider.Gemini, (outcome as ActiveProviderKeyStatus.Allowed).provider)
    }
}
