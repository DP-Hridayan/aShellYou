package `in`.hridayan.ashell.core.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class AdbFileBrowserConnectionMode {
    WIFI_ADB,
    OTG_ADB
}