package `in`.hridayan.ashell.core.presentation.ui.utils

import android.view.HapticFeedbackConstants
import android.view.View

object HapticUtils {

     fun View.weakHaptic() {
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun View.strongHaptic() {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}