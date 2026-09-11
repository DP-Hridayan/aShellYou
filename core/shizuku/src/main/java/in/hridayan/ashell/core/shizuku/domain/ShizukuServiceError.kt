package `in`.hridayan.ashell.core.shizuku.domain

/**
 * Failure reasons for reaching or using the privileged Shizuku helper service.
 * Messages are technical and meant for logs; presentation layers map them to localized text.
 */
sealed class ShizukuServiceError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    data object BinderMissing : ShizukuServiceError("Shizuku binder is not available")

    data object PermissionDenied : ShizukuServiceError("Shizuku permission is not granted")

    data object BindTimeout : ShizukuServiceError("Timed out waiting for the Shizuku helper service")

    data object BinderDied : ShizukuServiceError("Shizuku helper service died")

    class BindFailed(cause: Throwable) :
        ShizukuServiceError("Could not bind the Shizuku helper service: ${cause.message}", cause)

    class ProcessStartFailed(cause: Throwable?) :
        ShizukuServiceError("Shizuku helper could not start the process: ${cause?.message}", cause)
}
