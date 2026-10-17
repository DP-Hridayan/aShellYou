package `in`.hridayan.ashell.portforward.data.device

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannelNames
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import `in`.hridayan.ashell.portforward.domain.model.reaches
import `in`.hridayan.ashell.portforward.domain.repository.ConnectedDevices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** The ADB channel of each transport, and whether a tunnel's device is the one connected. */
@Singleton
class DeviceChannels @Inject constructor(
    @param:Named(ExternalDeviceChannelNames.OTG) private val otg: ExternalDeviceChannel,
    @param:Named(ExternalDeviceChannelNames.WIFI_ADB) private val wifiAdb: ExternalDeviceChannel
) : ConnectedDevices {

    operator fun get(transport: ExternalDeviceTransport): ExternalDeviceChannel = when (transport) {
        ExternalDeviceTransport.OTG -> otg
        ExternalDeviceTransport.WIFI_ADB -> wifiAdb
    }

    override fun state(transport: ExternalDeviceTransport): StateFlow<ExternalDeviceState> =
        get(transport).connectionState

    /** Emits null while [target]'s device is connected, and [WaitReason.Device] while it is not. */
    fun availability(target: TunnelTarget.Device): Flow<WaitReason?> =
        state(target.transport).map { if (it.reaches(target)) null else WaitReason.Device }

    fun isReachable(target: TunnelTarget.Device): Boolean = state(target.transport).value.reaches(target)
}
