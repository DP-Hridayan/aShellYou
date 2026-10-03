package `in`.hridayan.ashell.mirror.data.server

import `in`.hridayan.ashell.mirror.BuildConfig

/**
 * Names of the bundled scrcpy server, all derived from the single pinned version in the version
 * catalog so the asset, the pushed file and the launch argument cannot disagree.
 *
 * The remote name carries this app's prefix so it never collides with desktop scrcpy's own
 * `scrcpy-server.jar`, and carries the version so a size check is enough to detect a stale copy.
 */
object ScrcpyServerArtifact {
    const val VERSION: String = BuildConfig.SCRCPY_SERVER_VERSION
    const val ASSET_NAME: String = "scrcpy-server-v$VERSION.jar"
    const val REMOTE_PREFIX: String = "/data/local/tmp/ashellyou-scrcpy-server-v"
    const val REMOTE_PATH: String = "$REMOTE_PREFIX$VERSION.jar"
}
