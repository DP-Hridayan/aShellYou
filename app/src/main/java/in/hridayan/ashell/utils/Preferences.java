package in.hridayan.ashell.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class Preferences {
  private static final String PREF_AMOLED_THEME = "id_amoled_theme",
      PREF_CLEAR = "id_clear",
      PREF_SHARE_AND_RUN = "id_share_and_run",
      PREF_DISABLE_SOFTKEY = "id_disable_softkey",
      PREF_OVERRIDE_BOOKMARKS = "id_override_bookmarks",
      PREF_SMOOTH_SCROLL = "id_smooth_scroll",
      PREF_SAVED_VERSION_CODE = "saved_version_code",
      PREF_SORTING_OPTION = "sorting_option",
      PREF_REMEMBER_WORKING_MODE = "id_remember_working_mode",
      PREF_CURRENT_FRAGMENT = "current_fragment";
  public static final int SORT_A_TO_Z = 0,
      SORT_Z_TO_A = 1,
      SORT_OLDEST = 2,
      SORT_NEWEST = 3,
      LOCAL_FRAGMENT = 1,
      OTG_FRAGMENT = 2;

  private static SharedPreferences getSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  public static boolean getAmoledTheme(Context context) {
    return getSharedPreferences(context).getBoolean(PREF_AMOLED_THEME, false);
  }

  public static void setAmoledTheme(Context context, boolean value) {
    getSharedPreferences(context).edit().putBoolean(PREF_AMOLED_THEME, value).apply();
  }

  public static boolean getClear(Context context) {
    return getSharedPreferences(context).getBoolean(PREF_CLEAR, true);
  }

  public static void setClear(Context context, boolean value) {
    getSharedPreferences(context).edit().putBoolean(PREF_CLEAR, value).apply();
  }

  public static boolean getShareAndRun(Context context) {
    return getSharedPreferences(context).getBoolean(PREF_SHARE_AND_RUN, false);
  }

  public static void setShareAndRun(Context context, boolean value) {
    getSharedPreferences(context).edit().putBoolean(PREF_SHARE_AND_RUN, value).apply();
  }

  public static boolean getDisableSoftkey(Context context) {
    return getSharedPreferences(context).getBoolean(PREF_DISABLE_SOFTKEY, false);
  }

  public static void setDisableSoftkey(Context context, boolean value) {
    getSharedPreferences(context).edit().putBoolean(PREF_DISABLE_SOFTKEY, value).apply();
  }

  public static boolean getOverrideBookmarks(Context context) {
    return getSharedPreferences(context).getBoolean(PREF_OVERRIDE_BOOKMARKS, false);
  }

  public static void setOverrideBookmarks(Context context, boolean value) {
    getSharedPreferences(context).edit().putBoolean(PREF_OVERRIDE_BOOKMARKS, value).apply();
  }

  public static boolean getSmoothScroll(Context context) {
    return getSharedPreferences(context).getBoolean(PREF_SMOOTH_SCROLL, true);
  }

  public static void setSmoothScroll(Context context, boolean value) {
    getSharedPreferences(context).edit().putBoolean(PREF_SMOOTH_SCROLL, value).apply();
  }

  public static boolean getRememberWorkingMode(Context context) {
    return getSharedPreferences(context).getBoolean(PREF_REMEMBER_WORKING_MODE, false);
  }

  public static void setRememberWorkingMode(Context context, boolean value) {
    getSharedPreferences(context).edit().putBoolean(PREF_REMEMBER_WORKING_MODE, value).apply();
  }

  public static int getSavedVersionCode(Context context) {
    return getSharedPreferences(context).getInt(PREF_SAVED_VERSION_CODE, 1);
  }

  public static void setSavedVersionCode(Context context, int value) {
    getSharedPreferences(context).edit().putInt(PREF_SAVED_VERSION_CODE, value).apply();
  }

  public static int getSortingOption(Context context) {

    return getSharedPreferences(context).getInt(PREF_SORTING_OPTION, SORT_A_TO_Z);
  }

  public static void setSortingOption(Context context, int value) {
    getSharedPreferences(context).edit().putInt(PREF_SORTING_OPTION, value).apply();
  }

  public static int getCurrentFragment(Context context) {
    return getSharedPreferences(context).getInt(PREF_CURRENT_FRAGMENT, LOCAL_FRAGMENT);
  }

  public static void setCurrentFragment(Context context, int value) {
    getSharedPreferences(context).edit().putInt(PREF_CURRENT_FRAGMENT, value).apply();
  }
}
