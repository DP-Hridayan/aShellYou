package `in`.hridayan.ashell.adbsideload.data.session

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.adbsideload.domain.session.SideloadForegroundSession
import `in`.hridayan.ashell.adbsideload.service.SideloadService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Starts and stops the foreground service that keeps a transfer alive.
 *
 * A failure to start is logged rather than propagated: the transfer itself is still viable while
 * the app stays in the foreground, so it should not be abandoned because the service was refused.
 */
@Singleton
class SideloadForegroundSessionImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SideloadForegroundSession {

    override fun start() {
        runCatching { SideloadService.start(context) }
            .onFailure { Log.e(TAG, "Could not start the sideload service", it) }
    }

    override fun stop() {
        runCatching { SideloadService.stop(context) }
            .onFailure { Log.e(TAG, "Could not stop the sideload service", it) }
    }

    private companion object {
        const val TAG = "SideloadForegroundSession"
    }
}
