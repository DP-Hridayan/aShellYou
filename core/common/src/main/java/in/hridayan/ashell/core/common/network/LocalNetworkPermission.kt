package `in`.hridayan.ashell.core.common.network

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * The runtime permission that Android 17 requires before an app reaches devices on the local
 * network, or accepts connections from them.
 */
object LocalNetworkPermission {

    const val PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

    private const val FIRST_ENFORCING_SDK = 37

    /** True on Android versions that enforce the permission. */
    val isRequired: Boolean
        get() = Build.VERSION.SDK_INT >= FIRST_ENFORCING_SDK

    /** True when local network access is allowed, either granted or not enforced. */
    fun isGranted(context: Context): Boolean = !isRequired || hasPermission(context)

    private fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED
}
