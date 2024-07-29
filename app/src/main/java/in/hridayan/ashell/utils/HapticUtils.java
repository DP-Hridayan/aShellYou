package in.hridayan.ashell.utils;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.View;
import in.hridayan.ashell.utils.Preferences;

public class HapticUtils {

  /** Types of vibration. */
  public enum VibrationType {
    Weak,
    Strong
  }

  public static void vibrate(View view, VibrationType type, Context context) {
    if (Preferences.getHapticsAndVibration(context)) {
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

  public static void weakVibrate(View view, Context context) {
    vibrate(view, VibrationType.Weak, context);
  }

  public static void strongVibrate(View view, Context context) {
    vibrate(view, VibrationType.Strong, context);
  }
}
