package `in`.hridayan.ashell.logcat.data.session

import `in`.hridayan.ashell.logcat.data.session.LogcatDeeplinkHolder.ACTION_OPEN_LOGCAT
import `in`.hridayan.ashell.logcat.service.LogcatNotificationHelper

/**
 * Thin holder for the deeplink action string constant.
 * The actual reactive navigation event is fired via [LogcatSessionHolder.triggerLogcatNavigation].
 *
 * Keeping this object so [LogcatNotificationHelper] and MainActivity can reference
 * [ACTION_OPEN_LOGCAT] without importing the full holder.
 */
object LogcatDeeplinkHolder {
    const val ACTION_OPEN_LOGCAT = "in.hridayan.ashell.ACTION_OPEN_LOGCAT"
}
