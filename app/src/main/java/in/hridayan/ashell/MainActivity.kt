package `in`.hridayan.ashell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.core.presentation.ui.theme.AShellYouTheme
import `in`.hridayan.ashell.navigation.Navigation

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AShellYouTheme {
                Surface(
                    modifier = Modifier.Companion
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Navigation()
                }
            }
        }
    }
}
