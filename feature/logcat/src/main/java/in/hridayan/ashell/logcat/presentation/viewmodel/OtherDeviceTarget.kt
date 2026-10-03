package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.logcat.data.emitter.LogcatEmitterFactory
import `in`.hridayan.ashell.logcat.data.packages.PackageUidResolvers
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.repository.PackageUidResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The external device the Other Device tab streams from, with a package resolver created for this
 * connection alone, so a newly connected device never sees the previous device's packages.
 */
internal data class OtherDeviceTarget(
    val emitter: LogcatEmitter,
    val packages: PackageUidResolver,
)

private enum class Transport { OTG, WIFI_ADB }

/** [deviceId] tells apart two devices on the same transport. */
private data class TargetKey(val transport: Transport, val deviceId: String)

/**
 * The device the Other Device tab should stream from, or null when none is connected. OTG wins
 * when both transports are connected. The user's own phone connected over Wireless Debugging is
 * not an other device. A new target is produced only when the device actually changes.
 */
internal fun otherDeviceTargets(
    emitters: LogcatEmitterFactory,
    resolvers: PackageUidResolvers,
): Flow<OtherDeviceTarget?> =
    combine(
        OtgConnection.state,
        WifiAdbConnection.state,
        WifiAdbConnection.currentDevice,
    ) { otg, wifi, device -> keyOf(otg, wifi, device) }
        .distinctUntilChanged()
        .map { key -> key?.let { targetFor(it, emitters, resolvers) } }

private fun keyOf(otg: OtgState, wifi: WifiAdbState, device: WifiAdbDevice?): TargetKey? = when {
    otg is OtgState.Connected -> TargetKey(Transport.OTG, otg.deviceName)
    wifi is WifiAdbState.Connected && device?.isOwnDevice == false ->
        TargetKey(Transport.WIFI_ADB, device.id)

    else -> null
}

private fun targetFor(
    key: TargetKey,
    emitters: LogcatEmitterFactory,
    resolvers: PackageUidResolvers,
): OtherDeviceTarget = when (key.transport) {
    Transport.OTG -> OtherDeviceTarget(emitters.otg, resolvers.forOtg())
    Transport.WIFI_ADB -> OtherDeviceTarget(emitters.wifiAdb, resolvers.forWifiAdb())
}
