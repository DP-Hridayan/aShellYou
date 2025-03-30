@file:Suppress("DEPRECATION")

package `in`.hridayan.ashell.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.ui.ToastUtils
import `in`.hridayan.ashell.utils.DocumentTreeUtil.getFullPathFromTreeUri
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object Utils {
    /* <--------FILE ACTIONS -------> */ // Generate the filename
    @JvmStatic
    fun generateFileName(mHistory: List<String>?): String {
        return mHistory!![mHistory.size - 1].replace("/", "-").replace(" ", "")
    }

    // Checks if filename is valid
    @JvmStatic
    fun isValidFilename(s: String): Boolean {
        val invalidChars = arrayOf("*", "/", ":", "<", ">", "?", "\\", "|")

        for (invalidChar in invalidChars) {
            if (s.contains(invalidChar)) return false
        }
        return true
    }

    // Reads a file
    @JvmStatic
    fun read(file: File): String? {
        var buf: BufferedReader? = null
        try {
            buf = BufferedReader(FileReader(file))

            val stringBuilder = StringBuilder()
            var line: String?
            while ((buf.readLine().also { line = it }) != null) {
                stringBuilder.append(line).append("\n")
            }
            return stringBuilder.toString().trim { it <= ' ' }
        } catch (ignored: IOException) {
        } finally {
            try {
                buf?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    // Create a file at the path
    @JvmStatic
    fun create(text: String?, path: File?) {
        try {
            val writer = FileWriter(path)
            writer.write(text)
            writer.close()
        } catch (ignored: IOException) {
        }
    }

    // Logic behind saving output as txt files
    @JvmStatic
    fun saveToFile(sb: String?, activity: Activity, fileName: String?): Boolean {
        val treeUri = Preferences.getSavedOutputDir()
        if (treeUri.isNotEmpty()) {
            return sb?.let {
                fileName?.let { it1 ->
                    saveToCustomDirectory(
                        it, activity,
                        it1, treeUri
                    )
                }
            } == true
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) sb?.let {
            saveToFileApi29AndAbove(
                it,
                activity,
                fileName
            )
        } == true
        else sb?.let { fileName?.let { it1 -> saveToFileBelowApi29(it, activity, it1) } } == true
    }

    private fun saveToCustomDirectory(
        sb: String, activity: Activity, fileName: String, treeUri: String?
    ): Boolean {
        val uri = treeUri?.toUri()

        val documentFile = uri?.let { DocumentFile.fromTreeUri(activity, it) }

        if (documentFile == null) {
            Log.w("Utils", "DocumentFile is null")
            return false
        }
        val file = documentFile.createFile("text/plain", fileName)

        if (file == null) {
            Log.w("Utils", "File is null")
            return false
        }
        val fileUri = file.uri
        try {
            activity.contentResolver.openOutputStream(fileUri).use { outputStream ->
                outputStream!!.write(sb.toByteArray())
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /* Save output txt file on devices running Android 11 and above and return a boolean if the file is saved */
    private fun saveToFileApi29AndAbove(
        sb: String,
        activity: Activity,
        fileName: String?
    ): Boolean {
        try {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)

            val uri =
                activity.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)

            if (uri != null) {
                try {
                    activity.contentResolver.openOutputStream(uri).use { outputStream ->
                        outputStream!!.write(sb.toByteArray())
                        return true
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /*Save output txt file on devices running Android 10 and below and return a boolean if the file is saved */
    private fun saveToFileBelowApi29(sb: String, activity: Activity, fileName: String): Boolean {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0
            )
            return false
        }

        try {
            val file = File(Environment.DIRECTORY_DOWNLOADS, fileName)
            create(sb, file)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    // Method for sharing output to other apps
    @JvmStatic
    fun shareOutput(activity: Activity, context: Context, fileName: String, sb: String) {
        try {
            val file = File(activity.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            outputStream.write(sb.toByteArray())
            outputStream.close()

            val fileUri =
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            activity.startActivity(Intent.createChooser(shareIntent, "Share File"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Method to open the text file
    @JvmStatic
    fun openTextFileWithIntent(fileName: String, context: Context) {
        val savedOutputDir = Preferences.getSavedOutputDir()
        var file: File? = null

        // Check if a custom directory is set in preferences
        if (savedOutputDir.isNotEmpty()) {
            val customDirPath =
                getFullPathFromTreeUri(savedOutputDir.toUri(), context)
            if (customDirPath != null) {
                println(customDirPath)
                file = File(customDirPath, fileName)
            }
        }

        if (file == null || !file.exists()) {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            file = File(downloadsDir, fileName)
        }

        if (file.exists()) {
            val fileUri =
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, "text/plain")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    context, context.getString(R.string.no_application_found), Toast.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.file_not_found), Toast.LENGTH_SHORT)
                .show()
        }
    }

    /* <--------CLIPBOARD ACTIONS -------> */ // Copy the text on the clipboard
    @JvmStatic
    fun copyToClipboard(text: String?, context: Context) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.copied_to_clipboard), text)
        clipboard.setPrimaryClip(clip)
        ToastUtils.showToast(context, R.string.copied_to_clipboard, ToastUtils.LENGTH_SHORT)
    }

    // Paste text from clipboard
    @JvmStatic
    fun pasteFromClipboard(editText: TextInputEditText?) {
        if (editText == null) return

        val clipboard =
            editText.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (clipboard.hasPrimaryClip()
            && clipboard.primaryClipDescription!!.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
        ) {
            val item = clipboard.primaryClip!!.getItemAt(0)
            if (item != null && item.text != null) {
                val clipboardText = item.text.toString()
                editText.setText(clipboardText)
                editText.setSelection(editText.text!!.length)
            }
        } else {
            ToastUtils.showToast(
                editText.context.applicationContext,
                R.string.clipboard_empty,
                ToastUtils.LENGTH_SHORT
            )
        }
    }

    @JvmStatic
    fun getDrawable(drawable: Int, context: Context): Drawable? {
        return ContextCompat.getDrawable(context, drawable)
    }

    @JvmStatic
    fun snackBar(view: View?, message: String): Snackbar? {
        val snackbar = view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG) }
        snackbar?.setAction(
            R.string.dismiss
        ) { snackbar.dismiss() }
        return snackbar
    }

    @JvmStatic
    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(url.toUri())
            context.startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
        }
    }

    // returns if the app toolbar is expanded
    fun isToolbarExpanded(appBarLayout: AppBarLayout): Boolean {
        return appBarLayout.top == 0
    }

    @JvmStatic
    fun convertDpToPixel(dp: Float, context: Context): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    /* Generate the file name of the exported txt file . The name will be the last executed command. It gets the last executed command from the History List */
    @JvmStatic
    fun lastCommandOutput(text: String): String {
        val lastDollarIndex = text.lastIndexOf('$')
        require(lastDollarIndex != -1) { "Text must contain at least one '$' symbol" }

        val secondLastDollarIndex = text.lastIndexOf('$', lastDollarIndex - 1)
        require(secondLastDollarIndex != -1) { "Text must contain at least two '$' symbols" }

        // Find the start of the line containing the first '$' of the last two
        val startOfFirstLine = text.lastIndexOf('\n', secondLastDollarIndex) + 1
        // Find the start of the line containing the second '$' of the last two
        var startOfSecondLine = text.lastIndexOf('\n', lastDollarIndex - 1) + 1
        if (startOfSecondLine == -1) startOfSecondLine =
            0 // If there's no newline before, start from the beginning of the text


        return text.substring(startOfFirstLine, startOfSecondLine)
    }

    // Method to load the changelogs text
    @SuppressLint("DiscouragedApi")
    @JvmStatic
    fun loadChangelogText(versionNumber: String, context: Context): String {
        val resourceId =
            context
                .resources
                .getIdentifier(
                    "changelog_" + versionNumber.replace(".", "_"), "string", context.packageName
                )

        // Check if the resource ID is valid
        if (resourceId != 0) {
            val changeLog = context.getString(resourceId)
            // Check if the resource is not empty
            if (changeLog.isNotEmpty()) return changeLog
        }

        // Return default message if resource ID is invalid or the resource is empty
        return context.getString(R.string.no_changelog)
    }

    // Method to convert List to String (for shizuku shell output)
    @JvmStatic
    fun convertListToString(list: List<String>): String {
        val sb = StringBuilder()
        for (s in list) {
            if (shellDeadError() != s && "<i></i>" != s) sb.append(s).append("\n")
        }
        return sb.toString()
    }

    // String that shows Shell is dead in red colour
    @JvmStatic
    fun shellDeadError(): String {
        return "<font color=#FF0000>" + "Shell is dead" + "</font>"
    }

    // start animation of animated drawable
    @JvmStatic
    fun startAnim(icon: Drawable?) {
        if (icon is AnimatedVectorDrawable) {
            icon.stop()
            icon.start()
        }
    }

    // checks if device is connected to wifi
    fun isConnectedToWifi(context: Context): Boolean {
        val wifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiInfo = wifiManager.connectionInfo

        // Check if connected to a WiFi network (even if no internet)
        if (wifiInfo != null && wifiInfo.networkId != -1) {
            return true
        }

        // Extra check for Android 10+ to detect WiFi when mobile data is on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = connectivityManager.activeNetwork ?: return false

            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            }
        }

        return false
    }

    fun askUserToEnableWifi(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+): Show system WiFi enable popup
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
            context.startActivity(panelIntent)
        } else {
            // Android 9 and below: Enable WiFi directly (Needs CHANGE_WIFI_STATE permission)
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                wifiManager.setWifiEnabled(true)
            }
        }
    }
}
