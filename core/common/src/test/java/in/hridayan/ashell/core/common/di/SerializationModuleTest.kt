package `in`.hridayan.ashell.core.common.di

import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SerializationModuleTest {

    @Serializable
    private data class SamplePayload(
        val name: String,
        val count: Int = 1,
    )

    @Test
    fun `default Json preserves prettyPrint, lenient parsing, and ignoreUnknownKeys`() {
        val json = SerializationModule.provideJson()

        val encoded = json.encodeToString(SamplePayload(name = "test"))
        val decoded = json.decodeFromString<SamplePayload>("""{name: "test", extra: true}""")

        assertTrue(encoded.contains("\n"))
        assertEquals(SamplePayload(name = "test"), decoded)
    }

    @Test
    fun `compact Json omits whitespace, encodes defaults, and ignores unknown keys`() {
        val json = SerializationModule.provideCompactJson()

        val encoded = json.encodeToString(SamplePayload(name = "test"))
        val decoded = json.decodeFromString<SamplePayload>("""{"name":"test","extra":true}""")

        assertFalse(encoded.contains("\n"))
        assertTrue(encoded.contains("\"count\":1"))
        assertEquals(SamplePayload(name = "test"), decoded)
    }
}
