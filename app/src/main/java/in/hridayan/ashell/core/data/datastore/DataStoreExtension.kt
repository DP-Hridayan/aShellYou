package `in`.hridayan.ashell.core.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore

val Context.prefs: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")
