package `in`.hridayan.ashell.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.crashreporter.presentation.screens.CrashReportScreen

@AndroidEntryPoint
class CrashReportActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CrashReportScreen()
        }
    }
}