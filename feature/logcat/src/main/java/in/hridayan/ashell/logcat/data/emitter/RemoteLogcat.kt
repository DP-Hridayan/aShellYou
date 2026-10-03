package `in`.hridayan.ashell.logcat.data.emitter

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceShell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

private const val SDK_PROBE = "getprop ro.build.version.sdk"

/**
 * The first Android release whose logcat is taken to accept `-v uid`. Older logcats reject an
 * unknown format, which would end the stream, so they are given plain threadtime output.
 */
private const val UID_MODIFIER_MIN_SDK = 26

/**
 * Streams logcat from an external device, asking for each line's UID when the device's logcat
 * supports it. On older devices lines carry no UID, so package filters match nothing there.
 */
internal fun ExternalDeviceShell.logcatLines(since: String?): Flow<String> = flow {
    emitAll(execute(LogcatCommand.shellLine(since, withUid = supportsUidColumn())))
}

private suspend fun ExternalDeviceShell.supportsUidColumn(): Boolean {
    val sdk = execute(SDK_PROBE).firstOrNull { it.isNotBlank() }?.trim()?.toIntOrNull()
    return sdk != null && sdk >= UID_MODIFIER_MIN_SDK
}
