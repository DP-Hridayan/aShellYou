package `in`.hridayan.ashell.shell.common.presentation.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceActionCatalogTest {

    @Test
    fun `another connected device with output gets every action, ai last`() {
        assertEquals(
            listOf(
                DeviceActionId.SCREEN,
                DeviceActionId.FILES,
                DeviceActionId.SHARE_OUTPUT,
                DeviceActionId.SAVE_OUTPUT,
                DeviceActionId.ASK_AI
            ),
            DeviceActionCatalog.actionsFor(
                connection = DeviceConnection.OTHER_DEVICE,
                isAiEnabled = true,
                hasOutput = true
            )
        )
    }

    @Test
    fun `this phone over wireless debugging never offers screen`() {
        assertEquals(
            listOf(DeviceActionId.FILES, DeviceActionId.ASK_AI),
            DeviceActionCatalog.actionsFor(
                connection = DeviceConnection.OWN_DEVICE,
                isAiEnabled = true,
                hasOutput = false
            )
        )
    }

    @Test
    fun `output actions appear only while there is output`() {
        assertEquals(
            listOf(DeviceActionId.SHARE_OUTPUT, DeviceActionId.SAVE_OUTPUT),
            DeviceActionCatalog.actionsFor(
                connection = DeviceConnection.NONE,
                isAiEnabled = false,
                hasOutput = true
            )
        )
    }

    @Test
    fun `without a connection or output only ai remains`() {
        assertEquals(
            listOf(DeviceActionId.ASK_AI),
            DeviceActionCatalog.actionsFor(
                connection = DeviceConnection.NONE,
                isAiEnabled = true,
                hasOutput = false
            )
        )
    }

    @Test
    fun `nothing is offered when there is nothing to act on`() {
        assertEquals(
            emptyList<DeviceActionId>(),
            DeviceActionCatalog.actionsFor(
                connection = DeviceConnection.NONE,
                isAiEnabled = false,
                hasOutput = false
            )
        )
    }
}
