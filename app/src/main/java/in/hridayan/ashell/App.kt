package `in`.hridayan.ashell

import android.app.Application
import android.content.Intent
import android.os.Process.killProcess
import android.os.Process.myPid
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import `in`.hridayan.ashell.activities.CrashReportActivity
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val crashRepo = EntryPointAccessors.fromApplication(
            this,
            AppEntryPoint::class.java
        ).crashRepository()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread, throwable, crashRepo)
        }
    }

    private fun handleUncaughtException(
        thread: Thread,
        throwable: Throwable,
        crashRepo: CrashRepository
    ) {
        val timestamp = System.currentTimeMillis()
        val deviceName = android.os.Build.MODEL ?: "Unknown"
        val manufacturer = android.os.Build.MANUFACTURER ?: "Unknown"
        val osVersion = android.os.Build.VERSION.RELEASE ?: "Unknown"
        val stackTrace = throwable.stackTraceToString()

        val crashReport = CrashReport(
            timestamp = timestamp,
            deviceName = deviceName,
            manufacturer = manufacturer,
            osVersion = osVersion,
            stackTrace = stackTrace
        )

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            crashRepo.addCrash(crashReport)

            val intent = Intent(this@App, CrashReportActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("CRASH_TIMESTAMP", timestamp)
            this@App.startActivity(intent)
        }

        Thread.sleep(500)
        killProcess(myPid())
        exitProcess(2)
    }
}
