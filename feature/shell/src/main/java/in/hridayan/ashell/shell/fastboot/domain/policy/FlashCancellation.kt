package `in`.hridayan.ashell.shell.fastboot.domain.policy

import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus

/** What abandoning an operation means at the point it is asked for. */
enum class FlashCancelPolicy {
    /** Nothing has reached the device yet, so stopping costs nothing. */
    IMMEDIATE,

    /** The device is buffering the image. Stopping is safe for the device but ends the session. */
    CONFIRM,

    /** The device is writing. Stopping is not possible and pretending otherwise is dangerous. */
    UNSAFE,
}

/**
 * Decides whether the user may abandon an operation.
 *
 * The fastboot protocol has no cancel command: a host can only stop listening. Once the device has
 * been told to write a partition it finishes regardless, so offering a cancel button there would
 * promise something that cannot happen and invite the user to disconnect mid write.
 */
object FlashCancellation {

    fun policyFor(status: FlashStatus): FlashCancelPolicy = when (status) {
        FlashStatus.DOWNLOADING -> FlashCancelPolicy.CONFIRM
        FlashStatus.WRITING,
        FlashStatus.ERASING,
        FlashStatus.CANCELLING -> FlashCancelPolicy.UNSAFE

        else -> FlashCancelPolicy.IMMEDIATE
    }

    fun isCancellable(status: FlashStatus): Boolean = policyFor(status) != FlashCancelPolicy.UNSAFE
}
