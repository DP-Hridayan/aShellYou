package `in`.hridayan.ashell.utils

import android.content.Context
import android.view.View
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.utils.HapticUtils.weakVibrate
import java.io.File

object BookmarkUtils {

    @JvmStatic
    fun getBookmarks(context: Context): List<String> {
        val bookmarksDir = context.getExternalFilesDir("bookmarks") ?: return emptyList()
        val bookmarks = mutableListOf<String>()

        bookmarksDir.listFiles()?.forEach { file ->
            if (!file.name.equals("specialCommands", ignoreCase = true)) {
                bookmarks.add(file.name)
            }
        }

        val specialCommandsFile = File(bookmarksDir, "specialCommands")
        if (specialCommandsFile.exists()) {
            Utils.read(specialCommandsFile)?.let { fileContent ->
                fileContent.lines().filter { it.isNotBlank() }.forEach { bookmarks.add(it.trim()) }
            }
        }

        when (Preferences.getSortingOption()) {
            Const.SORT_A_TO_Z -> bookmarks.sort()
            Const.SORT_Z_TO_A -> bookmarks.sortDescending()
            Const.SORT_OLDEST -> bookmarks.reverse()
            // SORT_NEWEST (default order, no action needed)
        }

        return bookmarks
    }

    @JvmStatic
    fun isBookmarked(command: String, context: Context): Boolean {
        val bookmarksDir = context.getExternalFilesDir("bookmarks") ?: return false
        return if (Utils.isValidFilename(command)) {
            File(bookmarksDir, command).exists()
        } else {
            val specialCommandsFile = File(bookmarksDir, "specialCommands")
            specialCommandsFile.takeIf { it.exists() }?.let {
                Utils.read(it)?.lines()?.any { line -> line.trim() == command } ?: false
            } ?: false
        }
    }

    @JvmStatic
    fun addToBookmark(command: String, context: Context) {
        val bookmarksDir = context.getExternalFilesDir("bookmarks") ?: return
        if (Utils.isValidFilename(command)) {
            Utils.create(command, File(bookmarksDir, command))
        } else {
            val specialCommandsFile = File(bookmarksDir, "specialCommands")
            val content =
                Utils.read(specialCommandsFile)?.lines()?.filter { it.isNotBlank() }
                    ?.toMutableList()
                    ?: mutableListOf()
            content.add(command)
            Utils.create(content.joinToString("\n"), specialCommandsFile)
        }
    }

    @JvmStatic
    fun deleteFromBookmark(command: String, context: Context): Boolean {
        val bookmarksDir = context.getExternalFilesDir("bookmarks") ?: return false
        return if (Utils.isValidFilename(command)) {
            File(bookmarksDir, command).delete()
        } else {
            val specialCommandsFile = File(bookmarksDir, "specialCommands")
            val newContent =
                Utils.read(specialCommandsFile)?.lines()?.filter { it.trim() != command }
                    ?.joinToString("\n")
            if (newContent != null) {
                Utils.create(newContent, specialCommandsFile)
                true
            } else {
                false
            }
        }
    }

    @JvmStatic
    fun addBookmarkIconOnClickListener(bookmark: String, view: View?, context: Context) {
        weakVibrate(view)
        val switchState = Preferences.getOverrideBookmarks()
        val bookmarkLimitReached =
            getBookmarks(context).size >= Const.MAX_BOOKMARKS_LIMIT && !switchState

        if (bookmarkLimitReached) {
            Utils.snackBar(view, context.getString(R.string.bookmark_limit_reached))?.show()
        } else {
            addToBookmark(bookmark, context)
            Utils.snackBar(view, context.getString(R.string.bookmark_added_message, bookmark))
                ?.show()
        }
    }
}