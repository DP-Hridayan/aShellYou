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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.R;
import in.hridayan.ashell.activities.ChangelogActivity;
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
import rikka.shizuku.Shizuku;

public class Utils {
  public static Intent intent;
  public static int savedVersionCode;

  /*
   * Adapted from android.os.FileUtils
   * Ref: https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/os/FileUtils.java;l=972?q=isValidFatFilenameChar
   */

  private static boolean isValidFilename(String s) {
    return !s.contains("*")
        && !s.contains("/")
        && !s.contains(":")
        && !s.contains("<")
        && !s.contains(">")
        && !s.contains("?")
        && !s.contains("\\")
        && !s.contains("|");
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
    Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT)
        .show();
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
    if (editText == null) {
      return;
    }
    ClipboardManager clipboard =
        (ClipboardManager) editText.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard == null) {
      return;
    }

    if (clipboard.hasPrimaryClip()
        && clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
      ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
      if (item != null && item.getText() != null) {
        String clipboardText = item.getText().toString();
        editText.setText(clipboardText);
        editText.setSelection(editText.getText().length());
      }
    } else {
      Toast.makeText(
              editText.getContext().getApplicationContext(),
              editText.getContext().getString(R.string.clipboard_empty),
              Toast.LENGTH_SHORT)
          .show();
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

  public static void expandToolbar(AppBarLayout appBarLayout) {
    appBarLayout.setExpanded(true);
  }

  public static void collapseToolbar(AppBarLayout appBarLayout) {
    appBarLayout.setExpanded(false);
  }

  public static int recyclerViewPosition(RecyclerView recyclerView) {
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

    return firstVisibleItemPosition;
  }

  public static int currentVersion() {
    int versionCode = BuildConfig.VERSION_CODE;
    return versionCode;
  }

  public static boolean isAppUpdated(Context context) {
    savedVersionCode = Preferences.getSavedVersionCode(context);
    if (savedVersionCode != currentVersion() && savedVersionCode != 1) {
      return true;
    } else {
      return false;
    }
  }

  /*---------------------Bookmarks section------------------------*/

  public static List<String> getBookmarks(Context context) {
    List<String> mBookmarks = new ArrayList<>();
    for (File file : Objects.requireNonNull(context.getExternalFilesDir("bookmarks").listFiles())) {
      if (!file.getName().equalsIgnoreCase("specialCommands")) {
        mBookmarks.add(file.getName());
      }
    }
    if (new File(context.getExternalFilesDir("bookmarks"), "specialCommands").exists()) {
      for (String commands :
          Objects.requireNonNull(
                  read(new File(context.getExternalFilesDir("bookmarks"), "specialCommands")))
              .split("\\r?\\n")) {
        if (!commands.trim().isEmpty()) {
          mBookmarks.add(commands.trim());
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
    if (isValidFilename(command)) {
      return new File(context.getExternalFilesDir("bookmarks"), command).exists();
    } else {
      if (new File(context.getExternalFilesDir("bookmarks"), "specialCommands").exists()) {
        for (String commands :
            Objects.requireNonNull(
                    read(new File(context.getExternalFilesDir("bookmarks"), "specialCommands")))
                .split("\\r?\\n")) {
          if (commands.trim().equals(command)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static void addToBookmark(String command, Context context) {
    if (isValidFilename(command)) {
      create(command, new File(context.getExternalFilesDir("bookmarks"), command));
    } else {
      StringBuilder sb = new StringBuilder();
      if (new File(context.getExternalFilesDir("bookmarks"), "specialCommands").exists()) {
        for (String commands :
            Objects.requireNonNull(
                    read(new File(context.getExternalFilesDir("bookmarks"), "specialCommands")))
                .split("\\r?\\n")) {
          sb.append(commands).append("\n");
        }
        sb.append(command).append("\n");
      } else {
        sb.append(command).append("\n");
      }
      create(sb.toString(), new File(context.getExternalFilesDir("bookmarks"), "specialCommands"));
    }
  }

  public static boolean deleteFromBookmark(String command, Context context) {
    if (isValidFilename(command)) {
      return new File(context.getExternalFilesDir("bookmarks"), command).delete();
    } else {
      StringBuilder sb = new StringBuilder();
      for (String commands :
          Objects.requireNonNull(
                  read(new File(context.getExternalFilesDir("bookmarks"), "specialCommands")))
              .split("\\r?\\n")) {
        if (!commands.equals(command)) {
          sb.append(commands).append("\n");
        }
      }
      create(sb.toString(), new File(context.getExternalFilesDir("bookmarks"), "specialCommands"));
      return true;
    }
  }

  public static void bookmarksDialog(
      Context context,
      Activity activity,
      TextInputEditText mCommand,
      TextInputLayout mCommandInput,
      MaterialButton button) {

    List<String> bookmarks = Utils.getBookmarks(activity);

    int totalBookmarks = bookmarks.size();

    String title = context.getString(R.string.bookmarks) + " (" + totalBookmarks + ")";

    CharSequence[] bookmarkItems = new CharSequence[bookmarks.size()];
    for (int i = 0; i < bookmarks.size(); i++) {
      bookmarkItems[i] = bookmarks.get(i);
    }

    new MaterialAlertDialogBuilder(activity)
        .setTitle(title)
        .setItems(
            bookmarkItems,
            (dialog, which) -> {
              mCommand.setText(bookmarks.get(which));
              mCommand.setSelection(mCommand.getText().length());
            })
        .setPositiveButton(context.getString(R.string.cancel), (dialogInterface, i) -> {})
        .setNegativeButton(
            context.getString(R.string.sort),
            (dialogInterface, i) -> {
              Utils.sortingDialog(context, activity, mCommand, mCommandInput, button);
            })
        .setNeutralButton(
            context.getString(R.string.delete_all),
            (DialogInterface, i) -> {
              Utils.deleteDialog(context, activity, mCommand, mCommandInput, button);
            })
        .show();
  }

  public static void deleteDialog(
      Context context,
      Activity activity,
      TextInputEditText mCommand,
      TextInputLayout mCommandInput,
      MaterialButton button) {

    new MaterialAlertDialogBuilder(activity)
        .setTitle(context.getString(R.string.confirm_delete))
        .setMessage(context.getString(R.string.confirm_delete_message))
        .setPositiveButton(
            context.getString(R.string.ok),
            (dialogInterface, i) -> {
              List<String> bookmarks = Utils.getBookmarks(activity);
              for (String item : bookmarks) {
                Utils.deleteFromBookmark(item, context);
              }
              button.setVisibility(View.GONE);
              String s = mCommand.getText().toString();
              if (!s.equals("")) {
                mCommandInput.setEndIconDrawable(R.drawable.ic_add_bookmark);
              } else {
                mCommandInput.setEndIconVisible(false);
              }
            })
        .setNegativeButton(
            context.getString(R.string.cancel),
            (dialogInterface, i) -> {
              Utils.bookmarksDialog(context, activity, mCommand, mCommandInput, button);
            })
        .setOnCancelListener(
            v -> {
              List<String> bookmarks = Utils.getBookmarks(activity);
              if (bookmarks.size() != 0) {
                Utils.bookmarksDialog(context, activity, mCommand, mCommandInput, button);
              }
            })
        .show();
  }

  public static void sortingDialog(
      Context context,
      Activity activity,
      TextInputEditText mCommand,
      TextInputLayout mCommandInput,
      MaterialButton button) {
    CharSequence[] sortingOptions = {
      context.getString(R.string.sort_A_Z),
      context.getString(R.string.sort_Z_A),
      context.getString(R.string.sort_newest),
      context.getString(R.string.sort_oldest)
    };
    int currentSortingOption = Preferences.getSortingOption(context);

    final int[] sortingOption = {currentSortingOption};

    new MaterialAlertDialogBuilder(activity)
        .setTitle(context.getString(R.string.sort))
        .setSingleChoiceItems(
            sortingOptions,
            currentSortingOption,
            (dialog, which) -> {
              sortingOption[0] = which;
            })
        .setPositiveButton(
            context.getString(R.string.ok),
            (dialog, which) -> {
              Preferences.setSortingOption(context, sortingOption[0]);
              Utils.bookmarksDialog(context, activity, mCommand, mCommandInput, button);
            })
        .setNegativeButton(
            context.getString(R.string.cancel),
            (dialog, i) -> {
              Utils.bookmarksDialog(context, activity, mCommand, mCommandInput, button);
            })
        .setOnCancelListener(
            v -> {
              Utils.bookmarksDialog(context, activity, mCommand, mCommandInput, button);
            })
        .show();
  }

  public static void addBookmarkIconOnClickListener(String bookmark, View view, Context context) {
    boolean switchState = Preferences.getOverrideBookmarks(context);

    if (Utils.getBookmarks(context).size() <= Preferences.MAX_BOOKMARKS_LIMIT - 1 || switchState) {
      Utils.addToBookmark(bookmark, context);
      Utils.snackBar(view, context.getString(R.string.bookmark_added_message, bookmark)).show();
    } else {
      Utils.snackBar(view, context.getString(R.string.bookmark_limit_reached)).show();
    }
  }

  /*------------------------------------------------------*/

  public static void defaultWorkingModeDialog(Context context) {

    final CharSequence[] workingModes = {
      context.getString(R.string.local_adb),
      context.getString(R.string.otg),
      context.getString(R.string.remember_working_mode)
    };

    int defaultWorkingMode = Preferences.getWorkingMode(context);
    final int[] workingMode = {defaultWorkingMode};

    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.working_mode))
        .setSingleChoiceItems(
            workingModes,
            defaultWorkingMode,
            (dialog, which) -> {
              workingMode[0] = which;
            })
        .setPositiveButton(
            context.getString(R.string.choose),
            (dialog, which) -> {
              Preferences.setWorkingMode(context, workingMode[0]);
            })
        .setNegativeButton(context.getString(R.string.cancel), (dialog, i) -> {})
        .show();
  }

  public static void connectedDeviceDialog(Context context, String connectedDevice) {
    String device = connectedDevice;

    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.connected_device))
        .setMessage(device)
        .show();
  }

  public static void chipOnClickListener(Context context, Chip mChip, String device) {
    mChip.setOnClickListener(
        v -> {
          boolean hasShizuku =
              Shizuku.pingBinder()
                  && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
          Utils.connectedDeviceDialog(
              context, hasShizuku ? device : context.getString(R.string.none));
          mChip.setChecked(!mChip.isChecked());
        });
  }

  public static float convertDpToPixel(float dp, Context context) {
    float scale = context.getResources().getDisplayMetrics().density;
    return dp * scale + 0.5f;
  }

  // Dialog to show if the shell output is saved or not
  public static void outputSavedDialog(Activity activity, Context context, boolean saved) {
    String message =
        saved
            ? context.getString(
                R.string.shell_output_saved_message, Environment.DIRECTORY_DOWNLOADS)
            : context.getString(R.string.shell_output_not_saved_message);
    String title = saved ? context.getString(R.string.success) : context.getString(R.string.failed);

    new MaterialAlertDialogBuilder(activity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(context.getString(R.string.cancel), (dialogInterface, i) -> {})
        .show();
  }

  // Generate the file name of the exported txt file
  public static String generateFileName(List<String> mHistory) {
    return mHistory.get(mHistory.size() - 1).replace("/", "-").replace(" ", "") + ".txt";
  }

  public static String lastCommandOutput(String text) {
    int lastDollarIndex = text.lastIndexOf('$');
    if (lastDollarIndex == -1) {
      throw new IllegalArgumentException("Text must contain at least one '$' symbol");
    }

    int secondLastDollarIndex = text.lastIndexOf('$', lastDollarIndex - 1);
    if (secondLastDollarIndex == -1) {
      throw new IllegalArgumentException("Text must contain at least two '$' symbols");
    }

    // Find the start of the line containing the first '$' of the last two
    int startOfFirstLine = text.lastIndexOf('\n', secondLastDollarIndex) + 1;
    // Find the start of the line containing the second '$' of the last two
    int startOfSecondLine = text.lastIndexOf('\n', lastDollarIndex - 1) + 1;
    if (startOfSecondLine == -1) {
      startOfSecondLine = 0; // If there's no newline before, start from the beginning of the text
    }
    return text.substring(startOfFirstLine, startOfSecondLine);
  }

  // Logic behind saving output as txt files
  public static boolean saveToFile(String sb, Activity activity, List<String> mHistory) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return Utils.saveToFileApi29AndAbove(sb, activity, mHistory);
    } else {
      return Utils.saveToFileBelowApi29(sb, activity, mHistory);
    }
  }

  /* Save output txt file on devices running Android 11 and above and return a boolean if the file is saved */
  public static boolean saveToFileApi29AndAbove(
      String sb, Activity activity, List<String> mHistory) {
    try {
      ContentValues values = new ContentValues();
      String fileName = Utils.generateFileName(mHistory);
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
  public static boolean saveToFileBelowApi29(String sb, Activity activity, List<String> mHistory) {
    if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          activity, new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
      return false;
    }

    try {
      String fileName = Utils.generateFileName(mHistory);
      File file = new File(Environment.DIRECTORY_DOWNLOADS, fileName);
      Utils.create(sb.toString(), file);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  // Method for sharing output to other apps
  public static void shareOutput(
      Activity activity, Context context, List<String> mHistory, String sb) {
    try {
      String fileName = Utils.generateFileName(mHistory);

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

  // Method to retrieve the app version name
  public static String getAppVersionName(Context context) {
    String versionName = "";
    try {
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
      versionName = packageInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return versionName;
  }

  // Method to load the changelogs text
  public static String loadChangelogText(String versionNumber, Context context) {
    int resourceId =
        context
            .getResources()
            .getIdentifier(
                "changelog_" + versionNumber.replace(".", "_"), "string", context.getPackageName());
    return context.getResources().getString(resourceId);
  }
}
