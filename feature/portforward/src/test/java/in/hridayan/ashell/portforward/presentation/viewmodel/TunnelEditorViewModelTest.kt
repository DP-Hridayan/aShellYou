package `in`.hridayan.ashell.portforward.presentation.viewmodel

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpoint
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.DevicePreset
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelException
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.usecase.ParseHostInputUseCase
import `in`.hridayan.ashell.portforward.domain.usecase.ValidateTunnelFormUseCase
import `in`.hridayan.ashell.portforward.presentation.model.LocalPortStatus
import `in`.hridayan.ashell.portforward.presentation.model.TestState
import `in`.hridayan.ashell.portforward.presentation.model.TunnelEditorEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TunnelEditorViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeSavedTunnelRepository()
    private val controller = FakeTunnelController()
    private val devices = FakeConnectedDevices()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        ports: FakePorts = FakePorts(),
        transport: ExternalDeviceTransport? = null
    ) = TunnelEditorViewModel(
        tunnelId = null,
        transport = transport,
        devices = devices,
        repository = repository,
        controller = controller,
        tester = FakeTester(),
        ports = ports,
        localNetwork = FakeLocalNetworkGate(),
        settings = FakeSettingsRepository(),
        parseHostInput = ParseHostInputUseCase(),
        validate = ValidateTunnelFormUseCase()
    )

    @Test
    fun `pasting a url fills the fields and the local port`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAddressChange("http://192.168.1.10:5173/admin")
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals("192.168.1.10", state.form.host)
        assertEquals("5173", state.form.targetPort)
        assertEquals("5173", state.form.localPort)
        assertEquals("/admin", state.form.path)
        assertTrue(state.form.looksLikeHttp)
        assertTrue(state.targetIsLocal)
        assertEquals(LocalPortStatus.Free, state.localPortStatus)
    }

    @Test
    fun `editing the local port detaches it from the target port`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAddressChange("nas.local:8080")
        vm.onLocalPortChange("9000")
        vm.onTargetPortChange("8081")

        assertEquals("9000", vm.state.value.form.localPort)
        assertFalse(vm.state.value.form.sameAsTarget)
    }

    @Test
    fun `saving an invalid form shows errors and saves nothing`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.save(start = true)
        advanceUntilIdle()

        assertTrue(vm.state.value.showErrors)
        assertTrue(repository.saved.value.isEmpty())
    }

    @Test
    fun `start saves, remembers the host and starts`() = runTest(dispatcher) {
        val vm = viewModel()
        val events = mutableListOf<TunnelEditorEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { events += it } }
        vm.onAddressChange("http://10.0.0.2:3000")
        advanceUntilIdle()

        vm.save(start = true)
        advanceUntilIdle()

        val spec = repository.saved.value.single()
        assertEquals(TunnelTarget.NetworkHost("10.0.0.2", 3000, null), spec.target)
        assertEquals("10.0.0.2", repository.hosts.single().host)
        assertEquals(listOf(spec), controller.started)
        assertEquals(listOf(TunnelEditorEvent.Saved(spec, started = true)), events)
    }

    @Test
    fun `a failed start stays on the editor with the error`() = runTest(dispatcher) {
        controller.startResult = Result.failure(TunnelException(TunnelError.PortPrivileged))
        val vm = viewModel()
        vm.onAddressChange("10.0.0.2:3000")
        advanceUntilIdle()

        vm.save(start = true)
        advanceUntilIdle()

        assertEquals(TunnelError.PortPrivileged, vm.state.value.startError)
        assertFalse(vm.state.value.isSaving)
    }

    @Test
    fun `a port taken by another app blocks start and suggests the next one`() = runTest(dispatcher) {
        val vm = viewModel(ports = FakePorts(taken = setOf(3000)))
        vm.onAddressChange("10.0.0.2:3000")
        advanceUntilIdle()

        assertEquals(LocalPortStatus.InUse(3001), vm.state.value.localPortStatus)
        vm.save(start = true)
        advanceUntilIdle()

        assertEquals(TunnelError.PortInUse(3001), vm.state.value.startError)
        assertTrue(controller.started.isEmpty())
    }

    @Test
    fun `an http reply marks the target as a website`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onHostChange("10.0.0.2")
        vm.onTargetPortChange("9000")
        vm.test()
        advanceUntilIdle()

        assertTrue(vm.state.value.test is TestState.Done)
        assertTrue(vm.state.value.form.looksLikeHttp)
    }

    @Test
    fun `a device tunnel targets the connected device`() = runTest(dispatcher) {
        devices.connect(ExternalDeviceTransport.OTG, deviceId = "serial-9", name = "Pixel 7")
        val vm = viewModel(transport = ExternalDeviceTransport.OTG)
        vm.onEndpointValueChange("9222")
        advanceUntilIdle()

        vm.save(start = true)
        advanceUntilIdle()

        val target = repository.saved.value.single().target as TunnelTarget.Device
        assertEquals("serial-9", target.deviceId)
        assertEquals(DeviceEndpoint(DeviceEndpointType.TCP, "9222"), target.endpoint)
        assertEquals("9222", vm.state.value.form.localPort)
    }

    @Test
    fun `a preset fills the socket and the local port`() = runTest(dispatcher) {
        devices.connect(ExternalDeviceTransport.WIFI_ADB, deviceId = null, name = "Tablet")
        val vm = viewModel(transport = ExternalDeviceTransport.WIFI_ADB)

        vm.onPresetSelected(DevicePreset.CHROME_DEVTOOLS, name = "Chrome DevTools")

        val form = vm.state.value.form
        assertEquals(DeviceEndpointType.ABSTRACT, form.endpointType)
        assertEquals("chrome_devtools_remote", form.endpointValue)
        assertEquals("9222", form.localPort)
        assertEquals("Chrome DevTools", form.name)
        assertTrue(form.looksLikeHttp)
        assertFalse(vm.state.value.canShareOnNetwork)
    }

    @Test
    fun `changing the target clears an old test result`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onHostChange("10.0.0.2")
        vm.onTargetPortChange("9000")
        vm.test()
        advanceUntilIdle()

        vm.onTargetPortChange("9001")

        assertEquals(TestState.Idle, vm.state.value.test)
    }

    @Test
    fun `a blocked action tells the screen`() = runTest(dispatcher) {
        val vm = viewModel()
        val events = mutableListOf<TunnelEditorEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { events += it } }

        vm.save(start = false)
        vm.test()
        advanceUntilIdle()

        assertEquals(listOf(TunnelEditorEvent.InvalidForm, TunnelEditorEvent.InvalidForm), events)
    }

    @Test
    fun `testing needs only the target, not a valid local port`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAddressChange("10.0.0.2:3000")
        vm.onLocalPortChange("80")

        vm.test()
        advanceUntilIdle()

        assertTrue(vm.state.value.test is TestState.Done)
    }

    @Test
    fun `a local port used by another tunnel blocks saving`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAddressChange("10.0.0.2:3000")
        advanceUntilIdle()
        vm.save(start = false)
        advanceUntilIdle()

        val second = viewModel()
        second.onAddressChange("10.0.0.3:3000")
        advanceUntilIdle()
        second.save(start = true)
        advanceUntilIdle()

        assertTrue(second.state.value.validation.localPort is ValidateTunnelFormUseCase.FieldError.UsedByTunnel)
        assertEquals(1, repository.saved.value.size)
        assertTrue(controller.started.isEmpty())
    }

    @Test
    fun `tapping start twice starts once`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAddressChange("10.0.0.2:3000")
        advanceUntilIdle()

        vm.save(start = true)
        vm.save(start = true)
        advanceUntilIdle()

        assertEquals(1, controller.started.size)
    }
}
