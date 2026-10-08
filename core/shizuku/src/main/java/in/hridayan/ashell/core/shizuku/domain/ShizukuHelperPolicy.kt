package `in`.hridayan.ashell.core.shizuku.domain

import kotlinx.coroutines.flow.Flow

/**
 * User preferences that decide when the privileged helper is started and how long it lives.
 * Supplied by the app module so this module does not read settings itself.
 */
interface ShizukuHelperPolicy {

    /**
     * True while the user has set something to run through Shizuku, so the helper is worth
     * binding at app start instead of on the first command.
     */
    val warmUpWanted: Flow<Boolean>

    /**
     * True when the helper should keep running after the app process dies, so the next launch
     * reconnects to it instead of starting it again.
     */
    val keepAlive: Flow<Boolean>
}
