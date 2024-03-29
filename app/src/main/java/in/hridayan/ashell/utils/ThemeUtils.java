package in.hridayan.ashell.utils;

import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatActivity;
import in.hridayan.ashell.R;

public class ThemeUtils {
  private static boolean isAmoledTheme;

  public static void updateTheme(AppCompatActivity activity) {
    isAmoledTheme = Preferences.getAmoledTheme(activity);

    int currentMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

    if (isAmoledTheme && currentMode == Configuration.UI_MODE_NIGHT_YES) {
      activity.setTheme(R.style.ThemeOverlay_aShellYou_AmoledTheme);
    } else {
      activity.setTheme(R.style.aShellYou_AppTheme);
    }
  }
}
