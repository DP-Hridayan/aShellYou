package `in`.hridayan.ashell.core.shizuku.domain

import android.os.IBinder

/**
 * Identifies this app process to the helper. The helper watches [binder] for death and destroys
 * the processes this app started, so a helper kept running after the app dies leaves no orphans.
 */
class ShizukuClientToken(val binder: IBinder)
