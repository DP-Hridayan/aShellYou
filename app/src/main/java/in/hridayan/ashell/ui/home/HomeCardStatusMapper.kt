package `in`.hridayan.ashell.ui.home

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.core.common.domain.model.FastbootState
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.UNKNOWN_DEVICE
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.home.presentation.model.DeviceLinkStatus
import `in`.hridayan.ashell.home.presentation.model.WifiAdbCardStatus

internal fun OtgState.toLinkStatus(): DeviceLinkStatus = when (this) {
    is OtgState.Connected -> DeviceLinkStatus.Connected(deviceName)
    OtgState.Searching, OtgState.Connecting, is OtgState.DeviceFound -> DeviceLinkStatus.Connecting
    else -> DeviceLinkStatus.Idle
}

internal fun FastbootState.toLinkStatus(): DeviceLinkStatus = when (this) {
    is FastbootState.Connected -> DeviceLinkStatus.Connected(deviceName)
    FastbootState.Searching, FastbootState.Connecting, is FastbootState.DeviceFound ->
        DeviceLinkStatus.Connecting

    else -> DeviceLinkStatus.Idle
}

internal fun SideloadState.toLinkStatus(): DeviceLinkStatus = when (this) {
    is SideloadState.Connected -> DeviceLinkStatus.Connected(deviceName)
    SideloadState.Searching, SideloadState.Connecting, is SideloadState.DeviceFound ->
        DeviceLinkStatus.Connecting

    else -> DeviceLinkStatus.Idle
}

internal fun toWifiAdbCardStatus(state: WifiAdbState, device: WifiAdbDevice?): WifiAdbCardStatus =
    when (state) {
        is WifiAdbState.Connected -> WifiAdbCardStatus(
            link = DeviceLinkStatus.Connected(device?.deviceName ?: UNKNOWN_DEVICE),
            isOwnDevice = device?.isOwnDevice ?: false
        )

        is WifiAdbState.Connecting, is WifiAdbState.Reconnecting ->
            WifiAdbCardStatus(link = DeviceLinkStatus.Connecting)

        else -> WifiAdbCardStatus()
    }
