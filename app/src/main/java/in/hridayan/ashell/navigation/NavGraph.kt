package `in`.hridayan.ashell.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `in`.hridayan.ashell.commandexamples.presentation.screens.CommandExamplesScreen
import `in`.hridayan.ashell.home.presentation.screens.HomeScreen
import `in`.hridayan.ashell.settings.lookandfeel.presentation.screens.LookAndFeel
import kotlinx.serialization.Serializable

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavControllerHolder.navController = navController

    NavHost(
        navController = navController, startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            HomeScreen()
        }
        composable<CommandExamplesScreen> {
            CommandExamplesScreen()
        }
        composable<LookAndFeel>{
            LookAndFeel()
        }
    }
}

@Serializable
object HomeScreen

@Serializable
object CommandExamplesScreen

@Serializable
object LookAndFeel