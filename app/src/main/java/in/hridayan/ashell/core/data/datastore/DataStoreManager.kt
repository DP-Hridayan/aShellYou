package `in`.hridayan.ashell.core.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.config.MODE_SYSTEM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.prefs

    val isDarkMode: Flow<Int> = prefs.data
        .map { it[PreferenceKeys.IS_DARK_MODE] ?: MODE_SYSTEM }

    suspend fun setDarkMode(mode: Int) {
        updatePreference(PreferenceKeys.IS_DARK_MODE, mode)
    }

    val isHighContrastDarkMode: Flow<Boolean> = prefs.data
        .map { it[PreferenceKeys.IS_HIGH_CONTRAST_DARK_MODE] ?: true }

    suspend fun setHighContrastDarkMode(enabled: Boolean) {
        updatePreference(PreferenceKeys.IS_HIGH_CONTRAST_DARK_MODE, enabled)
    }

    val isFirstLaunch: Flow<Boolean> = prefs.data
        .map { it[PreferenceKeys.IS_FIRST_LAUNCH] ?: true }

    suspend fun setFirstLaunch(enabled: Boolean) {
        updatePreference(PreferenceKeys.IS_FIRST_LAUNCH, enabled)
    }

    private suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        prefs.edit { it[key] = value }
    }
}
