package `in`.hridayan.ashell.core.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("_yyyyMMddHHmmss")

    fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(formatter)
    }

    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes mins ago"
            hours < 24 -> "$hours hours ago"
            days < 7 -> "$days days ago"
            else -> "Long ago"
        }
    }
}
