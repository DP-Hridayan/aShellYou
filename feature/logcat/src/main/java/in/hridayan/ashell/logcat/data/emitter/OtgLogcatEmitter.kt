package `in`.hridayan.ashell.logcat.data.emitter

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceShell
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Emitter that streams logcat from a device connected via OTG ADB. */
@Singleton
class OtgLogcatEmitter @Inject constructor(
    @Named("otg") private val shell: ExternalDeviceShell,
) : LogcatEmitter {

    override fun lines(): Flow<String> =
        shell.execute("logcat -v threadtime").flowOn(Dispatchers.IO)

    override fun isAvailable(): Boolean = shell.isConnected
}
