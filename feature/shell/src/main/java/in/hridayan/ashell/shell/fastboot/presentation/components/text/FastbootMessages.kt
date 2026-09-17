package `in`.hridayan.ashell.shell.fastboot.presentation.components.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootError
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import java.util.Locale

@Composable
fun fastbootErrorText(error: FastbootError, detail: String? = null): String {
    val base = stringResource(errorStringRes(error))
    return if (detail.isNullOrBlank()) base else "$base ($detail)"
}

/**
 * @param completedText what success means for this operation, which only the caller knows
 */
@Composable
fun flashStatusText(operation: FlashOperation, completedText: String): String = when (operation.status) {
    FlashStatus.IDLE -> ""
    FlashStatus.READING_FILE -> stringResource(R.string.reading_image_file)
    FlashStatus.DOWNLOADING -> downloadingText(operation)
    FlashStatus.WRITING -> stringResource(R.string.writing_to_partition, operation.partition)
    FlashStatus.ERASING -> stringResource(R.string.erasing_partition, operation.partition)
    FlashStatus.CANCELLING -> stringResource(R.string.cancelling)
    FlashStatus.COMPLETE -> completedText
    FlashStatus.CANCELLED -> stringResource(R.string.cancelled)
    FlashStatus.ERROR -> errorText(operation)
}

@Composable
private fun errorText(operation: FlashOperation): String = when (val error = operation.error) {
    FastbootError.IMAGE_TOO_LARGE -> stringResource(R.string.image_too_large, operation.errorDetail.orEmpty())
    null -> fastbootErrorText(FastbootError.UNKNOWN, operation.errorDetail)
    else -> fastbootErrorText(error, operation.errorDetail)
}

@Composable
private fun downloadingText(operation: FlashOperation): String {
    val size = megabytes(operation.totalBytes)
    return if (operation.bytesSent <= 0L) {
        stringResource(R.string.downloading_to_device, size)
    } else {
        stringResource(R.string.sending_progress, (operation.progress * PERCENT).toInt(), size)
    }
}

private fun megabytes(bytes: Long): String =
    String.format(Locale.getDefault(), "%.1f", bytes / BYTES_PER_MEGABYTE)

@StringRes
private fun errorStringRes(error: FastbootError): Int = ERROR_STRINGS[error] ?: R.string.unknown_error

private val ERROR_STRINGS: Map<FastbootError, Int> = mapOf(
    FastbootError.NO_DEVICE_CONNECTED to R.string.no_device_connected,
    FastbootError.FILE_OPEN_FAILED to R.string.file_open_failed,
    FastbootError.FILE_READ_FAILED to R.string.file_read_failed,
    FastbootError.IMAGE_TOO_LARGE to R.string.image_too_large,
    FastbootError.DEVICE_REJECTED to R.string.device_rejected_operation,
    FastbootError.TRANSFER_FAILED to R.string.transfer_to_device_failed,
    FastbootError.CONNECTION_LOST to R.string.connection_lost,
    FastbootError.UNKNOWN to R.string.unknown_error,
)

private const val PERCENT = 100
private const val BYTES_PER_MEGABYTE = 1024.0 * 1024.0
