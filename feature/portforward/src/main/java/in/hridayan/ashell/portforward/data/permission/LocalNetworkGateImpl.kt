package `in`.hridayan.ashell.portforward.data.permission

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.network.LocalNetworkAddress
import `in`.hridayan.ashell.core.common.network.LocalNetworkPermission
import `in`.hridayan.ashell.portforward.di.PortForwardDispatcher
import `in`.hridayan.ashell.portforward.domain.repository.LocalNetworkGate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Resolves hosts to decide whether they are on the local network.
 *
 * A host that cannot be resolved is treated as not local, so the connection is attempted and fails
 * with a clear "host not found" instead.
 */
class LocalNetworkGateImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:PortForwardDispatcher private val dispatcher: CoroutineDispatcher
) : LocalNetworkGate {

    override fun isGranted(): Boolean = LocalNetworkPermission.isGranted(context)

    override suspend fun isBlocked(host: String): Boolean = !isGranted() && isLocal(host)

    override suspend fun isLocal(host: String): Boolean {
        if (LocalNetworkAddress.isMdnsName(host)) return true
        return withContext(dispatcher) { resolvesToLocal(host) }
    }

    private fun resolvesToLocal(host: String): Boolean = try {
        InetAddress.getAllByName(host).any(LocalNetworkAddress::isLocal)
    } catch (_: UnknownHostException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
