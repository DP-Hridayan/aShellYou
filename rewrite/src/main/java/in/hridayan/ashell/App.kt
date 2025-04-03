package `in`.hridayan.ashell

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import `in`.hridayan.ashell.data.local.database.command_examples.CommandDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class App : Application() {
    val database: CommandDatabase by lazy {
        CommandDatabase.getDatabase(this, CoroutineScope(Dispatchers.IO))
    }

    override fun onCreate() {
        super.onCreate()

        database.commandDao()
        Log.d("Database", "Database accessed in App onCreate")
    }
}