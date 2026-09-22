package `in`.hridayan.ashell.ai.data.remote.openai

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolCall
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolResponse
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiRequestMapperTest {

    private class FakeTool(
        override val name: String,
        override val parametersSchema: ToolSchema?,
    ) : AiTool {
        override val description = "test tool"
        override suspend fun execute(args: JsonObject?): String = ""
    }

    @Test
    fun `lowercases schema types that Gemini writes in upper case`() {
        val tool = FakeTool(
            name = "execute_command",
            parametersSchema = ToolSchema(
                type = ToolSchemaType.OBJECT,
                properties = mapOf(
                    "command" to ToolSchemaProperty(ToolSchemaType.STRING, "the command"),
                    "count" to ToolSchemaProperty(ToolSchemaType.INTEGER),
                ),
                required = listOf("command"),
            )
        )

        val schema = OpenAiRequestMapper.mapTools(listOf(tool)).single().function.parameters

        assertEquals("object", schema?.type)
        assertEquals("string", schema?.properties?.get("command")?.type)
        assertEquals("integer", schema?.properties?.get("count")?.type)
        assertEquals(listOf("command"), schema?.required)
    }

    @Test
    fun `gives array properties an items member so the schema stays valid`() {
        val tool = FakeTool(
            name = "batch",
            parametersSchema = ToolSchema(
                type = ToolSchemaType.OBJECT,
                properties = mapOf("items" to ToolSchemaProperty(ToolSchemaType.ARRAY)),
            )
        )

        val property = OpenAiRequestMapper.mapTools(listOf(tool))
            .single().function.parameters?.properties?.get("items")

        assertEquals("array", property?.type)
        assertNotNull("an array schema without items is rejected by the provider", property?.items)
    }

    @Test
    fun `leaves non-array properties without an items member`() {
        val tool = FakeTool(
            name = "simple",
            parametersSchema = ToolSchema(
                type = ToolSchemaType.OBJECT,
                properties = mapOf("command" to ToolSchemaProperty(ToolSchemaType.STRING)),
            )
        )

        val property = OpenAiRequestMapper.mapTools(listOf(tool))
            .single().function.parameters?.properties?.get("command")

        assertNull(property?.items)
    }

    @Test
    fun `puts the system prompt first`() {
        val messages = OpenAiRequestMapper.mapHistory(
            systemPrompt = "you are a shell assistant",
            history = listOf(LlmMessage(role = "user", content = "hi"))
        )

        assertEquals("system", messages.first().role)
        assertEquals("you are a shell assistant", messages.first().content)
    }

    @Test
    fun `maps the model role onto assistant`() {
        val messages = OpenAiRequestMapper.mapHistory(
            systemPrompt = "s",
            history = listOf(LlmMessage(role = "model", content = "hello"))
        )

        assertEquals("assistant", messages[1].role)
    }

    @Test
    fun `emits one tool message per tool call id`() {
        val history = listOf(
            LlmMessage(
                role = "model",
                content = "",
                toolCalls = listOf(
                    LlmToolCall("first", null, "call_a"),
                    LlmToolCall("second", null, "call_b"),
                ),
                providerId = LlmProvider.Groq.id,
            ),
            LlmMessage(
                role = "user",
                content = "",
                toolResponses = listOf(
                    LlmToolResponse("first", "ok a", "call_a"),
                    LlmToolResponse("second", "ok b", "call_b"),
                ),
                providerId = LlmProvider.Groq.id,
            ),
        )

        val messages = OpenAiRequestMapper.mapHistory("s", history)
        val assistant = messages[1]
        val toolMessages = messages.filter { it.role == "tool" }

        assertEquals(listOf("call_a", "call_b"), assistant.toolCalls?.map { it.id })
        assertEquals(listOf("call_a", "call_b"), toolMessages.map { it.toolCallId })
        assertEquals(listOf("ok a", "ok b"), toolMessages.map { it.content })
    }

    @Test
    fun `falls back to the tool name when a provider supplied no call id`() {
        val history = listOf(
            LlmMessage(
                role = "user",
                content = "",
                toolResponses = listOf(LlmToolResponse("get_battery", "82%")),
            )
        )

        val toolMessage = OpenAiRequestMapper.mapHistory("s", history).single { it.role == "tool" }

        assertEquals("get_battery", toolMessage.toolCallId)
    }

    @Test
    fun `encodes tool arguments as a json string`() {
        val call = LlmToolCall(
            name = "execute_command",
            args = buildJsonObject { put("command", JsonPrimitive("ls")) },
            id = "call_1",
        )

        assertEquals("""{"command":"ls"}""", OpenAiRequestMapper.encodeArguments(call))
    }

    @Test
    fun `encodes an absent argument object as an empty json object`() {
        assertEquals("{}", OpenAiRequestMapper.encodeArguments(LlmToolCall("t", null)))
    }

    @Test
    fun `rebuilds a transcript recorded under a different provider`() {
        val history = listOf(
            LlmMessage(
                role = "model",
                content = "from gemini",
                rawProviderData = """{"role":"model","parts":[{"text":"from gemini"}]}""",
                providerId = LlmProvider.Gemini.id,
            )
        )

        val messages = OpenAiRequestMapper.mapHistory("s", history)

        assertEquals(2, messages.size)
        assertEquals("assistant", messages[1].role)
        assertEquals("from gemini", messages[1].content)
        assertTrue(
            "Gemini payloads must not leak into an OpenAI request",
            messages.none { it.content?.contains("parts") == true }
        )
    }

    @Test
    fun `omits the tools member when no tools are enabled`() {
        val request = OpenAiRequestMapper.createRequest(
            model = "m",
            messages = OpenAiRequestMapper.mapHistory("s", emptyList()),
            tools = emptyList(),
            stream = false,
        )

        assertNull(request.tools)
    }
}
