@file:Suppress("DEPRECATION")

package `in`.hridayan.ashell.core.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.domain.model.SharedTextHolder
import `in`.hridayan.ashell.core.presentation.utils.ToastUtils

fun openUrl(url: String, context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())

        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    } catch (ignored: ActivityNotFoundException) {
    }
}

/**
 * Check if an app is installed by package name.
 */
fun Context.isAppInstalled(packageName: String): Boolean {
    return packageManager.getLaunchIntentForPackage(packageName) != null
}

fun Context.launchApp(packageName: String) {
    val pm = this.packageManager
    val launchIntent = pm.getLaunchIntentForPackage(packageName)

    if (launchIntent != null) {
        startActivity(launchIntent)
    } else {
        showToast(this, this.getString(R.string.shizuku_not_installed))
        // Optionally redirect to Play Store or Github
        // val playStoreIntent = Intent(
        //     Intent.ACTION_VIEW,
        //     Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        // )
        // activity.startActivity(playStoreIntent)
    }
}

fun openDeveloperOptions(
    context: Context,
    intent: Intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        showToast(context, context.getString(R.string.developer_options_not_available))
    }
}


fun showToast(context: Context, message: String) {
    ToastUtils.makeToast(context, message)
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun handleSharedText(intent: Intent) {
    if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!sharedText.isNullOrBlank()) {
            var cleanedText = sharedText.trim()

            if (cleanedText.startsWith("\"") && cleanedText.endsWith("\"") && cleanedText.length > 1) {
                cleanedText = cleanedText.substring(1, cleanedText.length - 1)
            }

            cleanedText = cleanedText.trim()
            SharedTextHolder.text = cleanedText
        }
    }
}

/**
 * Splits strings into lines by each \n
 */
fun splitStringToLines(input: String): List<String> {
    return input.split("\n").filter { it.isNotBlank() }
}



