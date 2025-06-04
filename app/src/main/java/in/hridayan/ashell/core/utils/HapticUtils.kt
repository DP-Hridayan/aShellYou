package `in`.hridayan.ashell.core.utils

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

object HapticUtils {

     fun View.weakHaptic() {
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun View.strongHaptic() {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}