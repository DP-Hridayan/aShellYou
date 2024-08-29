package in.hridayan.ashell.config;

import in.hridayan.ashell.BuildConfig;

public interface Const {
  String SHARED_PREFS = BuildConfig.APPLICATION_ID + "_preferences";

  String DEV_EMAIL = "hridayanofficial@gmail.com";
  String PREF_FIRST_LAUNCH = "first_launch";
  String PREF_AMOLED_THEME = "id_amoled_theme";
  String PREF_COUNTER_PREFIX = "counter_";
  String PREF_PINNED_PREFIX = "pinned";
  String PREF_CLEAR = "id_clear";
  String PREF_SHARE_AND_RUN = "id_share_and_run";
  String PREF_DISABLE_SOFTKEY = "id_disable_softkey";
  String PREF_OVERRIDE_BOOKMARKS = "id_override_bookmarks";
  String PREF_SMOOTH_SCROLL = "id_smooth_scroll";
  String PREF_SAVED_VERSION_CODE = "saved_version_code";
  String PREF_SORTING_OPTION = "sorting_option";
  String PREF_SORTING_EXAMPLES = "sorting_examples";
  String PREF_CURRENT_FRAGMENT = "current_fragment";
  String PREF_DEFAULT_LAUNCH_MODE = "id_default_launch_mode";
  String PREF_SPECIFIC_CARD_VISIBILITY = "specific_card_visibility";
  String PREF_AUTO_UPDATE_CHECK = "id_auto_update_check";
  String PREF_SAVE_PREFERENCE = "id_save_preference";
  String PREF_LATEST_VERSION_NAME = "latest_version_name";
  String PREF_LAST_SAVED_FILENAME = "last_saved_filename";
  String PREF_HAPTICS_AND_VIBRATION = "id_vibration";
  String PREF_LOCAL_ADB_MODE = "id_local_adb_mode";
  String PREF_ACTIVITY_RECREATED = "activity_recreated";
  String PREF_EXAMPLES_LAYOUT_STYLE = "id_examples_layout_style";
  String PREF_OUTPUT_SAVE_DIRECTORY = "output_save_directory";
  String URL_BUILD_GRADLE =
      "https://raw.githubusercontent.com/DP-Hridayan/aShellYou/master/app/build.gradle";
  String URL_OTG_INSTRUCTIONS =
      "https://github.com/DP-Hridayan/aShellYou/blob/master/instructions/OTG.md";
  String URL_GITHUB_RELEASE = "https://github.com/DP-Hridayan/aShellYou/releases/latest";
  // used in OTG utils
  String TAG = "flashbot";

  int SORT_A_TO_Z = 0;
  int SORT_Z_TO_A = 1;
  int SORT_MOST_USED = 2;
  int SORT_OLDEST = 2;
  int SORT_NEWEST = 3;
  int SORT_LEAST_USED = 3;
  int LOCAL_FRAGMENT = 0;
  int OTG_FRAGMENT = 1;
  int SETTINGS_FRAGMENT = 2;
  int ABOUT_FRAGMENT = 3;
  int EXAMPLES_FRAGMENT = 4;
  int CHANGELOG_FRAGMENT = 5;
  int MODE_LOCAL_ADB = 0;
  int MODE_OTG = 1;
  int MODE_REMEMBER_LAST_MODE = 2;
  int MAX_BOOKMARKS_LIMIT = 25;
  int UPDATE_AVAILABLE = 1;
  int UPDATE_NOT_AVAILABLE = 0;
  int CONNECTION_ERROR = 2;
  int LAST_COMMAND_OUTPUT = 0;
  int ALL_OUTPUT = 1;
  int BASIC_MODE = 0;
  int SHIZUKU_MODE = 1;
  int ROOT_MODE = 2;
  int LIST_STYLE = 1;
  int GRID_STYLE = 2;

  // used in OTG utils
  double PUSH_PERCENT = 0.5;
}
