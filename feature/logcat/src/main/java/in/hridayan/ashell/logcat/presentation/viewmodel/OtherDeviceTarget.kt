package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.logcat.data.emitter.LogcatEmitterFactory
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * The external device the Other Device tab streams from. [deviceId] tells apart two devices on
 * the same transport, so switching devices starts a new session instead of resuming the old one.
 */
internal data class OtherDeviceTarget(
    val emitter: LogcatEmitter,
    val deviceId: String,
)

/**
 * The device the Other Device tab should stream from, or null when none is connected. OTG wins
 * when both transports are connected. The user's own phone connected over Wireless Debugging is
 * not an other device.
 */
internal fun otherDeviceTargets(emitters: LogcatEmitterFactory): Flow<OtherDeviceTarget?> =
    combine(
        OtgConnection.state,
        WifiAdbConnection.state,
        WifiAdbConnection.currentDevice,
    ) { otg, wifi, device -> targetOf(otg, wifi, device, emitters) }
        .distinctUntilChanged()

private fun targetOf(
    otg: OtgState,
    wifi: WifiAdbState,
    device: WifiAdbDevice?,
    emitters: LogcatEmitterFactory,
): OtherDeviceTarget? = when {
    otg is OtgState.Connected -> OtherDeviceTarget(emitters.otg, otg.deviceName)
    wifi is WifiAdbState.Connected && device?.isOwnDevice == false ->
        OtherDeviceTarget(emitters.wifiAdb, device.id)

    else -> null
}
