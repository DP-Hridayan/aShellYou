package `in`.hridayan.ashell.ai.data.security

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : ApiKeyRepository {

    private val prefs = context.getSharedPreferences("ai_api_keys", Context.MODE_PRIVATE)
    private val aead: Aead

    init {
        AeadConfig.register()
        aead = AndroidKeysetManager.Builder()
            .withSharedPref(context, "master_keyset", "tink_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://master_key")
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private val presentIds = MutableStateFlow(
        LlmProvider.all.filter { prefs.contains(it.id) }.map { it.id }.toSet()
    )

    override fun setKey(provider: LlmProvider, key: String) {
        val encrypted = aead.encrypt(key.toByteArray(), null)
        val encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)

        prefs.edit { putString(provider.id, encoded) }
        presentIds.value += provider.id
    }

    override fun getKey(provider: LlmProvider): String? {
        val encoded = prefs.getString(provider.id, null) ?: return null
        return try {
            val decoded = Base64.decode(encoded, Base64.DEFAULT)
            String(aead.decrypt(decoded, null))
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteKey(provider: LlmProvider) {
        prefs.edit { remove(provider.id) }
        presentIds.value -= provider.id
    }

    override fun hasKey(provider: LlmProvider): Flow<Boolean> =
        presentIds.map { it.contains(provider.id) }
}
