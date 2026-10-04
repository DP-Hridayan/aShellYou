package `in`.hridayan.ashell.mirror.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.ViewerProfile
import kotlinx.coroutines.flow.Flow

/** The video quality choices kept between sessions, and what this phone can show. */
interface QualityRepository {

    fun selection(transport: ExternalDeviceTransport): Flow<QualitySelection>

    suspend fun saveSelection(transport: ExternalDeviceTransport, selection: QualitySelection)

    val isStatsOverlayOn: Flow<Boolean>

    suspend fun setStatsOverlayOn(isOn: Boolean)

    /** The long edge a device's encoder last fell back to, kept until the app process ends. */
    fun encoderLimit(serial: String): Int?

    fun rememberEncoderLimit(serial: String, longEdge: Int)

    /** Read once and kept, because the screen and decoders don't change while the app runs. */
    suspend fun viewerProfile(): ViewerProfile
}
