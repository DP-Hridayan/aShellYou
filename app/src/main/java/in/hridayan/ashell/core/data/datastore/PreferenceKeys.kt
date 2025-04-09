package `in`.hridayan.ashell.core.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferenceKeys{
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    val IS_DARK_MODE = intPreferencesKey("is_dark_mode")
    val IS_HIGH_CONTRAST_DARK_MODE = booleanPreferencesKey("is_high_contrast_dark_mode")
}
