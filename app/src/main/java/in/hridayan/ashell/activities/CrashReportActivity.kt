package `in`.hridayan.ashell.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.core.common.CompositionLocals
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.SeedColorProvider
import `in`.hridayan.ashell.core.presentation.ui.theme.AshellYouTheme
import `in`.hridayan.ashell.crashreporter.presentation.screens.CrashReportScreen

@AndroidEntryPoint
class CrashReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CompositionLocals {
                SeedColorProvider.seedColor = LocalSeedColor.current

                AshellYouTheme {
                    Surface(
                        modifier = Modifier.Companion.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) { CrashReportScreen() }
                }
            }
        }
    }
}