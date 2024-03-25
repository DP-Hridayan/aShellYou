package in.hridayan.ashell.utils;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import in.hridayan.ashell.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 28, 2022
 */
public class Utils {

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
    return mBookmarks;
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

  public static void copyToClipboard(String text, Context context) {
    ClipboardManager clipboard =
        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("Copied to clipboard", text);
    clipboard.setPrimaryClip(clip);
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
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
}
