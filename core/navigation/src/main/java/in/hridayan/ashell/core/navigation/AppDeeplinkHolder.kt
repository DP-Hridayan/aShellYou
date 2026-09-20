package `in`.hridayan.ashell.core.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
/**
 * Singleton holder for application-wide deeplink and shortcut actions.
 * Centralizes the reactive navigation events to allow [MainActivity] to trigger
 * navigation that the Compose NavGraph observes.
 */
object AppDeeplinkHolder {

    enum class DeeplinkDestination {
        LOGCAT,
        AI_CHAT
    }

    private val _navigationChannel = Channel<DeeplinkDestination>(capacity = Channel.BUFFERED)
    val navigationEvents: Flow<DeeplinkDestination> = _navigationChannel.receiveAsFlow()

    fun navigateTo(destination: DeeplinkDestination) {
        _navigationChannel.trySend(destination)
    }

    const val ACTION_OPEN_LOGCAT = "in.hridayan.ashell.ACTION_OPEN_LOGCAT"
    const val ACTION_OPEN_AI_AGENT = "in.hridayan.ashell.ACTION_OPEN_AI_AGENT"
}
