package `in`.hridayan.ashell.ai.data.remote.catalog

import `in`.hridayan.ashell.ai.data.remote.catalog.dto.OpenRouterModelDto
import `in`.hridayan.ashell.ai.data.remote.catalog.dto.OpenRouterPricingDto
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmModel
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenRouterModelSelectorTest {

    private fun model(
        id: String,
        prompt: String? = "0",
        completion: String? = "0",
        params: List<String> = listOf("tools"),
        contextLength: Long? = 1000,
    ) = OpenRouterModelDto(
        id = id,
        contextLength = contextLength,
        pricing = OpenRouterPricingDto(prompt, completion),
        supportedParameters = params,
    )

    @Test
    fun `keeps only models that cost nothing to prompt and to complete`() {
        val catalog = listOf(
            model("free/a"),
            model("paid/b", prompt = "0.0000015"),
            model("paid/c", completion = "0.000003"),
            model("free/d", prompt = "0.0", completion = "0.0"),
        )

        val free = OpenRouterModelSelector.toFreeModels(catalog).map { it.id }

        assertEquals(listOf("free/a", "free/d"), free)
    }

    @Test
    fun `treats a model with no pricing as not free`() {
        val catalog = listOf(OpenRouterModelDto(id = "unknown/a", pricing = null))

        assertTrue(OpenRouterModelSelector.toFreeModels(catalog).isEmpty())
    }

    @Test
    fun `identifies free models by price rather than by a name suffix`() {
        val catalog = listOf(model("vendor/no-suffix-but-free"))

        val free = OpenRouterModelSelector.toFreeModels(catalog)

        assertEquals(listOf("vendor/no-suffix-but-free"), free.map { it.id })
        assertTrue(free.single().supportsTools)
    }

    @Test
    fun `records which free models accept tool calls`() {
        val catalog = listOf(
            model("free/with-tools", params = listOf("tools", "temperature")),
            model("free/without-tools", params = listOf("temperature")),
        )

        val free = OpenRouterModelSelector.toFreeModels(catalog)

        assertTrue(free.single { it.id == "free/with-tools" }.supportsTools)
        assertFalse(free.single { it.id == "free/without-tools" }.supportsTools)
    }

    @Test
    fun `excludes tool-incapable models when tools are required`() {
        val models = listOf(
            LlmModel("free/with-tools", supportsTools = true),
            LlmModel("free/without-tools", supportsTools = false),
        )

        val chain = OpenRouterModelSelector.chain(models, ModelTier.LITE, toolsRequired = true)

        assertEquals(listOf("free/with-tools"), chain)
    }

    @Test
    fun `includes tool-incapable models when tools are not required`() {
        val models = listOf(
            LlmModel("free/with-tools", supportsTools = true),
            LlmModel("free/without-tools", supportsTools = false),
        )

        val chain = OpenRouterModelSelector.chain(models, ModelTier.LITE, toolsRequired = false)

        assertTrue(chain.containsAll(listOf("free/with-tools", "free/without-tools")))
    }

    @Test
    fun `leads with the preferred router when it is still offered`() {
        val models = listOf(
            LlmModel("some/other:free", supportsTools = true),
            LlmModel("openrouter/free", supportsTools = true),
        )

        val chain = OpenRouterModelSelector.chain(models, ModelTier.QUALITY, toolsRequired = true)

        assertEquals("openrouter/free", chain.first())
    }

    @Test
    fun `ranks unknown models by context window when no preference applies`() {
        val models = listOf(
            LlmModel("small", supportsTools = true),
            LlmModel("huge", supportsTools = true),
            LlmModel("medium", supportsTools = true),
        )
        val contexts = mapOf("small" to 8_000L, "medium" to 128_000L, "huge" to 1_000_000L)

        val chain = OpenRouterModelSelector.chain(
            models,
            ModelTier.QUALITY,
            toolsRequired = true,
            contextLengths = contexts,
        )

        assertEquals(listOf("huge", "medium", "small"), chain)
    }

    @Test
    fun `survives a complete catalog turnover with no preferred slug left`() {
        val models = (1..3).map { LlmModel("brand-new/model-$it:free", supportsTools = true) }

        val chain = OpenRouterModelSelector.chain(models, ModelTier.QUALITY, toolsRequired = true)

        assertEquals(3, chain.size)
    }

    @Test
    fun `caps the chain so one bad day is not twenty sequential failures`() {
        val models = (1..20).map { LlmModel("free/model-$it", supportsTools = true) }

        val chain = OpenRouterModelSelector.chain(models, ModelTier.LITE, toolsRequired = true)

        assertTrue("chain was ${chain.size}", chain.size <= 5)
    }

    @Test
    fun `produces an empty chain when nothing qualifies`() {
        val models = listOf(LlmModel("free/no-tools", supportsTools = false))

        assertTrue(
            OpenRouterModelSelector.chain(models, ModelTier.LITE, toolsRequired = true).isEmpty()
        )
    }

    @Test
    fun `reads context lengths keyed by model id`() {
        val catalog = listOf(
            model("a", contextLength = 4096),
            model("b", contextLength = null),
        )

        assertEquals(mapOf("a" to 4096L), OpenRouterModelSelector.contextLengths(catalog))
    }
}
