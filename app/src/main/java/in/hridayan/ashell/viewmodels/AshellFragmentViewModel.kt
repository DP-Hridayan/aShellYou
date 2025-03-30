package `in`.hridayan.ashell.viewmodels

import android.util.Pair
import androidx.lifecycle.ViewModel

class AshellFragmentViewModel : ViewModel() {
    var isSaveButtonVisible: Boolean = false
    var isEndIconVisible: Boolean = false
    var commandText: String? = null
    var shellOutput: MutableList<String>? = null
    var history: MutableList<String>? = null
    var scrollPosition: Int = 0
    var sendDrawable: Int = nullValue
    var rvPositionAndOffset: Pair<Int, Int>? = null

    val isSendDrawableSaved: Boolean
        get() = sendDrawable != nullValue

    companion object {
        private const val nullValue = 2004
    }
}
