package `in`.hridayan.ashell.core.utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object EncryptionHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"

    private const val SECRET_KEY =
        "your_32_byte_secret_key_string_here!" // security is not the main goal here

    private val keySpec get() = normalizeKey(SECRET_KEY)

    fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = Random.Default.nextBytes(16)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(data)
        return iv + encrypted
    }

    fun decrypt(data: ByteArray): ByteArray {
        if (data.size < 16) throw IllegalArgumentException("Invalid encrypted data")
        val iv = data.copyOfRange(0, 16)
        val encryptedData = data.copyOfRange(16, data.size)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
        return cipher.doFinal(encryptedData)
    }

    private fun normalizeKey(key: String, desiredLength: Int = 32): SecretKeySpec {
        require(desiredLength == 16 || desiredLength == 24 || desiredLength == 32) {
            "AES only supports 16, 24, or 32 byte keys"
        }

        val keyBytes = key.toByteArray(Charsets.UTF_8)

        val normalizedKey = ByteArray(desiredLength)
        System.arraycopy(keyBytes, 0, normalizedKey, 0, keyBytes.size.coerceAtMost(desiredLength))

        return SecretKeySpec(normalizedKey, "AES")
    }
}