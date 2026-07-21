package `in`.hridayan.ashell.core.common.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "settings")
