package `in`.hridayan.ashell.portforward.data.session

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.portforward.domain.session.PortForwardForegroundSession
import `in`.hridayan.ashell.portforward.service.PortForwardService
import javax.inject.Inject

/**
 * Starts and stops [PortForwardService].
 *
 * Android refuses to start a foreground service while the app is in the background, by throwing an
 * [IllegalStateException] subclass, and refuses it with a [SecurityException] when the service type's
 * requirements are not met. Both are returned as failures.
 */
class PortForwardForegroundSessionImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PortForwardForegroundSession {

    override fun start(): Result<Unit> = try {
        PortForwardService.start(context)
        Result.success(Unit)
    } catch (e: IllegalStateException) {
        Log.e(TAG, "Could not start the port forwarding service", e)
        Result.failure(e)
    } catch (e: SecurityException) {
        Log.e(TAG, "Could not start the port forwarding service", e)
        Result.failure(e)
    }

    override fun stop() = PortForwardService.stop(context)

    private companion object {
        const val TAG = "PortForwardSession"
    }
}
