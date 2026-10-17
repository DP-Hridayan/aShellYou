package `in`.hridayan.ashell.logcat.domain.usecase

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val FILE_NAME_PREFIX = "aShellYou_logcat_"
private const val FILE_EXTENSION = ".txt"
private const val TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss"

/** Name for an exported logcat file, unique to the second of [now]. */
class BuildLogExportFileNameUseCase @Inject constructor() {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN)

    operator fun invoke(now: LocalDateTime = LocalDateTime.now()): String =
        FILE_NAME_PREFIX + now.format(formatter) + FILE_EXTENSION
}
