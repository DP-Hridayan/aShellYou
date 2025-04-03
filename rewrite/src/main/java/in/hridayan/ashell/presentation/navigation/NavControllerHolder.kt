package `in`.hridayan.ashell.presentation.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavController

object NavControllerHolder {
   @SuppressLint("StaticFieldLeak")
   var navController: NavController? = null
}

fun navigateTo(route: String) {
    NavControllerHolder.navController?.navigate(route)
}