package `in`.hridayan.ashell.portforward.domain.notification

import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec

/** Tells the user, while the app is in the background, that a tunnel stopped on its own. */
interface TunnelAlertNotifier {
    suspend fun tunnelFailed(spec: TunnelSpec, error: TunnelError)
}
