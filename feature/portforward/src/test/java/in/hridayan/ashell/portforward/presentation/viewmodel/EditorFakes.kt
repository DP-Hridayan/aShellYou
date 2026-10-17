package `in`.hridayan.ashell.portforward.presentation.viewmodel

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.RecentHost
import `in`.hridayan.ashell.portforward.domain.model.TestResult
import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.repository.ConnectedDevices
import `in`.hridayan.ashell.portforward.domain.repository.LocalNetworkGate
import `in`.hridayan.ashell.portforward.domain.repository.PortAvailability
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import `in`.hridayan.ashell.portforward.domain.repository.TunnelTester
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeSavedTunnelRepository : SavedTunnelRepository {
    val saved = MutableStateFlow<List<TunnelSpec>>(emptyList())
    val hosts = mutableListOf<RecentHost>()

    override fun tunnels(): Flow<List<TunnelSpec>> = saved
    override fun wasRunningIds(): Flow<Set<String>> = flowOf(emptySet())
    override suspend fun tunnel(id: String) = Result.success(saved.value.firstOrNull { it.id == id })
    override suspend fun save(spec: TunnelSpec): Result<Unit> {
        saved.value = saved.value.filterNot { it.id == spec.id } + spec
        return Result.success(Unit)
    }

    override suspend fun delete(id: String): Result<Unit> {
        saved.value = saved.value.filterNot { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun setWasRunning(ids: Collection<String>, wasRunning: Boolean) = Result.success(Unit)
    override fun recentHosts(): Flow<List<RecentHost>> = flowOf(hosts.toList())
    override suspend fun rememberHost(host: RecentHost): Result<Unit> {
        hosts += host
        return Result.success(Unit)
    }

    override suspend fun clearRecentHosts(): Result<Unit> = Result.success(Unit)
}

class FakeTunnelController : TunnelController {
    val started = mutableListOf<TunnelSpec>()
    var startResult: Result<Unit> = Result.success(Unit)

    override val runtimes: StateFlow<Map<String, TunnelRuntime>> = MutableStateFlow(emptyMap())
    override suspend fun start(spec: TunnelSpec): Result<Unit> {
        started += spec
        return startResult
    }

    override suspend fun stop(id: String) = Unit
    override suspend fun stopAll(): List<TunnelSpec> = emptyList()
    override fun dismiss(id: String) = Unit
}

class FakeTester(var result: TestResult = TestResult.Reachable(1, 200, "vite")) : TunnelTester {
    override suspend fun test(target: TunnelTarget): TestResult = result
}

class FakePorts(private val taken: Set<Int> = emptySet()) : PortAvailability {
    override suspend fun isFree(port: Int) = port !in taken
    override suspend fun nextFree(port: Int) = ((port + 1)..(port + 10)).firstOrNull { it !in taken }
}

class FakeLocalNetworkGate(private val local: Boolean = true) : LocalNetworkGate {
    override suspend fun isBlocked(host: String) = false
    override suspend fun isLocal(host: String) = local
    override fun isGranted() = true
}

class FakeConnectedDevices : ConnectedDevices {
    private val states = ExternalDeviceTransport.entries.associateWith {
        MutableStateFlow<ExternalDeviceState>(ExternalDeviceState.Disconnected)
    }

    fun connect(transport: ExternalDeviceTransport, deviceId: String?, name: String) {
        states.getValue(transport).value = ExternalDeviceState.Connected(deviceId, name)
    }

    override fun state(transport: ExternalDeviceTransport): StateFlow<ExternalDeviceState> = states.getValue(transport)
}

class FakeSettingsRepository : SettingsRepository {
    private val booleans = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val ints = MutableStateFlow<Map<String, Int>>(emptyMap())

    override val preferences: Flow<Preferences> = flowOf(emptyPreferences())
    override fun getBoolean(key: SettingsKeys<Boolean>) = booleans.map { it[key.name] ?: key.defaultValue }
    override suspend fun setBoolean(key: SettingsKeys<Boolean>, value: Boolean) {
        booleans.value += key.name to value
    }

    override suspend fun toggleSetting(key: SettingsKeys<Boolean>) = Unit
    override fun getInt(key: SettingsKeys<Int>) = ints.map { it[key.name] ?: key.defaultValue }
    override suspend fun setInt(key: SettingsKeys<Int>, value: Int) {
        ints.value += key.name to value
    }

    override fun getFloat(key: SettingsKeys<Float>) = flowOf(key.defaultValue)
    override suspend fun setFloat(key: SettingsKeys<Float>, value: Float) = Unit
    override fun getString(key: SettingsKeys<String>) = flowOf(key.defaultValue)
    override suspend fun setString(key: SettingsKeys<String>, value: String) = Unit
    override fun getAllDefaultSettings(): Map<String, Any?> = emptyMap()
    override fun getPreserveKeys(): Set<String> = emptySet()
    override suspend fun getCurrentSettings(): Map<String, Any?> = emptyMap()
    override suspend fun resetAndRestoreDefaults() = true
}
