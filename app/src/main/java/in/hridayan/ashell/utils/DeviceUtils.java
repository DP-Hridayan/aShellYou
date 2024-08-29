package in.hridayan.ashell.utils;

import android.content.Context;
import android.os.Build;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.config.Preferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceUtils {
  public static int savedVersionCode;

  // Method for getting required device details for crash report
  public static String getDeviceDetails() {
    return "\n"
        + "Brand : "
        + Build.BRAND
        + "\n"
        + "Device : "
        + Build.DEVICE
        + "\n"
        + "Model : "
        + Build.MODEL
        + "\n"
        + "Product : "
        + Build.PRODUCT
        + "\n"
        + "SDK : "
        + Build.VERSION.SDK_INT
        + "\n"
        + "Release : "
        + Build.VERSION.RELEASE
        + "\n"
        + "App version name : "
        + BuildConfig.VERSION_NAME
        + "\n"
        + "App version code : "
        + BuildConfig.VERSION_CODE;
  }

  // returns android sdk version
  public static int androidVersion() {
    return Build.VERSION.SDK_INT;
  }

  // returns device model name
  public static String getDeviceName() {
    return Build.MODEL;
  }

  // returns current (installed) app version code
  public static int currentVersion() {
    return BuildConfig.VERSION_CODE;
  }

  /* returns if app has just been updated , used to perform certain things after opening the app after an update */
  public static boolean isAppUpdated(Context context) {
    savedVersionCode = Preferences.getSavedVersionCode();
    return savedVersionCode != currentVersion() && savedVersionCode != 1;
  }

  /* Compare current app version code with the one retrieved from github to see if update available */
  public static boolean isUpdateAvailable(int latestVersionCode) {
    return BuildConfig.VERSION_CODE < latestVersionCode;
  }

  public static interface FetchLatestVersionCodeCallback {
    void onResult(int result);
  }

  // Extracts the version code from the build.gradle file retrieved and converts it to integer
  public static int extractVersionCode(String text) {
    Pattern pattern = Pattern.compile("versionCode\\s+(\\d+)");
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      try {
        return Integer.parseInt(matcher.group(1));
      } catch (NumberFormatException e) {
        e.printStackTrace();
        return -1;
      }
    }
    return -1;
  }

  // Extracts the version name from the build.gradle file retrieved and converts it to string
  public static String extractVersionName(String text) {
    Pattern pattern = Pattern.compile("versionName\\s*\"([^\"]*)\"");
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) return matcher.group(1);

    return "";
  }

  /*Using this function to create unique file names for the saved txt files as there are methods which tries to open files based on its name */
  public static String getCurrentDateTime() {
    // Thread-safe date format
    ThreadLocal<SimpleDateFormat> threadLocalDateFormat =
        new ThreadLocal<SimpleDateFormat>() {
          @Override
          protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("_yyyyMMddHHmmss");
          }
        };

    // Get the current date and time
    Date now = new Date();

    // Format the date and time using the thread-local formatter
    return threadLocalDateFormat.get().format(now);
  }
}
