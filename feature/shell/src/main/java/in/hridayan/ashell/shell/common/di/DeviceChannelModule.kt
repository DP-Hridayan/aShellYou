package `in`.hridayan.ashell.shell.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannelNames
import `in`.hridayan.ashell.shell.common.data.channel.OtgDeviceChannel
import `in`.hridayan.ashell.shell.common.data.channel.WifiAdbDeviceChannel
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceChannelModule {

    @Binds
    @Named(ExternalDeviceChannelNames.OTG)
    abstract fun bindOtgDeviceChannel(impl: OtgDeviceChannel): ExternalDeviceChannel

    @Binds
    @Named(ExternalDeviceChannelNames.WIFI_ADB)
    abstract fun bindWifiAdbDeviceChannel(impl: WifiAdbDeviceChannel): ExternalDeviceChannel
}
