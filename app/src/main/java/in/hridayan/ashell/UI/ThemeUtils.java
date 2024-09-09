package in.hridayan.ashell.UI;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.config.Preferences;

public class ThemeUtils {
  private static boolean isAmoledTheme;

  public static void updateTheme(AppCompatActivity activity) {

    isAmoledTheme = Preferences.getAmoledTheme();

    int currentMode =
        activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

    if (isAmoledTheme && currentMode == Configuration.UI_MODE_NIGHT_YES)
      activity.setTheme(
          DeviceUtils.androidVersion() >= Build.VERSION_CODES.S
              ? R.style.ThemeOverlay_aShellYou_AmoledTheme
              : R.style.ThemeOverlay_aShellYou_AmoledThemeBelowV31);
    else activity.setTheme(R.style.aShellYou_AppTheme);
  }

  // Returns if device is in dark mode
  public static boolean isNightMode(Context context) {
    return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
        == Configuration.UI_MODE_NIGHT_YES;
  }

  // returns a color
  public static int getColor(int color, Context context) {
    return ContextCompat.getColor(context, color);
  }

  public static int colorError(Context context) {
    TypedValue typedValue = new TypedValue();
    context.getTheme().resolveAttribute(android.R.attr.colorError, typedValue, true);
    int colorError = typedValue.data;
    return colorError;
  }
}
