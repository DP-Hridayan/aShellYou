package `in`.hridayan.ashell.ui

import android.content.Context
import android.widget.Toast

object ToastUtils {
    private var toast: Toast? = null
    @JvmField
    var LENGTH_SHORT: Int = 0
    @JvmField
    var LENGTH_LONG: Int = 1

    @JvmStatic
    fun showToast(context: Context?, message: String?, length: Int) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast.makeText(context, message, length)
        toast?.show()
    }

    @JvmStatic
    fun showToast(context: Context, resId: Int, length: Int) {
        showToast(context, context.getString(resId), length)
    }
}
