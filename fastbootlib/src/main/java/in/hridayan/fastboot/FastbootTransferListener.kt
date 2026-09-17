package `in`.hridayan.fastboot

/**
 * Stages a data carrying command passes through. They are reported as each one begins, because
 * they differ in how safely the host may walk away: during [DOWNLOADING] the device is only
 * filling a memory buffer, while during [WRITING] it is committing to a partition.
 */
enum class FastbootStage {
    DOWNLOADING,
    WRITING,
}

interface FastbootTransferListener {
    fun onStage(stage: FastbootStage)
    fun onProgress(bytesSent: Long, totalBytes: Long)
}
