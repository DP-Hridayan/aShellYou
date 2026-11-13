package `in`.hridayan.ashell.shell.fastboot.domain.validation

enum class PartitionRisk {
    STANDARD,
    USER_DATA,
    CRITICAL
}

sealed interface PartitionValidationResult {
    data object Valid : PartitionValidationResult
    data object Blank : PartitionValidationResult
    data object InvalidFormat : PartitionValidationResult
    data object TooLong : PartitionValidationResult
}

object PartitionValidator {

    private const val MAX_PARTITION_NAME_LENGTH = 64
    private val PARTITION_NAME_REGEX = Regex("^[a-zA-Z0-9_-]+$")

    private val CRITICAL_PARTITIONS = setOf(
        "bootloader", "aboot", "sbl1", "sbl2", "sbl3",
        "xbl", "xbl_a", "xbl_b", "xbl_config", "xbl_config_a", "xbl_config_b",
        "tz", "tz_a", "tz_b",
        "rpm", "rpm_a", "rpm_b",
        "hyp", "hyp_a", "hyp_b",
        "pmic", "pmic_a", "pmic_b",
        "gpt", "pgpt", "sgpt", "partition",
        "devinfo", "persist", "modem", "radio", "dsp", "frp", "keystore", "sec",
        "super", "vbmeta", "vbmeta_system"
    )

    private val USER_DATA_PARTITIONS = setOf(
        "userdata", "metadata", "cache"
    )

    fun validate(name: String): PartitionValidationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return PartitionValidationResult.Blank
        if (trimmed.length > MAX_PARTITION_NAME_LENGTH) return PartitionValidationResult.TooLong
        if (!PARTITION_NAME_REGEX.matches(trimmed)) return PartitionValidationResult.InvalidFormat
        return PartitionValidationResult.Valid
    }

    fun assessRisk(name: String): PartitionRisk {
        val normalized = name.trim().lowercase()
        val baseName = normalized.removeSuffix("_a").removeSuffix("_b")
        return when {
            normalized in CRITICAL_PARTITIONS || baseName in CRITICAL_PARTITIONS -> PartitionRisk.CRITICAL
            normalized in USER_DATA_PARTITIONS || baseName in USER_DATA_PARTITIONS -> PartitionRisk.USER_DATA
            else -> PartitionRisk.STANDARD
        }
    }
}
