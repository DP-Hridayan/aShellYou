package `in`.hridayan.ashell.utils.app

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import `in`.hridayan.ashell.activities.CrashReportActivity
import kotlin.system.exitProcess

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val stackTrace = Log.getStackTraceString(throwable)
        val intent = Intent(context, CrashReportActivity::class.java)
        intent.putExtra("stackTrace", stackTrace)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)

        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
}
