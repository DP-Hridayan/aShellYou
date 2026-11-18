package `in`.hridayan.ashell.qstiles.domain.model

import androidx.compose.ui.text.input.TextFieldValue
import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TileConfigSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun deserialize_withoutCollapsePanelOnClick_defaultsToFalse() {
        val legacyJson = """
            {
                "id": 1,
                "name": "Test Tile",
                "iconId": "terminal",
                "executionMode": 0,
                "activeState": {
                    "isToggleable": false,
                    "isActive": false,
                    "activeTileSubtitle": "On",
                    "inactiveTileSubtitle": "Off",
                    "activeCommand": "echo 1",
                    "inactiveCommand": ""
                },
                "isCustom": true
            }
        """.trimIndent()

        val config = json.decodeFromString<TileConfig>(legacyJson)

        assertFalse(config.collapsePanelOnClick)
        assertEquals("Test Tile", config.name)
    }

    @Test
    fun serializeAndDeserialize_withCollapsePanelOnClickTrue_preservesValue() {
        val original = TileConfig(
            id = 2,
            name = "Collapse Tile",
            iconId = "settings",
            executionMode = TileExecutionMode.SHIZUKU,
            activeState = TileActiveState(
                isToggleable = false,
                isActive = false,
                activeCommand = TextFieldValue("reboot"),
            ),
            isCustom = true,
            collapsePanelOnClick = true,
        )

        val serialized = json.encodeToString(TileConfig.serializer(), original)
        val deserialized = json.decodeFromString<TileConfig>(serialized)

        assertTrue(deserialized.collapsePanelOnClick)
        assertEquals(original.id, deserialized.id)
        assertEquals(original.name, deserialized.name)
    }

    @Test
    fun serializeAndDeserialize_withTcpIpExecutionMode_preservesValue() {
        val original = TileConfig(
            id = 3,
            name = "TCP Tile",
            iconId = "wifi",
            executionMode = TileExecutionMode.TCPIP,
            activeState = TileActiveState(
                isToggleable = false,
                isActive = false,
                activeCommand = TextFieldValue("svc wifi enable"),
            ),
            isCustom = true,
        )

        val serialized = json.encodeToString(TileConfig.serializer(), original)
        val deserialized = json.decodeFromString<TileConfig>(serialized)

        assertEquals(TileExecutionMode.TCPIP, deserialized.executionMode)
        assertEquals("TCP Tile", deserialized.name)
    }
}
