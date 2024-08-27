package in.hridayan.ashell.utils;

import static in.hridayan.ashell.utils.Preferences.SORT_A_TO_Z;
import static in.hridayan.ashell.utils.Preferences.SORT_NEWEST;
import static in.hridayan.ashell.utils.Preferences.SORT_OLDEST;
import static in.hridayan.ashell.utils.Preferences.SORT_Z_TO_A;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ToastUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
  public static Intent intent;
  public static int savedVersionCode;

  private static boolean isValidFilename(String s) {

    String[] invalidChars = {"*", "/", ":", "<", ">", "?", "\\", "|"};

    for (String invalidChar : invalidChars) {
      if (s.contains(invalidChar)) return false;
    }
    return true;
  }

  public static Drawable getDrawable(int drawable, Context context) {
    return ContextCompat.getDrawable(context, drawable);
  }

  public static int getColor(int color, Context context) {
    return ContextCompat.getColor(context, color);
  }

  public static Snackbar snackBar(View view, String message) {
    Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
    snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
    return snackbar;
  }

  public static int androidVersion() {
    return Build.VERSION.SDK_INT;
  }

  public static String getDeviceName() {
    return Build.MODEL;
  }

  private static String read(File file) {
    BufferedReader buf = null;
    try {
      buf = new BufferedReader(new FileReader(file));

      StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = buf.readLine()) != null) {
        stringBuilder.append(line).append("\n");
      }
      return stringBuilder.toString().trim();
    } catch (IOException ignored) {
    } finally {
      try {
        if (buf != null) buf.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static void copyToClipboard(String text, Context context) {
    ClipboardManager clipboard =
        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText(context.getString(R.string.copied_to_clipboard), text);
    clipboard.setPrimaryClip(clip);
    ToastUtils.showToast(context, R.string.copied_to_clipboard, ToastUtils.LENGTH_SHORT);
  }

  public static void create(String text, File path) {
    try {
      FileWriter writer = new FileWriter(path);
      writer.write(text);
      writer.close();
    } catch (IOException ignored) {
    }
  }

  public static void openUrl(Context context, String url) {
    try {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(url));
      context.startActivity(intent);
    } catch (ActivityNotFoundException ignored) {
    }
  }

  public static void pasteFromClipboard(TextInputEditText editText) {
    if (editText == null) return;

    ClipboardManager clipboard =
        (ClipboardManager) editText.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

    if (clipboard == null) return;

    if (clipboard.hasPrimaryClip()
        && clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
      ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
      if (item != null && item.getText() != null) {
        String clipboardText = item.getText().toString();
        editText.setText(clipboardText);
        editText.setSelection(editText.getText().length());
      }
    } else {
      ToastUtils.showToast(
          editText.getContext().getApplicationContext(),
          R.string.clipboard_empty,
          ToastUtils.LENGTH_SHORT);
    }
  }

  public static void alignMargin(View component) {
    ViewGroup.MarginLayoutParams params =
        (ViewGroup.MarginLayoutParams) component.getLayoutParams();
    params.bottomMargin = 29;
    component.setLayoutParams(params);
    component.requestLayout();
  }

  public static boolean isToolbarExpanded(AppBarLayout appBarLayout) {
    return appBarLayout.getTop() == 0;
  }

  public static int recyclerViewPosition(RecyclerView recyclerView) {
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

    return firstVisibleItemPosition;
  }

  public static int currentVersion() {
    return BuildConfig.VERSION_CODE;
  }

  public static boolean isAppUpdated(Context context) {
    savedVersionCode = Preferences.getSavedVersionCode(context);
    return savedVersionCode != currentVersion() && savedVersionCode != 1;
  }

  public static List<String> getBookmarks(Context context) {
    List<String> mBookmarks = new ArrayList<>();

    // Get the bookmarks directory
    File bookmarksDir = context.getExternalFilesDir("bookmarks");

    // Check if the directory is null or empty
    if (bookmarksDir == null || bookmarksDir.listFiles() == null)
      // Return empty list if bookmarks directory is null or has no files
      return mBookmarks;

    // Add bookmark files to the list
    for (File file : bookmarksDir.listFiles()) {
      if (!file.getName().equalsIgnoreCase("specialCommands")) mBookmarks.add(file.getName());
    }

    // Handle specialCommands file
    File specialCommandsFile = new File(bookmarksDir, "specialCommands");
    if (specialCommandsFile.exists()) {
      String fileContent = read(specialCommandsFile);
      if (fileContent != null) {
        String[] commands = fileContent.split("\\r?\\n");
        for (String command : commands) {
          if (!command.trim().isEmpty()) mBookmarks.add(command.trim());
        }
      }
    }

    switch (Preferences.getSortingOption(context)) {
      case SORT_A_TO_Z:
        Collections.sort(mBookmarks);
        break;
      case SORT_Z_TO_A:
        Collections.sort(mBookmarks, Collections.reverseOrder());
        break;
      case SORT_NEWEST:
        break;
      case SORT_OLDEST:
        Collections.reverse(mBookmarks);
        break;
    }

    return mBookmarks;
  }

  public static boolean isBookmarked(String command, Context context) {
    if (isValidFilename(command))
      return new File(context.getExternalFilesDir("bookmarks"), command).exists();
    else {
      if (new File(context.getExternalFilesDir("bookmarks"), "specialCommands").exists()) {
        for (String commands :
            Objects.requireNonNull(
                    read(new File(context.getExternalFilesDir("bookmarks"), "specialCommands")))
                .split("\\r?\\n")) {
          if (commands.trim().equals(command)) return true;
        }
      }
    }
    return false;
  }

  public static void addToBookmark(String command, Context context) {
    if (isValidFilename(command))
      create(command, new File(context.getExternalFilesDir("bookmarks"), command));
    else {
      StringBuilder sb = new StringBuilder();
      if (new File(context.getExternalFilesDir("bookmarks"), "specialCommands").exists()) {
        for (String commands :
            Objects.requireNonNull(
                    read(new File(context.getExternalFilesDir("bookmarks"), "specialCommands")))
                .split("\\r?\\n")) {
          sb.append(commands).append("\n");
        }
        sb.append(command).append("\n");
      } else sb.append(command).append("\n");

      create(sb.toString(), new File(context.getExternalFilesDir("bookmarks"), "specialCommands"));
    }
  }

  public static boolean deleteFromBookmark(String command, Context context) {
    if (isValidFilename(command))
      return new File(context.getExternalFilesDir("bookmarks"), command).delete();
    else {
      StringBuilder sb = new StringBuilder();
      for (String commands :
          Objects.requireNonNull(
                  read(new File(context.getExternalFilesDir("bookmarks"), "specialCommands")))
              .split("\\r?\\n")) {
        if (!commands.equals(command)) sb.append(commands).append("\n");
      }
      create(sb.toString(), new File(context.getExternalFilesDir("bookmarks"), "specialCommands"));
      return true;
    }
  }

  public static void addBookmarkIconOnClickListener(String bookmark, View view, Context context) {
    HapticUtils.weakVibrate(view, context);
    boolean switchState = Preferences.getOverrideBookmarks(context);

    if (Utils.getBookmarks(context).size() <= Preferences.MAX_BOOKMARKS_LIMIT - 1 || switchState) {
      Utils.addToBookmark(bookmark, context);
      Utils.snackBar(view, context.getString(R.string.bookmark_added_message, bookmark)).show();
    } else Utils.snackBar(view, context.getString(R.string.bookmark_limit_reached)).show();
  }

  /*------------------------------------------------------*/

  public static float convertDpToPixel(float dp, Context context) {
    float scale = context.getResources().getDisplayMetrics().density;
    return dp * scale + 0.5f;
  }

  /* Generate the file name of the exported txt file . The name will be the last executed command. It gets the last executed command from the History List */
  public static String generateFileName(List<String> mHistory) {
    return mHistory.get(mHistory.size() - 1).replace("/", "-").replace(" ", "") + ".txt";
  }

  public static String lastCommandOutput(String text) {
    int lastDollarIndex = text.lastIndexOf('$');
    if (lastDollarIndex == -1)
      throw new IllegalArgumentException("Text must contain at least one '$' symbol");

    int secondLastDollarIndex = text.lastIndexOf('$', lastDollarIndex - 1);
    if (secondLastDollarIndex == -1)
      throw new IllegalArgumentException("Text must contain at least two '$' symbols");

    // Find the start of the line containing the first '$' of the last two
    int startOfFirstLine = text.lastIndexOf('\n', secondLastDollarIndex) + 1;
    // Find the start of the line containing the second '$' of the last two
    int startOfSecondLine = text.lastIndexOf('\n', lastDollarIndex - 1) + 1;
    if (startOfSecondLine == -1)
      startOfSecondLine = 0; // If there's no newline before, start from the beginning of the text

    return text.substring(startOfFirstLine, startOfSecondLine);
  }

  // Logic behind saving output as txt files
  public static boolean saveToFile(String sb, Activity activity, String fileName) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
      return Utils.saveToFileApi29AndAbove(sb, activity, fileName);
    else return Utils.saveToFileBelowApi29(sb, activity, fileName);
  }

  /* Save output txt file on devices running Android 11 and above and return a boolean if the file is saved */
  public static boolean saveToFileApi29AndAbove(String sb, Activity activity, String fileName) {
    try {
      ContentValues values = new ContentValues();
      values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
      values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
      values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
      Uri uri =
          activity.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

      if (uri != null) {
        try (OutputStream outputStream = activity.getContentResolver().openOutputStream(uri)) {
          outputStream.write(sb.toString().getBytes());
          return true;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /*Save output txt file on devices running Android 10 and below and return a boolean if the file is saved */
  public static boolean saveToFileBelowApi29(String sb, Activity activity, String fileName) {
    if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          activity, new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
      return false;
    }

    try {
      File file = new File(Environment.DIRECTORY_DOWNLOADS, fileName);
      Utils.create(sb.toString(), file);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  // Method for sharing output to other apps
  public static void shareOutput(Activity activity, Context context, String fileName, String sb) {
    try {
      File file = new File(activity.getCacheDir(), fileName);
      FileOutputStream outputStream = new FileOutputStream(file);
      outputStream.write(sb.getBytes());
      outputStream.close();

      Uri fileUri =
          FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      activity.startActivity(Intent.createChooser(shareIntent, "Share File"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Method to load the changelogs text
  public static String loadChangelogText(String versionNumber, Context context) {
    int resourceId =
        context
            .getResources()
            .getIdentifier(
                "changelog_" + versionNumber.replace(".", "_"), "string", context.getPackageName());

    // Check if the resource ID is valid
    if (resourceId != 0) {
      String changeLog = context.getString(resourceId);
      // Check if the resource is not empty
      if (changeLog != null && !changeLog.isEmpty()) return changeLog;
    }

    // Return default message if resource ID is invalid or the resource is empty
    return context.getString(R.string.no_changelog);
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

  /* Compare current app version code with the one retrieved from github to see if update available */
  public static boolean isUpdateAvailable(int latestVersionCode) {
    return BuildConfig.VERSION_CODE < latestVersionCode;
  }

  public static interface FetchLatestVersionCodeCallback {
    void onResult(int result);
  }

  // Method to convert List to String (for shizuku shell output)
  public static String convertListToString(List<String> list) {
    StringBuilder sb = new StringBuilder();
    for (String s : list) {
      if (!Utils.shellDeadError().equals(s) && !"<i></i>".equals(s)) sb.append(s).append("\n");
    }
    return sb.toString();
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

  // Method to open the text file
  public static void openTextFileWithIntent(String fileName, Context context) {
    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    File file = new File(directory, fileName);

    if (file.exists()) {
      Uri fileUri = FileProvider.getUriForFile(context, "in.hridayan.ashell.fileprovider", file);
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(fileUri, "text/plain");
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      try {
        context.startActivity(intent);
      } catch (ActivityNotFoundException e) {
        Toast.makeText(
                context, context.getString(R.string.no_application_found), Toast.LENGTH_SHORT)
            .show();
      }
    } else {
      Toast.makeText(context, context.getString(R.string.file_not_found), Toast.LENGTH_SHORT)
          .show();
    }
  }

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

  // String that shows Shell is dead in red colour
  public static String shellDeadError() {
    return "<font color=#FF0000>" + "Shell is dead" + "</font>";
  }

  public static boolean isNightMode(Context context) {
    return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
        == Configuration.UI_MODE_NIGHT_YES;
  }
}
