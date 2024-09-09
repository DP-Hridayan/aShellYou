package in.hridayan.ashell.utils;

import android.view.HapticFeedbackConstants;
import android.view.View;
import in.hridayan.ashell.config.Preferences;

public class HapticUtils {

  /** Types of vibration. */
  public enum VibrationType {
    Weak,
    Strong
  }

  public static void vibrate(View view, VibrationType type) {
    if (Preferences.getHapticsAndVibration()) {
      switch (type) {
        case Weak:
          view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
          break;
        case Strong:
          view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
          break;
      }
    }
  }

  public static void weakVibrate(View view) {
    vibrate(view, VibrationType.Weak);
  }

  public static void strongVibrate(View view) {
    vibrate(view, VibrationType.Strong);
  }
}
