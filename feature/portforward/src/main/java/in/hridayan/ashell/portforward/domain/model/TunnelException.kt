package `in`.hridayan.ashell.portforward.domain.model

/** Carries a [TunnelError] through a failed [Result]. */
class TunnelException(val error: TunnelError) : Exception(error.toString())

/** The [TunnelError] behind this failure, or [TunnelError.Unexpected] when it has none. */
fun Throwable.toTunnelError(): TunnelError =
    (this as? TunnelException)?.error ?: TunnelError.Unexpected(message)
