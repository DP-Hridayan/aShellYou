package `in`.hridayan.ashell.core.utils

import android.content.Context
import android.content.Intent
import android.os.Process

object AppRestartUtils {

    /**
     * Relaunches the launcher activity in a fresh task and kills the current
     * process so a new one is forked with the current permissions and groups.
     */
    fun restart(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launchIntent)
        Process.killProcess(Process.myPid())
    }
}
