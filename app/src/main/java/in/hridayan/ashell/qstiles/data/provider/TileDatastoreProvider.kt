package `in`.hridayan.ashell.qstiles.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.tileDataStore by preferencesDataStore(name = "tile_prefs")