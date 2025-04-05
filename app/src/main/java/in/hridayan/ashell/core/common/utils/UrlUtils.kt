package `in`.hridayan.ashell.core.common.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

class UrlUtils {
    companion object {
        fun openUrl(url: String, context: Context) {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        }
    }
}