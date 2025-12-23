package `in`.hridayan.ashell

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process.killProcess
import android.os.Process.myPid
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import `in`.hridayan.ashell.activities.CrashReportActivity
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import io.github.muntashirakon.adb.PRNGFixes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        contextReference = WeakReference(applicationContext)
        PRNGFixes.apply()

        val crashRepo = EntryPointAccessors.fromApplication(
            this,
            AppEntryPoint::class.java
        ).crashRepository()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException( throwable, crashRepo)
        }
    }

    private fun handleUncaughtException(
        throwable: Throwable,
        crashRepo: CrashRepository
    ) {
        val timestamp = System.currentTimeMillis()
        val deviceBrand = Build.BRAND ?: "Unknown"
        val deviceModel = Build.MODEL
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val osVersion = Build.VERSION.RELEASE ?: "Unknown"
        val socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MANUFACTURER
        } else {
            "Unknown"
        }

        val cpuAbi = Build.SUPPORTED_ABIS.joinToString()
        val appPackageName = BuildConfig.APPLICATION_ID
        val appVersionName = BuildConfig.VERSION_NAME
        val appVersionCode = BuildConfig.VERSION_CODE.toString()
        val stackTrace = throwable.stackTraceToString()

        val crashReport = CrashReport(
            timestamp = timestamp,
            deviceBrand = deviceBrand,
            deviceModel = deviceModel,
            manufacturer = manufacturer,
            osVersion = osVersion,
            socManufacturer = socManufacturer,
            cpuAbi = cpuAbi,
            appPackageName = appPackageName,
            appVersionName = appVersionName,
            appVersionCode = appVersionCode,
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

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        HiddenApiBypass.addHiddenApiExemptions("L")
    }


    companion object {
        private lateinit var instance: App
        private lateinit var contextReference: WeakReference<Context>

        val appContext: Context
            get() {
                if (!this::contextReference.isInitialized || contextReference.get() == null) {
                    contextReference = WeakReference(
                        getInstance().applicationContext
                    )
                }
                return contextReference.get()!!
            }

        private fun getInstance(): App {
            if (!this::instance.isInitialized) {
                instance = App()
            }
            return instance
        }
    }
}
