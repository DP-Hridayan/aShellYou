package `in`.hridayan.ashell.mirror.domain.protocol

/** A server log line that changes what the session should do. */
enum class ServerLogSignal {
    /** The device refused input injection; video keeps working, so the session goes view only. */
    INJECTION_DENIED,
    VERSION_MISMATCH,
    ENCODER_FAILED
}

/** Recognises the v4.1 server's log messages that the session reacts to. */
object ServerLogClassifier {

    private const val INJECTION_MARKER = "INJECT_EVENTS"
    private const val VERSION_MARKER = "does not match the client"

    /**
     * Only failures to create the encoder. Errors logged while streaming, such as "Video encoding
     * error", are also what the server prints when its video socket is reset, so they say nothing
     * about the encoder.
     */
    private val encoderMarkers = listOf(
        "Could not create default video encoder",
        "Could not create video encoder",
        "Video encoder '"
    )

    fun classify(line: String): ServerLogSignal? = when {
        INJECTION_MARKER in line -> ServerLogSignal.INJECTION_DENIED
        VERSION_MARKER in line -> ServerLogSignal.VERSION_MISMATCH
        encoderMarkers.any { it in line } -> ServerLogSignal.ENCODER_FAILED
        else -> null
    }
}
