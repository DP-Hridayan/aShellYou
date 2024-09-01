package in.hridayan.ashell.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.shell.RootShell;
import in.hridayan.ashell.utils.DocumentTreeUtil;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.List;
import rikka.shizuku.Shizuku;

public class DialogUtils {

  /* <--------DIALOGS SHOWN IN SETTINGS -------> */

  // Dialog asking to choose preferred local adb commands executing mode
  public static void localAdbModeDialog(Context context) {
    final CharSequence[] preferences = {
      context.getString(R.string.basic_shell),
      context.getString(R.string.shizuku),
      context.getString(R.string.root)
    };

    int savePreference = Preferences.getLocalAdbMode();
    final int[] preference = {savePreference};

    String title =
        context.getString(R.string.local_adb)
            + " "
            + context.getString(R.string.mode).toLowerCase();

    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setSingleChoiceItems(
            preferences,
            savePreference,
            (dialog, which) -> {
              preference[0] = which;
            })
        .setPositiveButton(
            context.getString(R.string.choose),
            (dialog, which) -> {
              Preferences.setLocalAdbMode(preference[0]);
            })
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show();
  }

  // Dialog asking to choose default launch mode
  public static void defaultLaunchModeDialog(Context context) {

    final CharSequence[] workingModes = {
      context.getString(R.string.local_adb),
      context.getString(R.string.otg),
      context.getString(R.string.remember_working_mode)
    };

    int defaultWorkingMode = Preferences.getLaunchMode();
    final int[] workingMode = {defaultWorkingMode};

    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.launch_mode))
        .setSingleChoiceItems(
            workingModes,
            defaultWorkingMode,
            (dialog, which) -> {
              workingMode[0] = which;
            })
        .setPositiveButton(
            context.getString(R.string.choose),
            (dialog, which) -> {
              Preferences.setLaunchMode(workingMode[0]);
            })
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show();
  }

  // Dialog asking to choose which layout style to use show command examples
  public static void examplesLayoutStyleDialog(Context context) {
    final CharSequence[] preferences = {
      context.getString(R.string.list), context.getString(R.string.grid)
    };

    int savePreference = Preferences.getExamplesLayoutStyle() - 1;
    final int[] preference = {savePreference};

    String title = context.getString(R.string.choose);

    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setSingleChoiceItems(
            preferences,
            savePreference,
            (dialog, which) -> {
              preference[0] = which + 1;
            })
        .setPositiveButton(
            context.getString(R.string.choose),
            (dialog, which) -> {
              Preferences.setExamplesLayoutStyle(preference[0]);
            })
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show();
  }

  // Dialog asking to choose preferred output saving option
  public static void savePreferenceDialog(Context context) {
    final CharSequence[] preferences = {
      context.getString(R.string.only_last_command_output), context.getString(R.string.whole_output)
    };

    int savePreference = Preferences.getSavePreference();
    final int[] preference = {savePreference};

    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.save))
        .setSingleChoiceItems(
            preferences,
            savePreference,
            (dialog, which) -> {
              preference[0] = which;
            })
        .setPositiveButton(
            context.getString(R.string.choose),
            (dialog, which) -> {
              Preferences.setSavePreference(preference[0]);
            })
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show();
  }

  /* <--------DIALOGS SHOWN AFTER ERRORS -------> */

  /*Method to display a dialog which asks user to manually grant root permission*/
  public static void grantPermissionManually(Context context) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.error))
        .setMessage(context.getString(R.string.grant_permission_manually))
        .setPositiveButton(context.getString(R.string.ok), null)
        .show();
  }

  // Display error when shizuku is not installed or running on the device
  public static void shizukuUnavailableDialog(Context context) {
    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_no_shizuku, null);

    AlertDialog dialog = new MaterialAlertDialogBuilder(context).setView(dialogView).show();

    Button close = dialogView.findViewById(R.id.close);
    Button aboutShizuku = dialogView.findViewById(R.id.aboutShizuku);

    close.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dialog.dismiss();
        });

    aboutShizuku.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Utils.openUrl(context, Const.URL_SHIZUKU_SITE);
        });
  }

  public static void rootUnavailableDialog(Context context) {
    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_no_root, null);

    AlertDialog dialog = new MaterialAlertDialogBuilder(context).setView(dialogView).show();

    Button close = dialogView.findViewById(R.id.close);

    close.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dialog.dismiss();
        });
  }

  /* <--------DIALOGS SHOWN TO REQUEST PERMISSION -------> */

  // Method for displaying the shizuku permission requesting dialog
  public static void shizukuPermRequestDialog(Context context) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.access_denied))
        .setMessage(context.getString(R.string.shizuku_access_denied_message))
        .setNegativeButton(context.getString(R.string.cancel), null)
        .setPositiveButton(
            context.getString(R.string.request_permission),
            (dialogInterface, i) -> Shizuku.requestPermission(0))
        .show();
  }

  // Method for displaying the root permission requesting dialog
  public static void rootPermRequestDialog(Context context) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.access_denied))
        .setMessage(context.getString(R.string.root_access_denied_message))
        .setNegativeButton(context.getString(R.string.cancel), null)
        .setPositiveButton(
            context.getString(R.string.request_permission),
            (dialogInterface, i) -> {
              RootShell.exec("su", true);
              RootShell.refresh();
              if (!RootShell.hasPermission()) DialogUtils.grantPermissionManually(context);
            })
        .show();
  }

  /* <--------DIALOGS SHOWN FOR FEEDBACK PURPOSES -------> */

  // Dialog to show if the shell output is saved or not
  public static void outputSavedDialog(Context context, boolean saved) {

    String saveDir = Environment.DIRECTORY_DOWNLOADS;

    String outputSaveDirectory = Preferences.getSavedOutputDir();
    if (!outputSaveDirectory.equals("")) {
      saveDir = DocumentTreeUtil.getFullPathFromTreeUri(Uri.parse(outputSaveDirectory), context);
    }

    String successMessage =
        Preferences.getSavePreference() == Const.ALL_OUTPUT
            ? context.getString(R.string.shell_output_saved_whole_message, saveDir)
            : context.getString(R.string.shell_output_saved_message, saveDir);
    String message =
        saved ? successMessage : context.getString(R.string.shell_output_not_saved_message);
    String title = saved ? context.getString(R.string.success) : context.getString(R.string.failed);

    MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(message);
    if (saved) {
      builder.setPositiveButton(
          context.getString(R.string.open),
          (dialogInterface, i) -> {
            Utils.openTextFileWithIntent(Preferences.getLastSavedFileName(), context);
          });
    }

    builder.setNegativeButton(context.getString(R.string.cancel), null).show();
  }

  /* <--------DIALOGS SHOWN TO PERFORM SOME ACTION -------> */

  // Dialog showing all the bookmarked items
  public static void bookmarksDialog(
      Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {

    List<String> bookmarks = Utils.getBookmarks(context);

    int totalBookmarks = bookmarks.size();

    String title = context.getString(R.string.bookmarks) + " (" + totalBookmarks + ")";

    CharSequence[] bookmarkItems = new CharSequence[bookmarks.size()];
    for (int i = 0; i < bookmarks.size(); i++) {
      bookmarkItems[i] = bookmarks.get(i);
    }

    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setItems(
            bookmarkItems,
            (dialog, which) -> {
              mCommand.setText(bookmarks.get(which));
              mCommand.setSelection(mCommand.getText().length());
            })
        .setPositiveButton(context.getString(R.string.cancel), null)
        .setNegativeButton(
            context.getString(R.string.sort),
            (dialogInterface, i) -> {
              sortingDialog(context, mCommand, mCommandInput);
            })
        .setNeutralButton(
            context.getString(R.string.delete_all),
            (DialogInterface, i) -> {
              deleteDialog(context, mCommand, mCommandInput);
            })
        .show();
  }

  // Dialog shown to confirm if the user actually wants to delete all bookmarks
  public static void deleteDialog(
      Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {

    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.confirm_delete))
        .setMessage(context.getString(R.string.confirm_delete_message))
        .setPositiveButton(
            context.getString(R.string.ok),
            (dialogInterface, i) -> {
              List<String> bookmarks = Utils.getBookmarks(context);
              for (String item : bookmarks) {
                Utils.deleteFromBookmark(item, context);
              }
              String s = mCommand.getText().toString();
              if (s.length() != 0) mCommandInput.setEndIconDrawable(R.drawable.ic_add_bookmark);
              else mCommandInput.setEndIconVisible(false);
            })
        .setNegativeButton(
            context.getString(R.string.cancel),
            (dialogInterface, i) -> {
              bookmarksDialog(context, mCommand, mCommandInput);
            })
        .setOnCancelListener(
            v -> {
              List<String> bookmarks = Utils.getBookmarks(context);
              if (bookmarks.size() != 0) bookmarksDialog(context, mCommand, mCommandInput);
            })
        .show();
  }

  // Dialog showing sorting options for bookmarks
  public static void sortingDialog(
      Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
    CharSequence[] sortingOptions = {
      context.getString(R.string.sort_A_Z),
      context.getString(R.string.sort_Z_A),
      context.getString(R.string.sort_newest),
      context.getString(R.string.sort_oldest)
    };
    int currentSortingOption = Preferences.getSortingOption();

    final int[] sortingOption = {currentSortingOption};

    new MaterialAlertDialogBuilder(context)
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
              Preferences.setSortingOption(sortingOption[0]);
              bookmarksDialog(context, mCommand, mCommandInput);
            })
        .setNegativeButton(
            context.getString(R.string.cancel),
            (dialog, i) -> {
              bookmarksDialog(context, mCommand, mCommandInput);
            })
        .setOnCancelListener(
            v -> {
              bookmarksDialog(context, mCommand, mCommandInput);
            })
        .show();
  }

  // Method to show a dialog showing the device name on which shell is being executed
  public static void connectedDeviceDialog(Context context, String connectedDevice) {
    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_connected_device, null);

    MaterialTextView device = dialogView.findViewById(R.id.device);

    device.setText(connectedDevice);

    new MaterialAlertDialogBuilder(context).setView(dialogView).show();
  }

  // show shell working dialog
  public static void shellWorkingDialog(Context context) {
    new MaterialAlertDialogBuilder(context)
        .setCancelable(false)
        .setTitle(context.getString(R.string.shell_working))
        .setMessage(context.getString(R.string.app_working_message))
        .setPositiveButton(context.getString(R.string.cancel), null)
        .show();
  }

  // asks confirmation dialog before exiting the app
  public static void confirmExitDialog(Context context, Activity activity) {
    new MaterialAlertDialogBuilder(context)
        .setCancelable(false)
        .setTitle(R.string.confirm_exit)
        .setMessage(context.getString(R.string.quit_app_message))
        .setNegativeButton(context.getString(R.string.cancel), null)
        .setPositiveButton(
            context.getString(R.string.quit), (dialogInterface, i) -> activity.finish())
        .show();
  }

  // configure save directory dialog
  public static MaterialTextView configureSaveDirDialog(Context context, Activity activity) {
    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_choose_directory, null);

    MaterialTextView textView = dialogView.findViewById(R.id.path);
    Button folderPicker = dialogView.findViewById(R.id.folder_picker);
    Chip reset = dialogView.findViewById(R.id.chip_reset);

    String outputSaveDirectory = Preferences.getSavedOutputDir();
    if (outputSaveDirectory.equals("")) {
      textView.setText(Const.PREF_DEFAULT_SAVE_DIRECTORY);
    } else {
      String outputSaveDirPath =
          DocumentTreeUtil.getFullPathFromTreeUri(Uri.parse(outputSaveDirectory), context);

      textView.setText(outputSaveDirPath);
    }

    folderPicker.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          activity.startActivityForResult(
              new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), MainActivity.SAVE_DIRECTORY_CODE);
        });

    reset.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          Preferences.setSavedOutputDir("");
          if (textView != null && textView.getVisibility() == View.VISIBLE)
            textView.setText(Const.PREF_DEFAULT_SAVE_DIRECTORY);
        });

    new MaterialAlertDialogBuilder(context).setView(dialogView).show();

    return textView;
  }
}
