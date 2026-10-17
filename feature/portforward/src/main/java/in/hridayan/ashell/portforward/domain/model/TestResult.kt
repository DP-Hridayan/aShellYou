package `in`.hridayan.ashell.portforward.domain.model

/** The outcome of testing whether a tunnel's target is reachable. */
sealed interface TestResult {

    /**
     * @property httpStatus the status code of an HTTP reply, or null when the target did not speak
     * HTTP.
     * @property server the HTTP `Server` header, when present.
     */
    data class Reachable(
        val latencyMillis: Long,
        val httpStatus: Int?,
        val server: String?
    ) : TestResult

    data class Unreachable(val error: TunnelError) : TestResult
}
