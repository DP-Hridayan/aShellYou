package `in`.hridayan.ashell.shell.local_adb_shell.data.shizuku

import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import rikka.shizuku.Shizuku

class ShizukuPermissionHandler {

    private val _permissionGranted: MutableStateFlow<Boolean> =
        MutableStateFlow(getInitialPermissionState())
    val permissionGranted: StateFlow<Boolean> get() = _permissionGranted

    private var permissionListener: Shizuku.OnRequestPermissionResultListener

    init {
        permissionListener = Shizuku.OnRequestPermissionResultListener { _, result ->
            val granted = result == PackageManager.PERMISSION_GRANTED
            _permissionGranted.value = granted
            Shizuku.removeRequestPermissionResultListener(permissionListener)
        }
    }

    private fun getInitialPermissionState(): Boolean {
        return Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    fun hasPermission(): Boolean = getInitialPermissionState()

    fun requestPermission() {
        if (!Shizuku.pingBinder()) return
        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.requestPermission(0)
    }

    fun refreshPermissionState() {
        _permissionGranted.value = getInitialPermissionState()
    }
}