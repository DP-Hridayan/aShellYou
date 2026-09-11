package `in`.hridayan.ashell.core.shizuku.provider

import android.os.Bundle
import `in`.hridayan.ashell.core.shizuku.data.ShizukuBinderArbiter
import rikka.shizuku.ShizukuProvider

/**
 * [ShizukuProvider] that lets [ShizukuBinderArbiter] veto binder deliveries so stock Shizuku is
 * preferred when Shizuku+ is installed alongside it.
 */
class AShellShizukuProvider : ShizukuProvider() {

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method != METHOD_SEND_BINDER || extras == null) return super.call(method, arg, extras)
        val source = ShizukuBinderArbiter.classify(extras)
        if (!ShizukuBinderArbiter.shouldAccept(source)) return Bundle()
        val reply = super.call(method, arg, extras)
        ShizukuBinderArbiter.onAccepted(source)
        return reply
    }
}
