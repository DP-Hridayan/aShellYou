package `in`.hridayan.ashell.core.ui.biometric

/**
 * Represents the various errors that can occur during biometric authentication.
 */
sealed class BiometricError {
    /**
     * The hardware is unavailable. Try again later.
     */
    object HardwareUnavailable : BiometricError()

    /**
     * The user can't authenticate because there is no suitable hardware (e.g., no biometric sensor or no keyguard).
     */
    object NoHardware : BiometricError()

    /**
     * The user does not have any biometrics enrolled.
     */
    object NoneEnrolled : BiometricError()

    /**
     * A security vulnerability has been discovered and the sensor is unavailable until a security update has addressed this issue.
     */
    object SecurityUpdateRequired : BiometricError()

    /**
     * The requested authenticators are not supported by the device.
     */
    object Unsupported : BiometricError()

    /**
     * Unable to determine whether the user can authenticate.
     */
    object StatusUnknown : BiometricError()

    /**
     * An error occurred during the authentication process itself (e.g., too many attempts, user canceled).
     */
    data class AuthError(val code: Int, val message: CharSequence) : BiometricError()

    /**
     * A biometric is valid but not recognized.
     */
    object AuthFailed : BiometricError()
}
