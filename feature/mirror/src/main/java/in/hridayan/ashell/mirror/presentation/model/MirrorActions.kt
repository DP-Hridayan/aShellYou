package `in`.hridayan.ashell.mirror.presentation.model

import android.view.Surface
import androidx.compose.runtime.Stable
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.DeviceKey

@Stable
interface MirrorActions {
    fun onSurfaceAvailable(surface: Surface)
    fun onSurfaceDestroyed()
    fun onTouch(messages: List<ControlMessage>)
    fun onDeviceKey(key: DeviceKey)
    fun onExpandQuickSettings()
    fun onRetry()
}
