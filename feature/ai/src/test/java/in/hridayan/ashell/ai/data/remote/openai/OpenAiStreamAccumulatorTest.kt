package `in`.hridayan.ashell.ai.data.remote.openai

import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiDeltaFunctionCall
import `in`.hridayan.ashell.ai.data.remote.openai.dto.OpenAiDeltaToolCall
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiStreamAccumulatorTest {

    @Test
    fun `concatenates argument fragments before parsing`() {
        val accumulator = OpenAiStreamAccumulator()

        accumulator.appendToolCalls(
            listOf(
                OpenAiDeltaToolCall(
                    index = 0,
                    id = "call_1",
                    function = OpenAiDeltaFunctionCall(name = "execute_command")
                )
            )
        )
        listOf("""{"comm""", """and":"ls""", """ -la"}""").forEach { fragment ->
            accumulator.appendToolCalls(
                listOf(
                    OpenAiDeltaToolCall(
                        index = 0,
                        function = OpenAiDeltaFunctionCall(arguments = fragment)
                    )
                )
            )
        }

        val calls = accumulator.toolCalls()

        assertEquals(1, calls.size)
        assertEquals("execute_command", calls[0].name)
        assertEquals("call_1", calls[0].id)
        assertEquals("ls -la", calls[0].args?.get("command")?.jsonPrimitive?.content)
    }

    @Test
    fun `keeps interleaved tool calls apart by index`() {
        val accumulator = OpenAiStreamAccumulator()

        accumulator.appendToolCalls(
            listOf(
                OpenAiDeltaToolCall(0, "call_a", OpenAiDeltaFunctionCall(name = "first")),
                OpenAiDeltaToolCall(1, "call_b", OpenAiDeltaFunctionCall(name = "second")),
            )
        )
        accumulator.appendToolCalls(
            listOf(OpenAiDeltaToolCall(0, function = OpenAiDeltaFunctionCall(arguments = """{"a":""")))
        )
        accumulator.appendToolCalls(
            listOf(OpenAiDeltaToolCall(1, function = OpenAiDeltaFunctionCall(arguments = """{"b":""")))
        )
        accumulator.appendToolCalls(
            listOf(OpenAiDeltaToolCall(0, function = OpenAiDeltaFunctionCall(arguments = """1}""")))
        )
        accumulator.appendToolCalls(
            listOf(OpenAiDeltaToolCall(1, function = OpenAiDeltaFunctionCall(arguments = """2}""")))
        )

        val calls = accumulator.toolCalls()

        assertEquals(2, calls.size)
        assertEquals(listOf("first", "second"), calls.map { it.name })
        assertEquals(listOf("call_a", "call_b"), calls.map { it.id })
        assertEquals("1", calls[0].args?.get("a")?.jsonPrimitive?.content)
        assertEquals("2", calls[1].args?.get("b")?.jsonPrimitive?.content)
    }

    @Test
    fun `a call with no arguments yields null args rather than failing`() {
        val accumulator = OpenAiStreamAccumulator()

        accumulator.appendToolCalls(
            listOf(OpenAiDeltaToolCall(0, "call_1", OpenAiDeltaFunctionCall(name = "get_battery")))
        )

        val calls = accumulator.toolCalls()

        assertEquals(1, calls.size)
        assertNull(calls[0].args)
    }

    @Test
    fun `fragments that never carried a name are dropped`() {
        val accumulator = OpenAiStreamAccumulator()

        accumulator.appendToolCalls(
            listOf(OpenAiDeltaToolCall(0, function = OpenAiDeltaFunctionCall(arguments = "{}")))
        )

        assertTrue(accumulator.hasToolCalls())
        assertTrue(accumulator.toolCalls().isEmpty())
    }

    @Test
    fun `accumulates streamed text`() {
        val accumulator = OpenAiStreamAccumulator()

        listOf("Hello", ", ", "world").forEach(accumulator::appendText)

        assertEquals("Hello, world", accumulator.text())
        assertTrue(accumulator.toolCalls().isEmpty())
    }

    @Test
    fun `keeps reasoning apart from the answer`() {
        val accumulator = OpenAiStreamAccumulator()

        accumulator.appendReasoning("The user wants ")
        accumulator.appendReasoning("a greeting.")
        accumulator.appendText("Hello")

        assertEquals("Hello", accumulator.text())
        assertEquals("The user wants a greeting.", accumulator.reasoning())
    }

    @Test
    fun `reports no reasoning when the model sent none`() {
        val accumulator = OpenAiStreamAccumulator()

        accumulator.appendText("Hello")

        assertNull(accumulator.reasoning())
    }
}
