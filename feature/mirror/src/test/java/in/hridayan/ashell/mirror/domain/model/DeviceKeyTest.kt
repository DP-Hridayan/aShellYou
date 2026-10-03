package `in`.hridayan.ashell.mirror.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceKeyTest {

    @Test
    fun `a press is a down followed by an up for the same key`() {
        assertEquals(
            listOf(
                ControlMessage.InjectKeycode(action = InputAction.DOWN, keyCode = 4),
                ControlMessage.InjectKeycode(action = InputAction.UP, keyCode = 4)
            ),
            DeviceKey.BACK.pressMessages()
        )
    }

    @Test
    fun `screenshot presses the system screenshot key`() {
        assertEquals(120, DeviceKey.SCREENSHOT.keyCode)
    }
}
