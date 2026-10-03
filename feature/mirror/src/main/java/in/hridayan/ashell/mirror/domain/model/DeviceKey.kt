package `in`.hridayan.ashell.mirror.domain.model

/** Hardware and navigation keys the mirror offers as buttons, with their Android key codes. */
enum class DeviceKey(val keyCode: Int) {
    BACK(4),
    HOME(3),
    RECENTS(187),
    VOLUME_UP(24),
    VOLUME_DOWN(25),
    POWER(26),

    /** The system screenshot key, which saves a screenshot on the mirrored device itself. */
    SCREENSHOT(120);

    fun pressMessages(): List<ControlMessage> = listOf(
        ControlMessage.InjectKeycode(action = InputAction.DOWN, keyCode = keyCode),
        ControlMessage.InjectKeycode(action = InputAction.UP, keyCode = keyCode)
    )
}
