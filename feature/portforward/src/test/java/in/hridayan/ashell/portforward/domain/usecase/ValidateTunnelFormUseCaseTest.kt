package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.usecase.ValidateTunnelFormUseCase.DeviceInput
import `in`.hridayan.ashell.portforward.domain.usecase.ValidateTunnelFormUseCase.FieldError
import `in`.hridayan.ashell.portforward.domain.usecase.ValidateTunnelFormUseCase.Input
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateTunnelFormUseCaseTest {

    private val validate = ValidateTunnelFormUseCase()

    private val valid = Input(host = "192.168.1.10", targetPort = "5173", localPort = "5173")

    @Test
    fun `valid input has no errors`() {
        val result = validate(valid)
        assertTrue(result.isValid)
        assertNull(result.host)
        assertNull(result.targetPort)
        assertNull(result.localPort)
    }

    @Test
    fun `blank host is required`() {
        assertEquals(FieldError.Required, validate(valid.copy(host = " ")).host)
    }

    @Test
    fun `host with a space is invalid`() {
        assertEquals(FieldError.Invalid, validate(valid.copy(host = "my pc")).host)
    }

    @Test
    fun `missing target port is required`() {
        assertEquals(FieldError.Required, validate(valid.copy(targetPort = "")).targetPort)
    }

    @Test
    fun `target port out of range is invalid`() {
        assertEquals(FieldError.Invalid, validate(valid.copy(targetPort = "0")).targetPort)
        assertEquals(FieldError.Invalid, validate(valid.copy(targetPort = "65536")).targetPort)
    }

    @Test
    fun `privileged target port is allowed`() {
        assertNull(validate(valid.copy(targetPort = "80")).targetPort)
    }

    @Test
    fun `privileged local port is rejected`() {
        assertEquals(FieldError.Privileged, validate(valid.copy(localPort = "80")).localPort)
    }

    @Test
    fun `non numeric local port is invalid`() {
        assertEquals(FieldError.Invalid, validate(valid.copy(localPort = "80a")).localPort)
    }

    @Test
    fun `device tcp endpoint needs a valid port`() {
        assertNull(validate.device(DeviceInput(DeviceEndpointType.TCP, "9222", "9222")).targetPort)
        assertEquals(FieldError.Invalid, validate.device(DeviceInput(DeviceEndpointType.TCP, "0", "9222")).targetPort)
        assertEquals(FieldError.Required, validate.device(DeviceInput(DeviceEndpointType.TCP, "", "9222")).targetPort)
    }

    @Test
    fun `device socket names cannot contain spaces`() {
        assertNull(
            validate.device(DeviceInput(DeviceEndpointType.ABSTRACT, "chrome_devtools_remote", "9222")).targetPort
        )
        assertEquals(
            FieldError.Invalid,
            validate.device(DeviceInput(DeviceEndpointType.ABSTRACT, "bad name", "9222")).targetPort
        )
    }

    @Test
    fun `jdwp needs a process id`() {
        assertNull(validate.device(DeviceInput(DeviceEndpointType.JDWP, "4321", "8700")).targetPort)
        assertEquals(
            FieldError.Invalid,
            validate.device(DeviceInput(DeviceEndpointType.JDWP, "abc", "8700")).targetPort
        )
    }

    @Test
    fun `device form checks the local port and has no host`() {
        val result = validate.device(DeviceInput(DeviceEndpointType.TCP, "9222", "80"))
        assertNull(result.host)
        assertEquals(FieldError.Privileged, result.localPort)
    }

    @Test
    fun `local port used by another tunnel is reported with its name`() {
        val result = validate(valid.copy(portsInUse = mapOf(5173 to "Grafana")))
        assertEquals(FieldError.UsedByTunnel("Grafana"), result.localPort)
        assertFalse(result.isValid)
    }
}
