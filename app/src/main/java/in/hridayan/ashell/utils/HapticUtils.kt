package `in`.hridayan.ashell.utils

import android.view.HapticFeedbackConstants
import android.view.View
import `in`.hridayan.ashell.config.Preferences

object HapticUtils {
    private fun vibrate(view: View?, type: VibrationType) {
        if (Preferences.getHapticsAndVibration()) {
            when (type) {
                VibrationType.Weak -> view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                VibrationType.Strong -> view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    @JvmStatic
    fun weakVibrate(view: View?) {
        vibrate(view, VibrationType.Weak)
    }

    fun strongVibrate(view: View?) {
        vibrate(view, VibrationType.Strong)
    }

    /** Types of vibration.  */
    enum class VibrationType {
        Weak,
        Strong
    }
}
