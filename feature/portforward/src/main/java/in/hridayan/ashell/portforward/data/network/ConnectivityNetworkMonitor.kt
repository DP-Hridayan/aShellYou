package `in`.hridayan.ashell.portforward.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.portforward.domain.repository.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Follows the default network for the life of the process. */
@Singleton
class ConnectivityNetworkMonitor @Inject constructor(
    @param:ApplicationContext context: Context
) : NetworkMonitor {

    private val connectivity: ConnectivityManager? = context.getSystemService(ConnectivityManager::class.java)
    private val _isConnected = MutableStateFlow(connectivity?.activeNetwork != null)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        connectivity?.registerDefaultNetworkCallback(DefaultNetworkCallback())
    }

    private inner class DefaultNetworkCallback : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
        }

        override fun onLost(network: Network) {
            _isConnected.value = connectivity?.activeNetwork != null
        }
    }
}
