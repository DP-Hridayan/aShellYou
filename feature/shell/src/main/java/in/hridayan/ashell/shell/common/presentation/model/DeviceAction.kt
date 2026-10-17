package `in`.hridayan.ashell.shell.common.presentation.model

/** The actions the device dock can offer, in the order they appear. */
enum class DeviceActionId {
    SCREEN,
    FILES,
    PORTS,
    SHARE_OUTPUT,
    SAVE_OUTPUT,
    ASK_AI
}

/** What a shell screen is attached to, which decides the device actions that make sense. */
enum class DeviceConnection {
    NONE,

    /** This phone itself, reached through Wireless Debugging. */
    OWN_DEVICE,

    OTHER_DEVICE
}

object DeviceActionCatalog {

    /**
     * Mirroring is only offered for another device, because mirroring this phone onto itself is an
     * endless hall of mirrors; port forwarding likewise, since this phone's ports are already local.
     * Output actions appear only while there is output to act on. The AI
     * agent needs no connection, so it is always offered when enabled, and always last.
     */
    fun actionsFor(
        connection: DeviceConnection,
        isAiEnabled: Boolean,
        hasOutput: Boolean
    ): List<DeviceActionId> = buildList {
        if (connection == DeviceConnection.OTHER_DEVICE) add(DeviceActionId.SCREEN)
        if (connection != DeviceConnection.NONE) add(DeviceActionId.FILES)
        if (connection == DeviceConnection.OTHER_DEVICE) add(DeviceActionId.PORTS)
        if (hasOutput) {
            add(DeviceActionId.SHARE_OUTPUT)
            add(DeviceActionId.SAVE_OUTPUT)
        }
        if (isAiEnabled) add(DeviceActionId.ASK_AI)
    }
}
