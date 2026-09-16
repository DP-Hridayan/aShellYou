package `in`.hridayan.ashell.adbsideload.presentation.components.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.core.resources.R

@Composable
fun sideloadErrorText(error: SideloadError, detail: String? = null): String {
    val base = stringResource(errorStringRes(error))
    return if (detail.isNullOrBlank()) base else "$base ($detail)"
}

@Composable
fun sideloadStatusText(operation: SideloadOperation): String = when (operation.status) {
    SideloadStatus.IDLE -> ""
    SideloadStatus.READING_FILE -> stringResource(R.string.preparing_file)
    SideloadStatus.WAITING_FOR_RECOVERY -> stringResource(R.string.waiting_for_recovery)
    SideloadStatus.SENDING -> stringResource(R.string.sideloading)
    SideloadStatus.COMPLETE -> stringResource(R.string.sideload_complete)
    SideloadStatus.CANCELLED -> stringResource(R.string.cancelled)
    SideloadStatus.ERROR -> sideloadErrorText(operation.error ?: SideloadError.UNKNOWN, operation.errorDetail)
}

@Composable
fun sideloadConnectionText(state: SideloadState): String = when (state) {
    is SideloadState.Idle -> stringResource(R.string.put_device_in_sideload_mode)
    is SideloadState.Searching -> stringResource(R.string.searching_for_devices)
    is SideloadState.PermissionDenied -> stringResource(R.string.permission_denied)
    is SideloadState.Connecting -> stringResource(R.string.connecting)
    is SideloadState.Disconnected -> stringResource(R.string.disconnected)
    is SideloadState.UsbManagerUnavailable -> stringResource(R.string.usb_service_unavailable)
    is SideloadState.Error -> sideloadErrorText(state.error, state.detail)
    is SideloadState.DeviceFound -> stringResource(R.string.allow_usb_access_message)
    is SideloadState.WrongMode -> stringResource(R.string.device_not_in_sideload_mode)
    is SideloadState.Connected -> ""
}

@StringRes
private fun errorStringRes(error: SideloadError): Int = ERROR_STRINGS[error] ?: R.string.unknown_error

private val ERROR_STRINGS: Map<SideloadError, Int> = mapOf(
    SideloadError.NO_DEVICE_CONNECTED to R.string.no_device_connected,
    SideloadError.USB_SERVICE_UNAVAILABLE to R.string.usb_service_unavailable,
    SideloadError.NO_ADB_INTERFACE to R.string.no_adb_interface_found,
    SideloadError.USB_OPEN_FAILED to R.string.usb_open_failed,
    SideloadError.USB_CLAIM_FAILED to R.string.usb_claim_failed,
    SideloadError.ADB_KEY_UNAVAILABLE to R.string.adb_key_unavailable,
    SideloadError.CONNECTION_TIMED_OUT to R.string.sideload_connection_timed_out,
    SideloadError.CONNECTION_FAILED to R.string.connection_failed,
    SideloadError.CONNECTION_LOST to R.string.connection_lost,
    SideloadError.FILE_SIZE_UNKNOWN to R.string.file_size_unknown,
    SideloadError.FILE_OPEN_FAILED to R.string.file_open_failed,
    SideloadError.FILE_READ_FAILED to R.string.file_read_failed,
    SideloadError.RECOVERY_NOT_RESPONDING to R.string.recovery_not_responding,
    SideloadError.STREAM_OPEN_FAILED to R.string.sideload_stream_rejected,
    SideloadError.STREAM_CLOSED to R.string.sideload_stream_closed,
    SideloadError.RECOVERY_REPORTED_FAILURE to R.string.recovery_reported_failure,
    SideloadError.BLOCK_OUT_OF_RANGE to R.string.sideload_block_out_of_range,
    SideloadError.INVALID_REQUEST to R.string.sideload_invalid_request,
    SideloadError.UNKNOWN to R.string.unknown_error,
)
