package `in`.hridayan.ashell.core.common.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * The ADB link that reaches another device: a USB OTG cable, or Wireless Debugging to a device other
 * than this one.
 */
@Keep
@Serializable
enum class ExternalDeviceTransport {
    OTG,
    WIFI_ADB
}
