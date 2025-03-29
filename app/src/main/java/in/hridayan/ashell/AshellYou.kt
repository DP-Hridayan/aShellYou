package `in`.hridayan.ashell

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import `in`.hridayan.ashell.config.Preferences
import java.lang.ref.WeakReference

class AshellYou : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        contextReference = WeakReference(
            applicationContext
        )

        Preferences.init()

        AppCompatDelegate.setDefaultNightMode(Preferences.getThemeMode())
    }

    companion object {
        private var instance: AshellYou? = null
        private var contextReference: WeakReference<Context?>? = null

        @JvmStatic
        val appContext: Context?
            get() {
                if (contextReference == null || contextReference!!.get() == null) {
                    contextReference =
                        WeakReference(
                            getInstance().applicationContext
                        )
                }
                return contextReference!!.get()
            }

        private fun getInstance(): AshellYou {
            if (instance == null) {
                instance = AshellYou()
            }
            return instance!!
        }
    }
}
