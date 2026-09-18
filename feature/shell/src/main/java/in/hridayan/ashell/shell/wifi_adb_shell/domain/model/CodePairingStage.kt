package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * How far a code pairing attempt has got, for the device the user actually pressed Pair on.
 *
 * The connection state alone cannot drive the card, because it is global: without knowing which
 * discovered device the attempt belongs to, every card would report progress at once.
 */
enum class CodePairingStage {
    /** Nothing in flight. */
    Idle,

    /** The pairing code is being checked against the device. */
    Pairing,

    /** The code was accepted and the connection is being established. */
    Connecting
}
