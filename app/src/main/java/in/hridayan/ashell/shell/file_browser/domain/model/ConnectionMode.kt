package `in`.hridayan.ashell.shell.file_browser.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class ConnectionMode {
    WIFI_ADB,
    OTG_ADB
}
