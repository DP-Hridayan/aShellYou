package `in`.hridayan.ashell.adbsideload.data.adb

import android.content.Context
import android.util.Base64
import android.util.Log
import com.cgutman.adblib.AdbBase64
import com.cgutman.adblib.AdbCrypto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the app's ADB RSA key pair, generating and persisting one on first use. The key files are
 * shared with the OTG shell so the target device only has to authorise this app once.
 */
@Singleton
class AdbKeyStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val base64 = AdbBase64 { data -> Base64.encodeToString(data, Base64.NO_WRAP) }

    @Volatile
    private var cached: AdbCrypto? = null

    fun load(): AdbCrypto? = cached ?: synchronized(this) {
        cached ?: loadOrGenerate().also { cached = it }
    }

    private fun loadOrGenerate(): AdbCrypto? {
        val privateKey = File(context.filesDir, PRIVATE_KEY_FILE)
        val publicKey = File(context.filesDir, PUBLIC_KEY_FILE)
        return runCatching {
            if (privateKey.exists() && publicKey.exists()) {
                AdbCrypto.loadAdbKeyPair(base64, privateKey, publicKey)
            } else {
                AdbCrypto.generateAdbKeyPair(base64).apply { saveAdbKeyPair(privateKey, publicKey) }
            }
        }.onFailure { Log.e(TAG, "Failed to initialise ADB key pair", it) }.getOrNull()
    }

    private companion object {
        const val TAG = "AdbKeyStore"
        const val PRIVATE_KEY_FILE = "private_key"
        const val PUBLIC_KEY_FILE = "public_key"
    }
}
