package `in`.hridayan.ashell.mirror.presentation.components.controls

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport

/**
 * What the mirror's top bar shows.
 *
 * @property deviceName null until the device has introduced itself.
 * @property isViewOnly shows a badge when the device refused input, so a touch that does nothing
 * has a visible reason.
 * @property canEnterFullscreen whether the fullscreen button is enabled.
 */
data class MirrorTopBarState(
    val deviceName: String?,
    val transport: ExternalDeviceTransport,
    val isViewOnly: Boolean,
    val canEnterFullscreen: Boolean
)
