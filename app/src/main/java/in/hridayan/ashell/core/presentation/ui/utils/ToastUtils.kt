package `in`.hridayan.ashell.core.presentation.ui.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    private var toast: Toast? = null

    const val LENGTH_SHORT = Toast.LENGTH_SHORT
    const val LENGTH_LONG = Toast.LENGTH_LONG

    fun makeToast(context: Context, message: String, length: Int = LENGTH_SHORT) {
        toast?.cancel()
        toast = Toast.makeText(context.applicationContext, message, length)
        toast?.show()
    }
}