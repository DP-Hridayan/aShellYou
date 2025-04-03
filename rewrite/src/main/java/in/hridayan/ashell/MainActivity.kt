package `in`.hridayan.ashell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.presentation.navigation.Navigation
import `in`.hridayan.ashell.presentation.ui.screens.CommandExamplesScreen
import `in`.hridayan.ashell.presentation.ui.screens.HomeScreen
import `in`.hridayan.ashell.presentation.ui.theme.AShellYouTheme

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
