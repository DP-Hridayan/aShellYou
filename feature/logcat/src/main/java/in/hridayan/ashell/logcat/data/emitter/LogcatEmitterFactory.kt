package `in`.hridayan.ashell.logcat.data.emitter

import `in`.hridayan.ashell.core.common.domain.model.LogcatWorkingMode
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Selects the correct [LogcatEmitter] based on [LogcatWorkingMode].
 *
 * - [forMode] picks the "This Device" emitter.
 * - [otg] / [wifiAdb] are used by the "Other Device" tab.
 */
@Singleton
class LogcatEmitterFactory @Inject constructor(
    val basic: BasicLogcatEmitter,
    val shizuku: ShizukuLogcatEmitter,
    val root: RootLogcatEmitter,
    val wireless: WifiAdbOwnLogcatEmitter,
    val otg: OtgLogcatEmitter,
    val wifiAdb: WifiAdbLogcatEmitter,
) {
    fun forMode(mode: Int): LogcatEmitter = when (mode) {
        LogcatWorkingMode.SHIZUKU  -> shizuku
        LogcatWorkingMode.ROOT     -> root
        LogcatWorkingMode.WIRELESS -> wireless
        else                       -> basic
    }
}
