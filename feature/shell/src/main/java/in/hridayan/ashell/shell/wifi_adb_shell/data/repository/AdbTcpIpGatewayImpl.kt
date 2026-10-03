package `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.utils.TcpIpUtils
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.executor.AdbServiceExecutor
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.executor.RESTART_REPLY_PREFIX
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.AdbTcpIpGateway
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.TcpIpRequestOutcome
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "AdbTcpIpGateway"
private const val TCPIP_SERVICE_PREFIX = "tcpip:"

class AdbTcpIpGatewayImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val wifiAdbRepository: WifiAdbRepository
) : AdbTcpIpGateway {

    private val serviceExecutor = AdbServiceExecutor(context)

    override fun isConnected(): Boolean = wifiAdbRepository.isConnected()

    /**
     * Disconnects once the daemon accepts, as the terminal does after a restarting service, so the
     * saved connection state never outlives the daemon that held it.
     */
    override suspend fun requestTcpIp(port: Int): TcpIpRequestOutcome = withContext(Dispatchers.IO) {
        val outcome = try {
            tcpIpOutcomeOf(firstReply(port))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "tcpip request failed", e)
            TcpIpRequestOutcome.Refused(e.message.orEmpty())
        }

        if (outcome is TcpIpRequestOutcome.Accepted) wifiAdbRepository.disconnect()
        outcome
    }

    override fun currentTcpPort(): Int? = TcpIpUtils.getAdbTcpPort().takeIf { it > 0 }

    private suspend fun firstReply(port: Int): OutputLine? =
        serviceExecutor.execute(
            service = TCPIP_SERVICE_PREFIX + port,
            restartsDaemon = true,
            onStreamOpened = {},
            onDaemonRestarted = {}
        ).firstOrNull()
}

/**
 * A restarting daemon may close the stream before its reply arrives, so no reply at all is treated as
 * acceptance and left for the port check to confirm.
 */
internal fun tcpIpOutcomeOf(firstLine: OutputLine?): TcpIpRequestOutcome = when {
    firstLine == null -> TcpIpRequestOutcome.Accepted
    firstLine.isError -> TcpIpRequestOutcome.Refused(firstLine.text)
    firstLine.text.trimStart().startsWith(RESTART_REPLY_PREFIX, ignoreCase = true) ->
        TcpIpRequestOutcome.Accepted

    else -> TcpIpRequestOutcome.Refused(firstLine.text)
}
