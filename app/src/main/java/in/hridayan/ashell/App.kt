package `in`.hridayan.ashell

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import `in`.hridayan.ashell.activities.CrashReportActivity
import `in`.hridayan.ashell.core.common.FeatureConfig
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import io.github.muntashirakon.adb.PRNGFixes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this
        contextReference = WeakReference(applicationContext)

        FeatureConfig.isAiEnabled = BuildConfig.AI_FEATURES_ENABLED

        PRNGFixes.apply()

        val entryPoint = EntryPointAccessors.fromApplication(
            this,
            AppEntryPoint::class.java
        )
        val crashRepo = entryPoint.crashRepository()
        val tileComponentManager = entryPoint.tileComponentManager()

        tileComponentManager.ensureAllEnabled()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(throwable, crashRepo)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun handleUncaughtException(
        throwable: Throwable,
        crashRepo: CrashRepository
    ) {
        val crashReport = buildCrashReport(throwable)
        saveAndLaunchCrashActivity(crashReport, crashRepo)
        Thread.sleep(CRASH_DELAY_MS)
    }

    private fun buildCrashReport(throwable: Throwable): CrashReport {
        val socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MANUFACTURER
        } else {
            "Unknown"
        }

        return CrashReport(
            timestamp = System.currentTimeMillis(),
            deviceBrand = Build.BRAND ?: "Unknown",
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER ?: "Unknown",
            osVersion = Build.VERSION.RELEASE ?: "Unknown",
            socManufacturer = socManufacturer,
            cpuAbi = Build.SUPPORTED_ABIS.joinToString(),
            appPackageName = BuildConfig.APPLICATION_ID,
            appVersionName = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE.toString(),
            stackTrace = throwable.stackTraceToString()
        )
    }

    private fun saveAndLaunchCrashActivity(crashReport: CrashReport, crashRepo: CrashRepository) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            crashRepo.addCrash(crashReport)

            val intent = Intent(this@App, CrashReportActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("CRASH_TIMESTAMP", crashReport.timestamp)
            }
            this@App.startActivity(intent)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        HiddenApiBypass.addHiddenApiExemptions("L")
    }

    companion object {
        private const val CRASH_DELAY_MS = 500L
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
