package `in`.hridayan.ashell.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import `in`.hridayan.ashell.core.resources.R

object ClipboardUtils {

    fun copyToClipboard(
        text: String,
        context: Context,
        onSuccess: (() -> Unit) = {
            showToast(context, context.getString(R.string.copied_to_clipboard))
        },
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)

        try {
            clipboard.setPrimaryClip(clip)
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
