package `in`.hridayan.ashell.core.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("_yyyyMMddHHmmss")

    fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(formatter)
    }
}
