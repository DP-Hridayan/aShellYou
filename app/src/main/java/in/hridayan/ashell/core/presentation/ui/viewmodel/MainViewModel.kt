package `in`.hridayan.ashell.core.presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.data.datastore.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    init {
        handleFirstLaunch()
    }

    private fun handleFirstLaunch() {
        viewModelScope.launch {
            val firstLaunch = dataStoreManager.isFirstLaunch.first()
            if (firstLaunch) {
                dataStoreManager.setFirstLaunch(false)
                Log.d("HomeViewModel", "First Launch")
            } else {
                Log.d("HomeViewModel", "Not First Launch")
            }
        }
    }
}
