package `in`.hridayan.ashell.mirror.data.quality

import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.ViewerProfile
import `in`.hridayan.ashell.mirror.domain.repository.QualityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps quality choices in the app's settings store, which also puts them in backups. Encoder limits
 * are kept in memory only: they are cheap to learn again and may change with a device update.
 */
@Singleton
class QualityRepositoryImpl @Inject constructor(
    private val settings: SettingsRepository,
    private val viewerProfileReader: ViewerProfileReader,
    private val dispatchers: DispatcherProvider
) : QualityRepository {

    private val encoderLimits = ConcurrentHashMap<String, Int>()
    private val viewerLock = Mutex()
    private var viewer: ViewerProfile? = null

    override fun selection(transport: ExternalDeviceTransport): Flow<QualitySelection> {
        val keys = QualityKeys.of(transport)
        return combine(
            settings.getString(keys.mode),
            settings.getInt(keys.resolution),
            settings.getInt(keys.frameRate),
            settings.getInt(keys.bitrate),
            settings.getString(keys.codec)
        ) { mode, resolution, frameRate, bitrate, codec ->
            QualitySettingsMapper.fromStored(StoredQuality(mode, resolution, frameRate, bitrate, codec))
        }
    }

    override suspend fun saveSelection(transport: ExternalDeviceTransport, selection: QualitySelection) {
        val keys = QualityKeys.of(transport)
        val stored = QualitySettingsMapper.toStored(selection)
        settings.setString(keys.mode, stored.mode)
        settings.setInt(keys.resolution, stored.resolution)
        settings.setInt(keys.frameRate, stored.frameRate)
        settings.setInt(keys.bitrate, stored.bitrate)
        settings.setString(keys.codec, stored.codec)
    }

    override val isStatsOverlayOn: Flow<Boolean> = settings.getBoolean(SettingsKeys.MirrorStatsOverlay)

    override suspend fun setStatsOverlayOn(isOn: Boolean) = settings.setBoolean(SettingsKeys.MirrorStatsOverlay, isOn)

    override fun encoderLimit(serial: String): Int? = encoderLimits[serial]

    override fun rememberEncoderLimit(serial: String, longEdge: Int) {
        encoderLimits[serial] = longEdge
    }

    override suspend fun viewerProfile(): ViewerProfile = viewerLock.withLock {
        viewer ?: withContext(dispatchers.io) { viewerProfileReader.read() }.also { viewer = it }
    }

    /**
     * The settings keys of one transport's quality.
     *
     * Not a data class: generated equality would need the settings library's key supertype on this
     * module's classpath, and nothing compares these.
     */
    @Suppress("UseDataClass")
    private class QualityKeys(
        val mode: SettingsKeys<String>,
        val resolution: SettingsKeys<Int>,
        val frameRate: SettingsKeys<Int>,
        val bitrate: SettingsKeys<Int>,
        val codec: SettingsKeys<String>
    ) {
        companion object {
            private val usb = QualityKeys(
                SettingsKeys.MirrorQualityUsb,
                SettingsKeys.MirrorResolutionUsb,
                SettingsKeys.MirrorFrameRateUsb,
                SettingsKeys.MirrorBitrateUsb,
                SettingsKeys.MirrorCodecUsb
            )
            private val wifi = QualityKeys(
                SettingsKeys.MirrorQualityWifi,
                SettingsKeys.MirrorResolutionWifi,
                SettingsKeys.MirrorFrameRateWifi,
                SettingsKeys.MirrorBitrateWifi,
                SettingsKeys.MirrorCodecWifi
            )

            fun of(transport: ExternalDeviceTransport): QualityKeys = when (transport) {
                ExternalDeviceTransport.OTG -> usb
                ExternalDeviceTransport.WIFI_ADB -> wifi
            }
        }
    }
}
