package in.hridayan.ashell.utils;

import static in.hridayan.ashell.config.Const.SORT_A_TO_Z;
import static in.hridayan.ashell.config.Const.SORT_NEWEST;
import static in.hridayan.ashell.config.Const.SORT_OLDEST;
import static in.hridayan.ashell.config.Const.SORT_Z_TO_A;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Utils {

  /* <--------FILE ACTIONS -------> */

  // Generate the filename
  public static String generateFileName(List<String> mHistory) {
    return mHistory.get(mHistory.size() - 1).replace("/", "-").replace(" ", "");
  }

  // Checks if filename is valid
  private static boolean isValidFilename(String s) {

    String[] invalidChars = {"*", "/", ":", "<", ">", "?", "\\", "|"};

    for (String invalidChar : invalidChars) {
      if (s.contains(invalidChar)) return false;
    }
    return true;
  }

  // Reads a file
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

  // Create a file at the path
  public static void create(String text, File path) {
    try {
      FileWriter writer = new FileWriter(path);
      writer.write(text);
      writer.close();
    } catch (IOException ignored) {
    }
  }

  // Logic behind saving output as txt files
  public static boolean saveToFile(String sb, Activity activity, String fileName) {
    String treeUri = Preferences.getSavedOutputDir();
    if (!treeUri.isEmpty()) {
      return saveToCustomDirectory(sb, activity, fileName, treeUri);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
      return Utils.saveToFileApi29AndAbove(sb, activity, fileName);
    else return Utils.saveToFileBelowApi29(sb, activity, fileName);
  }

  public static boolean saveToCustomDirectory(
      String sb, Activity activity, String fileName, String treeUri) {

    Uri uri = Uri.parse(treeUri);

    DocumentFile documentFile = DocumentFile.fromTreeUri(activity, uri);

    if (documentFile == null) {
      Log.w("Utils", "DocumentFile is null");
      return false;
    }
    DocumentFile file = documentFile.createFile("text/plain", fileName);

    if (file == null) {
      Log.w("Utils", "File is null");
      return false;
    }
    Uri fileUri = file.getUri();
    try (OutputStream outputStream = activity.getContentResolver().openOutputStream(fileUri)) {
      outputStream.write(sb.getBytes());
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
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

  // Method to open the text file
  public static void openTextFileWithIntent(String fileName, Context context) {
    String savedOutputDir = Preferences.getSavedOutputDir();
    File file = null;

    // Check if a custom directory is set in preferences
    if (!savedOutputDir.isEmpty()) {
      String customDirPath =
          DocumentTreeUtil.getFullPathFromTreeUri(Uri.parse(savedOutputDir), context);
      if (customDirPath != null) {
        System.out.println(customDirPath);
        file = new File(customDirPath, fileName);
      }
    }

    if (file == null || !file.exists()) {
      File downloadsDir =
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      file = new File(downloadsDir, fileName);
    }

    if (file.exists()) {
      Uri fileUri =
          FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
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

  /* <--------CLIPBOARD ACTIONS -------> */

  // Copy the text on the clipboard
  public static void copyToClipboard(String text, Context context) {
    ClipboardManager clipboard =
        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText(context.getString(R.string.copied_to_clipboard), text);
    clipboard.setPrimaryClip(clip);
    ToastUtils.showToast(context, R.string.copied_to_clipboard, ToastUtils.LENGTH_SHORT);
  }

  // Paste text from clipboard
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

  /* <-------- BOOKMARK ACTIONS -------> */

  // Return the bookmarks list
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

    // Sort them according to preference
    switch (Preferences.getSortingOption()) {
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

  // Checks if the command is already bookmarked
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

  // Add the command to bookmarks
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

  // Delete the command from bookmarks
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

  /* Onclick listener of the add bookmarks icon ( It is the icon in the text input field that allows adding or removing from bookmarks) */
  public static void addBookmarkIconOnClickListener(String bookmark, View view, Context context) {
    HapticUtils.weakVibrate(view);
    boolean switchState = Preferences.getOverrideBookmarks();

    // if current bookmarks is less than limit add it else show a toast
    if (Utils.getBookmarks(context).size() <= Const.MAX_BOOKMARKS_LIMIT - 1 || switchState) {
      Utils.addToBookmark(bookmark, context);
      Utils.snackBar(view, context.getString(R.string.bookmark_added_message, bookmark)).show();
    } else Utils.snackBar(view, context.getString(R.string.bookmark_limit_reached)).show();
  }

  public static Drawable getDrawable(int drawable, Context context) {
    return ContextCompat.getDrawable(context, drawable);
  }

  public static Snackbar snackBar(View view, String message) {
    Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
    snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
    return snackbar;
  }

  public static void openUrl(Context context, String url) {
    try {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(url));
      context.startActivity(intent);
    } catch (ActivityNotFoundException ignored) {
    }
  }

  // returns if the app toolbar is expanded
  public static boolean isToolbarExpanded(AppBarLayout appBarLayout) {
    return appBarLayout.getTop() == 0;
  }

  public static int recyclerViewPosition(RecyclerView recyclerView) {
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

    return firstVisibleItemPosition;
  }

  public static float convertDpToPixel(float dp, Context context) {
    float scale = context.getResources().getDisplayMetrics().density;
    return dp * scale + 0.5f;
  }

  /* Generate the file name of the exported txt file . The name will be the last executed command. It gets the last executed command from the History List */

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

  // Method to convert List to String (for shizuku shell output)
  public static String convertListToString(List<String> list) {
    StringBuilder sb = new StringBuilder();
    for (String s : list) {
      if (!Utils.shellDeadError().equals(s) && !"<i></i>".equals(s)) sb.append(s).append("\n");
    }
    return sb.toString();
  }

  // String that shows Shell is dead in red colour
  public static String shellDeadError() {
    return "<font color=#FF0000>" + "Shell is dead" + "</font>";
  }

  // start animation of animated drawable
  public static void startAnim(Drawable icon) {
    if (icon instanceof AnimatedVectorDrawable) {
      AnimatedVectorDrawable animatedVector = (AnimatedVectorDrawable) icon;
      animatedVector.stop();
      animatedVector.start();
    }
  }

  // checks if device is connected to wifi
  public static boolean isConnectedToWifi(Context context) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    if (wifiManager == null) return false;

    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

    // Check if connected to a WiFi network (even if no internet)
    if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
      return true;
    }

    // Extra check for Android 10+ to detect WiFi when mobile data is on
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ConnectivityManager connectivityManager =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connectivityManager == null) return false;

      Network activeNetwork = connectivityManager.getActiveNetwork();
      if (activeNetwork == null) return false;

      NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
      if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
        return true;
      }
    }

    return false;
  }

  public static void askUserToEnableWifi(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      // Android 10+ (API 29+): Show system WiFi enable popup
      Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
      context.startActivity(panelIntent);
    } else {
      // Android 9 and below: Enable WiFi directly (Needs CHANGE_WIFI_STATE permission)
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      if (wifiManager != null && !wifiManager.isWifiEnabled()) {
        wifiManager.setWifiEnabled(true);
      }
    }
  }
}
