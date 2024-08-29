package in.hridayan.ashell.config;

import android.content.Context;
import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import in.hridayan.ashell.AshellYou;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.config.Const;

public class Preferences {

  public static SharedPreferences prefs =
      AshellYou.getAppContext()
          .createDeviceProtectedStorageContext()
          .getSharedPreferences(Const.SHARED_PREFS, MODE_PRIVATE);
  static SharedPreferences.Editor editor = prefs.edit();

  public static String getLatestVersionName() {
    return prefs.getString(Const.PREF_LATEST_VERSION_NAME, BuildConfig.VERSION_NAME);
  }

  public static void setLatestVersionName(String value) {
    editor.putString(Const.PREF_LATEST_VERSION_NAME, value).apply();
  }

  public static String getLastSavedFileName() {
    return prefs.getString(Const.PREF_LAST_SAVED_FILENAME, "");
  }

  public static void setLastSavedFileName(String value) {
    editor.putString(Const.PREF_LAST_SAVED_FILENAME, value).apply();
  }

  /*Boolean to check if app has been launched first time after installation , so we return true by default*/
  public static boolean getFirstLaunch() {
    return prefs.getBoolean(Const.PREF_FIRST_LAUNCH, true);
  }

  public static void setFirstLaunch(boolean value) {
    editor.putBoolean(Const.PREF_FIRST_LAUNCH, value).apply();
  }

  public static boolean getAmoledTheme() {
    return prefs.getBoolean(Const.PREF_AMOLED_THEME, false);
  }

  public static void setAmoledTheme(boolean value) {
    editor.putBoolean(Const.PREF_AMOLED_THEME, value).apply();
  }

  public static boolean getClear() {
    return prefs.getBoolean(Const.PREF_CLEAR, true);
  }

  public static void setClear(boolean value) {
    editor.putBoolean(Const.PREF_CLEAR, value).apply();
  }

  public static boolean getShareAndRun() {
    return prefs.getBoolean(Const.PREF_SHARE_AND_RUN, false);
  }

  public static void setShareAndRun(boolean value) {
    editor.putBoolean(Const.PREF_SHARE_AND_RUN, value).apply();
  }

  public static boolean getDisableSoftkey() {
    return prefs.getBoolean(Const.PREF_DISABLE_SOFTKEY, false);
  }

  public static void setDisableSoftkey(boolean value) {
    editor.putBoolean(Const.PREF_DISABLE_SOFTKEY, value).apply();
  }

  public static boolean getHapticsAndVibration() {
    return prefs.getBoolean(Const.PREF_HAPTICS_AND_VIBRATION, true);
  }

  public static void setHapticsAndVibration(boolean value) {
    editor.putBoolean(Const.PREF_HAPTICS_AND_VIBRATION, value).apply();
  }

  public static boolean getOverrideBookmarks() {
    return prefs.getBoolean(Const.PREF_OVERRIDE_BOOKMARKS, false);
  }

  public static void setOverrideBookmarks(boolean value) {
    editor.putBoolean(Const.PREF_OVERRIDE_BOOKMARKS, value).apply();
  }

  public static boolean getSmoothScroll() {
    return prefs.getBoolean(Const.PREF_SMOOTH_SCROLL, true);
  }

  public static void setSmoothScroll(boolean value) {
    editor.putBoolean(Const.PREF_SMOOTH_SCROLL, value).apply();
  }

  public static boolean getAutoUpdateCheck() {
    return prefs.getBoolean(Const.PREF_AUTO_UPDATE_CHECK, false);
  }

  public static void setAutoUpdateCheck(boolean value) {
    editor.putBoolean(Const.PREF_AUTO_UPDATE_CHECK, value).apply();
  }

  /* we need to check if the main activity is recreated (not restart) to perform certain tasks based on it */
  public static boolean getActivityRecreated() {
    return prefs.getBoolean(Const.PREF_ACTIVITY_RECREATED, false);
  }

  public static void setActivityRecreated(boolean value) {
    editor.putBoolean(Const.PREF_ACTIVITY_RECREATED, value).apply();
  }

  public static int getLocalAdbMode() {
    return prefs.getInt(Const.PREF_LOCAL_ADB_MODE, Const.BASIC_MODE);
  }

  public static void setLocalAdbMode(int value) {
    editor.putInt(Const.PREF_LOCAL_ADB_MODE, value).apply();
  }

  public static int getSavePreference() {
    return prefs.getInt(Const.PREF_SAVE_PREFERENCE, Const.LAST_COMMAND_OUTPUT);
  }

  public static void setSavePreference(int value) {
    editor.putInt(Const.PREF_SAVE_PREFERENCE, value).apply();
  }

  public static int getSavedVersionCode() {

    return prefs.getInt(Const.PREF_SAVED_VERSION_CODE, 1);
  }

  public static void setSavedVersionCode(int value) {
    editor.putInt(Const.PREF_SAVED_VERSION_CODE, value).apply();
  }

  public static int getSortingOption() {

    return prefs.getInt(Const.PREF_SORTING_OPTION, Const.SORT_A_TO_Z);
  }

  public static void setSortingOption(int value) {
    editor.putInt(Const.PREF_SORTING_OPTION, value).apply();
  }

  public static int getExamplesLayoutStyle() {
    return prefs.getInt(Const.PREF_EXAMPLES_LAYOUT_STYLE, Const.LIST_STYLE);
  }

  public static void setExamplesLayoutStyle(int value) {
    editor.putInt(Const.PREF_EXAMPLES_LAYOUT_STYLE, value).apply();
  }

  public static int getSortingExamples() {

    return prefs.getInt(Const.PREF_SORTING_EXAMPLES, Const.SORT_A_TO_Z);
  }

  public static void setSortingExamples(int value) {
    editor.putInt(Const.PREF_SORTING_EXAMPLES, value).apply();
  }

  public static void setLaunchMode(int value) {
    editor.putInt(Const.PREF_DEFAULT_LAUNCH_MODE, value).apply();
  }

  public static int getLaunchMode() {
    return prefs.getInt(Const.PREF_DEFAULT_LAUNCH_MODE, Const.MODE_LOCAL_ADB);
  }

  public static int getCurrentFragment() {
    return prefs.getInt(Const.PREF_CURRENT_FRAGMENT, Const.LOCAL_FRAGMENT);
  }

  public static void setCurrentFragment(int value) {
    editor.putInt(Const.PREF_CURRENT_FRAGMENT, value).apply();
  }

  public static int getUseCounter(String title) {
    return prefs.getInt(getCounterKey(title), 0);
  }

  public static void setUseCounter(String title, int counter) {
    editor.putInt(getCounterKey(title), counter).apply();
  }

  public static boolean getPinned(String title) {
    return prefs.getBoolean(getPinnedKey(title), false);
  }

  public static void setPinned(String title, boolean value) {
    editor.putBoolean(getPinnedKey(title), value).apply();
  }

  public static boolean getSpecificCardVisibility(String title) {
    return prefs.getBoolean(getCardKey(title), true);
  }

  public static void setSpecificCardVisibility(String title, boolean value) {
    editor.putBoolean(getCardKey(title), value).apply();
  }

  private static String getCounterKey(String title) {
    return Const.PREF_COUNTER_PREFIX + title;
  }

  private static String getPinnedKey(String title) {
    return Const.PREF_PINNED_PREFIX + title;
  }

  private static String getCardKey(String title) {
    return Const.PREF_SPECIFIC_CARD_VISIBILITY + title;
  }
}
