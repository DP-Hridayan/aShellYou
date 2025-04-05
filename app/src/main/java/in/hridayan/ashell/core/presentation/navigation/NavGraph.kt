package `in`.hridayan.ashell.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `in`.hridayan.ashell.commandexamples.presentation.screens.CommandExamplesScreen
import `in`.hridayan.ashell.home.presentation.screens.HomeScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavControllerHolder.navController = navController

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen()
        }
        composable("command_examples") {
            CommandExamplesScreen()
        }
    }
}