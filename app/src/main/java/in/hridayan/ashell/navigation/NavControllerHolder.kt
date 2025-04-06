package `in`.hridayan.ashell.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavHostController

object NavControllerHolder {
    @SuppressLint("StaticFieldLeak")
    var navController: NavHostController? = null
}

inline fun <reified T : Any> navigateTo(destination: T) {
    NavControllerHolder.navController?.navigate(destination)
}