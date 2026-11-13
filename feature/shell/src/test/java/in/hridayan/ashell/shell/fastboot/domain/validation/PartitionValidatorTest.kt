package `in`.hridayan.ashell.shell.fastboot.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PartitionValidatorTest {

    @Test
    fun `validate returns Blank for empty or whitespace string`() {
        assertEquals(PartitionValidationResult.Blank, PartitionValidator.validate(""))
        assertEquals(PartitionValidationResult.Blank, PartitionValidator.validate("   "))
    }

    @Test
    fun `validate returns InvalidFormat for illegal characters`() {
        assertEquals(PartitionValidationResult.InvalidFormat, PartitionValidator.validate("boot loader"))
        assertEquals(PartitionValidationResult.InvalidFormat, PartitionValidator.validate("boot:slot"))
        assertEquals(PartitionValidationResult.InvalidFormat, PartitionValidator.validate("boot/a"))
        assertEquals(PartitionValidationResult.InvalidFormat, PartitionValidator.validate("system!"))
        assertEquals(PartitionValidationResult.InvalidFormat, PartitionValidator.validate("boot;reboot"))
    }

    @Test
    fun `validate returns TooLong when exceeding maximum length`() {
        val tooLong = "a".repeat(65)
        assertEquals(PartitionValidationResult.TooLong, PartitionValidator.validate(tooLong))
    }

    @Test
    fun `validate returns Valid for standard partition names`() {
        assertEquals(PartitionValidationResult.Valid, PartitionValidator.validate("boot"))
        assertEquals(PartitionValidationResult.Valid, PartitionValidator.validate("boot_a"))
        assertEquals(PartitionValidationResult.Valid, PartitionValidator.validate("vendor-boot"))
        assertEquals(PartitionValidationResult.Valid, PartitionValidator.validate("my_custom_123"))
    }

    @Test
    fun `assessRisk identifies critical bootloader and hardware partitions`() {
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("bootloader"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("xbl"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("xbl_a"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("gpt"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("persist"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("devinfo"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("modem_b"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("super"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("vbmeta"))
        assertEquals(PartitionRisk.CRITICAL, PartitionValidator.assessRisk("vbmeta_system"))
    }

    @Test
    fun `assessRisk identifies user data partitions`() {
        assertEquals(PartitionRisk.USER_DATA, PartitionValidator.assessRisk("userdata"))
        assertEquals(PartitionRisk.USER_DATA, PartitionValidator.assessRisk("metadata"))
        assertEquals(PartitionRisk.USER_DATA, PartitionValidator.assessRisk("cache"))
    }

    @Test
    fun `assessRisk identifies standard partitions`() {
        assertEquals(PartitionRisk.STANDARD, PartitionValidator.assessRisk("boot"))
        assertEquals(PartitionRisk.STANDARD, PartitionValidator.assessRisk("recovery"))
        assertEquals(PartitionRisk.STANDARD, PartitionValidator.assessRisk("product"))
        assertEquals(PartitionRisk.STANDARD, PartitionValidator.assessRisk("custom_partition"))
    }
}
