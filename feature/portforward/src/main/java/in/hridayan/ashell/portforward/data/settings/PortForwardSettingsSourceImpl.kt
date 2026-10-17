package `in`.hridayan.ashell.portforward.data.settings

import `in`.hridayan.ashell.core.common.domain.model.PortForwardDefaults
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettings
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettingsSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PortForwardSettingsSourceImpl @Inject constructor(
    private val settings: SettingsRepository
) : PortForwardSettingsSource {

    override suspend fun current(): PortForwardSettings = PortForwardSettings(
        connectTimeoutMillis = connectTimeoutSeconds() * MILLIS_PER_SECOND,
        maxConnections = PortForwardDefaults.sanitizeMaxConnections(
            settings.getInt(SettingsKeys.PortForwardMaxConnections).first()
        ),
        waitTimeoutMillis = PortForwardDefaults.waitTimeoutMillis(
            settings.getInt(SettingsKeys.PortForwardWaitTimeoutMinutes).first()
        ),
        resumeOnReconnect = settings.getBoolean(SettingsKeys.PortForwardResumeOnReconnect).first(),
        allowLanSharing = settings.getBoolean(SettingsKeys.PortForwardAllowLanSharing).first(),
        autoStart = settings.getBoolean(SettingsKeys.PortForwardAutoStart).first(),
        stopOnTaskRemoved = settings.getBoolean(SettingsKeys.PortForwardStopOnTaskRemoved).first(),
        unexpectedStopAlerts = settings.getBoolean(SettingsKeys.PortForwardUnexpectedStopAlerts).first()
    )

    private suspend fun connectTimeoutSeconds(): Int = PortForwardDefaults.sanitizeConnectTimeout(
        settings.getInt(SettingsKeys.PortForwardConnectTimeoutSeconds).first()
    )

    private companion object {
        const val MILLIS_PER_SECOND = 1_000
    }
}
