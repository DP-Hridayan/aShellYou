package `in`.hridayan.ashell.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtils {

    fun copyToClipboard(text: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    fun readFromClipboard(context: Context): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        return if (clipData != null && clipData.itemCount > 0) {
            clipData.getItemAt(0).text?.toString()
        } else {
            null
        }
    }
}
