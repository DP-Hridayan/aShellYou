package `in`.hridayan.ashell.logcat.domain.repository

/**
 * Looks up which UID each installed package runs as, on one device.
 *
 * Logcat lines carry a UID rather than a package name, so package filters are applied through
 * these UIDs. Lookups never fail: a package that is not installed, or that cannot be looked up, is
 * simply absent from the result.
 */
interface PackageUidResolver {
    /** UIDs of those [packages] installed on the device, keyed by package. */
    suspend fun uidsOf(packages: Set<String>): Map<String, String>

    /** Packages installed under [uid], sorted; more than one when they share a UID. */
    suspend fun packagesOf(uid: String): List<String>
}
