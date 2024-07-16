package in.hridayan.ashell.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class Preferences {
  public static final String buildGradleUrl =
      "https://raw.githubusercontent.com/DP-Hridayan/aShellYou/master/app/build.gradle";

  private static final String PREF_AMOLED_THEME = "id_amoled_theme",
      PREF_COUNTER_PREFIX = "counter_",
      PREF_PINNED_PREFIX = "pinned",
      PREF_CLEAR = "id_clear",
      PREF_SHARE_AND_RUN = "id_share_and_run",
      PREF_DISABLE_SOFTKEY = "id_disable_softkey",
      PREF_OVERRIDE_BOOKMARKS = "id_override_bookmarks",
      PREF_SMOOTH_SCROLL = "id_smooth_scroll",
      PREF_SAVED_VERSION_CODE = "saved_version_code",
      PREF_SORTING_OPTION = "sorting_option",
      PREF_SORTING_EXAMPLES = "sorting_examples",
      PREF_CURRENT_FRAGMENT = "current_fragment",
      PREF_DEFAULT_WORKING_MODE = "id_default_working_mode",
      PREF_SPECIFIC_CARD_VISIBILITY = "specific_card_visibility",
      PREF_AUTO_UPDATE_CHECK = "id_auto_update_check";
  public static final int SORT_A_TO_Z = 0,
      SORT_Z_TO_A = 1,
      SORT_MOST_USED = 2,
      SORT_OLDEST = 2,
      SORT_NEWEST = 3,
      SORT_LEAST_USED = 3,
      LOCAL_FRAGMENT = 1,
      OTG_FRAGMENT = 2,
      MODE_LOCAL_ADB = 0,
      MODE_OTG = 1,
      MODE_REMEMBER_LAST_MODE = 2,
      MAX_BOOKMARKS_LIMIT = 25,
      UPDATE_AVAILABLE = 1,
      UPDATE_NOT_AVAILABLE = 0,
      CONNECTION_ERROR = 2;

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

  public static boolean getAutoUpdateCheck(Context context) {
    return getSharedPreferences(context).getBoolean(PREF_AUTO_UPDATE_CHECK, true);
  }

  public static void setAutoUpdateCheck(Context context, boolean value) {
    getSharedPreferences(context).edit().putBoolean(PREF_AUTO_UPDATE_CHECK, value).apply();
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

  public static int getSortingExamples(Context context) {

    return getSharedPreferences(context).getInt(PREF_SORTING_EXAMPLES, SORT_A_TO_Z);
  }

  public static void setSortingExamples(Context context, int value) {
    getSharedPreferences(context).edit().putInt(PREF_SORTING_EXAMPLES, value).apply();
  }

  public static void setWorkingMode(Context context, int value) {
    getSharedPreferences(context).edit().putInt(PREF_DEFAULT_WORKING_MODE, value).apply();
  }

  public static int getWorkingMode(Context context) {
    return getSharedPreferences(context).getInt(PREF_DEFAULT_WORKING_MODE, MODE_LOCAL_ADB);
  }

  public static int getCurrentFragment(Context context) {
    return getSharedPreferences(context).getInt(PREF_CURRENT_FRAGMENT, LOCAL_FRAGMENT);
  }

  public static void setCurrentFragment(Context context, int value) {
    getSharedPreferences(context).edit().putInt(PREF_CURRENT_FRAGMENT, value).apply();
  }

  public static int getUseCounter(Context context, String title) {
    return getSharedPreferences(context).getInt(getCounterKey(title), 0);
  }

  public static void setUseCounter(Context context, String title, int counter) {
    getSharedPreferences(context).edit().putInt(getCounterKey(title), counter).apply();
  }

  public static boolean getPinned(Context context, String title) {
    return getSharedPreferences(context).getBoolean(getPinnedKey(title), false);
  }

  public static void setPinned(Context context, String title, boolean value) {
    getSharedPreferences(context).edit().putBoolean(getPinnedKey(title), value).apply();
  }

  public static boolean getSpecificCardVisibility(Context context, String title) {
    return getSharedPreferences(context).getBoolean(getCardKey(title), true);
  }

  public static void setSpecificCardVisibility(Context context, String title, boolean value) {
    getSharedPreferences(context).edit().putBoolean(getCardKey(title), value).apply();
  }

  private static String getCounterKey(String title) {
    return PREF_COUNTER_PREFIX + title;
  }

  private static String getPinnedKey(String title) {
    return PREF_PINNED_PREFIX + title;
  }

  private static String getCardKey(String title) {
    return PREF_SPECIFIC_CARD_VISIBILITY + title;
  }
}
