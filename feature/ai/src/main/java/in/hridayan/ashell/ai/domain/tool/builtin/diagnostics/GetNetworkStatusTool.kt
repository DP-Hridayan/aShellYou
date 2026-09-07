package `in`.hridayan.ashell.ai.domain.tool.builtin.diagnostics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetNetworkStatusTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AiTool {

    override val name: String = "get_network_status"

    override val description: String =
        "Retrieves the current network connectivity status (e.g., Connected to Wi-Fi/Cellular) natively."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return "Failed to access ConnectivityManager."

        return buildNetworkStatusString(connectivityManager)
    }

    private fun buildNetworkStatusString(connectivityManager: ConnectivityManager): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getNetworkStatusModern(connectivityManager)
        } else {
            getNetworkStatusLegacy(connectivityManager)
        }
    }

    private fun getNetworkStatusModern(connectivityManager: ConnectivityManager): String {
        val activeNetwork =
            connectivityManager.activeNetwork ?: return "Network Status: Disconnected"
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return "Network Status: Disconnected"

        val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val networkType = determineModernNetworkType(capabilities)

        return """
            Network Status:
            Is Connected: $isConnected
            Active Network Type: $networkType
        """.trimIndent()
    }

    private fun determineModernNetworkType(capabilities: NetworkCapabilities): String {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Unknown"
        }
    }

    @Suppress("DEPRECATION")
    private fun getNetworkStatusLegacy(connectivityManager: ConnectivityManager): String {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isConnected = activeNetworkInfo?.isConnected == true
        val typeName = activeNetworkInfo?.typeName ?: "Unknown"

        return """
            Network Status:
            Is Connected: $isConnected
            Active Network Type: $typeName
        """.trimIndent()
    }
}
