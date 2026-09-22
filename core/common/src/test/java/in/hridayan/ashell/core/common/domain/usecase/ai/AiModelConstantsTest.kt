package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.constants.AiModelConstants
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiModelConstantsTest {

    @Test
    fun `gemini and groq expose a chain for every tier`() {
        val staticProviders = listOf(LlmProvider.Gemini, LlmProvider.Groq)

        staticProviders.forEach { provider ->
            ModelTier.entries.forEach { tier ->
                assertTrue(
                    "${provider.id} has no $tier chain",
                    AiModelConstants.staticChain(provider, tier, toolsRequired = false).isNotEmpty()
                )
            }
        }
    }

    @Test
    fun `every static provider offers at least one tool-capable model per tier`() {
        val staticProviders = listOf(LlmProvider.Gemini, LlmProvider.Groq)

        staticProviders.forEach { provider ->
            ModelTier.entries.forEach { tier ->
                assertTrue(
                    "${provider.id} has no tool-capable $tier model",
                    AiModelConstants.staticChain(provider, tier, toolsRequired = true).isNotEmpty()
                )
            }
        }
    }

    @Test
    fun `requiring tools never widens the chain`() {
        LlmProvider.all.forEach { provider ->
            ModelTier.entries.forEach { tier ->
                val all = AiModelConstants.staticChain(provider, tier, toolsRequired = false)
                val toolsOnly = AiModelConstants.staticChain(provider, tier, toolsRequired = true)

                assertTrue(
                    "${provider.id}/$tier tool chain is not a subset",
                    all.containsAll(toolsOnly)
                )
            }
        }
    }

    @Test
    fun `chains preserve declared preference order`() {
        val declared = AiModelConstants.models(LlmProvider.Gemini, ModelTier.QUALITY).map { it.id }
        val chain = AiModelConstants.staticChain(LlmProvider.Gemini, ModelTier.QUALITY, false)

        assertEquals(declared, chain)
    }

    @Test
    fun `chains contain no duplicates`() {
        LlmProvider.all.forEach { provider ->
            ModelTier.entries.forEach { tier ->
                val chain = AiModelConstants.staticChain(provider, tier, toolsRequired = false)
                assertEquals(
                    "${provider.id}/$tier contains duplicates",
                    chain.distinct(),
                    chain
                )
            }
        }
    }

    @Test
    fun `openrouter has no static chain because it is discovered at runtime`() {
        ModelTier.entries.forEach { tier ->
            assertTrue(
                AiModelConstants.staticChain(LlmProvider.OpenRouter, tier, false).isEmpty()
            )
        }
    }

    @Test
    fun `openrouter seeds are available as a last resort floor`() {
        assertTrue(AiModelConstants.openRouterSeedModels.isNotEmpty())
        assertTrue(AiModelConstants.openRouterSeedModels.any { it.supportsTools })
    }

    @Test
    fun `gemini chains lead with its cheapest models`() {
        ModelTier.entries.forEach { tier ->
            val chain = AiModelConstants.staticChain(LlmProvider.Gemini, tier, toolsRequired = false)

            assertTrue(
                "$tier chain leads with ${chain.first()}; a lite model should come first so the " +
                    "common case answers without walking past rate-limited models",
                chain.first().contains("lite")
            )
        }
    }
}
