package in.hridayan.ashell.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import in.hridayan.ashell.R;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.DeviceUtils;

public class ThemeUtils {
  private static boolean isAmoledTheme, isDynamicTheme;
  private static int themeMode;

  public static void updateTheme(AppCompatActivity activity) {

    isAmoledTheme = Preferences.getAmoledTheme();
    isDynamicTheme = Preferences.getDynamicColors();
    themeMode = Preferences.getThemeMode();

    AppCompatDelegate.setDefaultNightMode(themeMode);

    if (isAmoledTheme && isNightMode(activity)) setHighContrastDarkTheme(activity);
    else setNormalTheme(activity);
  }

  private static void setHighContrastDarkTheme(AppCompatActivity activity) {
    if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S)
      activity.setTheme(
          isDynamicTheme
              ? R.style.aShellYou_AmoledTheme_DynamicColors
              : R.style.aShellYou_AmoledTheme);
    else activity.setTheme(R.style.ThemeOverlay_aShellYou_AmoledThemeBelowV31);
  }

  private static void setNormalTheme(AppCompatActivity activity) {
    if (isDynamicTheme) activity.setTheme(R.style.aShellYou_DynamicColors);
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
