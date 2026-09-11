package `in`.hridayan.ashell.core.shizuku.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.constants.SHIZUKU_PACKAGE_NAME
import `in`.hridayan.ashell.core.common.constants.SHIZUKU_PLUS_PACKAGE_NAME
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuBinderSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

private const val ACTION_REQUEST_BINDER = "rikka.shizuku.intent.action.REQUEST_BINDER"
private const val EXTRA_DATA = "data"
private const val EXTRA_REPLY_BINDER = "binder"
private const val REPLY_TRANSACTION_CODE = 1
private const val REPLY_TIMEOUT_MS = 3_000L

/**
 * Asks an installed Shizuku manager to hand over its live server binder.
 * Uses the manager's exported `REQUEST_BINDER` receiver: the manager calls back into the
 * supplied [Binder] with transaction 1 carrying the server binder. Stock is asked first.
 */
@Singleton
class ShizukuBinderRequester @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun requestPreferringStock(): Boolean {
        val candidates = listOf(
            SHIZUKU_PACKAGE_NAME to ShizukuBinderSource.STOCK,
            SHIZUKU_PLUS_PACKAGE_NAME to ShizukuBinderSource.PLUS
        )
        return candidates
            .filter { (packageName, _) -> isInstalled(packageName) }
            .any { (packageName, source) -> requestFrom(packageName, source) }
    }

    @SuppressLint("RestrictedApi")
    private suspend fun requestFrom(packageName: String, source: ShizukuBinderSource): Boolean {
        val reply = CompletableDeferred<IBinder?>()
        context.sendBroadcast(requestIntent(packageName, ReplyBinder(reply)))
        val binder = withTimeoutOrNull(REPLY_TIMEOUT_MS) { reply.await() } ?: return false
        if (!binder.pingBinder()) return false
        Shizuku.onBinderReceived(binder, context.packageName)
        ShizukuBinderArbiter.onAccepted(source)
        return true
    }

    private fun requestIntent(packageName: String, replyBinder: Binder): Intent {
        val data = Bundle().apply { putBinder(EXTRA_REPLY_BINDER, replyBinder) }
        return Intent(ACTION_REQUEST_BINDER)
            .setPackage(packageName)
            .putExtra(EXTRA_DATA, data)
    }

    private fun isInstalled(packageName: String): Boolean =
        runCatching { context.packageManager.getPackageInfo(packageName, 0) }.isSuccess

    private class ReplyBinder(private val reply: CompletableDeferred<IBinder?>) : Binder() {
        override fun onTransact(code: Int, data: Parcel, replyParcel: Parcel?, flags: Int): Boolean {
            if (code != REPLY_TRANSACTION_CODE) return super.onTransact(code, data, replyParcel, flags)
            reply.complete(data.readStrongBinder())
            return true
        }
    }
}
