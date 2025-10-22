package `in`.hridayan.ashell.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
) : ViewModel() {

    fun requestRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream

            outputStream.write("id\n".toByteArray())
            outputStream.flush()

            outputStream.write("exit\n".toByteArray())
            outputStream.flush()

            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            false
        }
    }
}
