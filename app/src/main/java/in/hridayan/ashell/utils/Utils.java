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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

  public static void isAppUpdated(Context context, Activity activity) {
    CoordinatorLayout view = activity.findViewById(R.id.fragment_container);
    savedVersionCode = Preferences.getSavedVersionCode(context);
    if (savedVersionCode != currentVersion() && savedVersionCode != 1) {
      Utils.snackBar(view, context.getString(R.string.app_updated_message))
          .setAction(
              context.getString(R.string.yes),
              (v -> {
                intent = new Intent(context, ChangelogActivity.class);
                context.startActivity(intent);
              }))
          .show();
    }
    return;
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
              Utils.sortingDialog(context, activity, mCommand,mCommandInput,button);
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
              if (mCommand.getText() != null) {
                mCommandInput.setEndIconDrawable(
                    Utils.getDrawable(R.drawable.ic_add_bookmark, context));
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
    if (Utils.getBookmarks(context).size() <= 24) {
      Utils.addToBookmark(bookmark, context);
      Utils.snackBar(view, context.getString(R.string.bookmark_added_message, bookmark)).show();
    } else {
      if (switchState) {
        Utils.addToBookmark(bookmark, context);
        Utils.snackBar(view, context.getString(R.string.bookmark_added_message, bookmark)).show();
      } else {
        Utils.snackBar(view, context.getString(R.string.bookmark_limit_reached)).show();
      }
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
          if (Shizuku.pingBinder()
              && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            Utils.connectedDeviceDialog(context, device);
          } else {
            Utils.connectedDeviceDialog(context, context.getString(R.string.none));
          }
          mChip.setChecked(!mChip.isChecked());
        });
  }
}
