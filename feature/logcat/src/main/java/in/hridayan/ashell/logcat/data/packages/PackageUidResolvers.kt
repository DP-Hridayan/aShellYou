package `in`.hridayan.ashell.logcat.data.packages

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceShell
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Package resolvers for each device the logcat screen can read from. Remote resolvers cache the
 * connected device's package table, so create one per connection with [forOtg] or [forWifiAdb].
 */
@Singleton
class PackageUidResolvers @Inject constructor(
    val local: LocalPackageUidResolver,
    @param:Named("otg") private val otgShell: ExternalDeviceShell,
    @param:Named("wifiAdb") private val wifiAdbShell: ExternalDeviceShell,
) {
    fun forOtg(): RemotePackageUidResolver = RemotePackageUidResolver(otgShell)

    fun forWifiAdb(): RemotePackageUidResolver = RemotePackageUidResolver(wifiAdbShell)
}
