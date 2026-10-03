package `in`.hridayan.ashell.mirror.domain.model

/** A message from this app to the server on the control socket. */
sealed interface ControlMessage {

    data class InjectKeycode(
        val action: Int,
        val keyCode: Int,
        val repeat: Int = 0,
        val metaState: Int = 0
    ) : ControlMessage

    /** Typed text. The server can only inject ASCII and a few accented characters this way. */
    data class InjectText(val text: String) : ControlMessage

    /**
     * One pointer's change. Send one message per pointer; the server derives Android's
     * `ACTION_POINTER_*` codes itself.
     *
     * @property pressure 0 to 1.
     */
    data class InjectTouch(
        val action: Int,
        val pointerId: Long,
        val position: DevicePosition,
        val pressure: Float,
        val actionButton: Int = 0,
        val buttons: Int = 0
    ) : ControlMessage

    /** @property horizontal and [vertical] are scroll amounts between -16 and 16. */
    data class InjectScroll(
        val position: DevicePosition,
        val horizontal: Float,
        val vertical: Float,
        val buttons: Int = 0
    ) : ControlMessage

    /** Back when the screen is on; turns it on otherwise. */
    data class BackOrScreenOn(val action: Int) : ControlMessage

    data object ExpandNotificationPanel : ControlMessage

    data object ExpandSettingsPanel : ControlMessage

    data object CollapsePanels : ControlMessage

    /**
     * Puts [text] on the device's clipboard. With [paste], also pastes it into the focused field,
     * which is how text that [InjectText] cannot carry gets typed.
     */
    data class SetClipboard(val sequence: Long, val text: String, val paste: Boolean) : ControlMessage

    /** Turns the device's physical screen off or on while mirroring continues. */
    data class SetDisplayPower(val on: Boolean) : ControlMessage

    data object RotateDevice : ControlMessage

    /** Asks the server to restart encoding, which yields a fresh config packet and key frame. */
    data object ResetVideo : ControlMessage
}
