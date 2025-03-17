package in.hridayan.ashell.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.DocumentTreeUtil;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;

/**
 * A utility class that provides various settings dialogs. Designed for scalability,
 * maintainability, and high-performance enterprise-level applications.
 */
public class SettingsDialogs {

  /** Displays a dialog to select the preferred local ADB execution mode. */
  public static void localAdbModeDialog(Context context) {
    final CharSequence[] options = {
      context.getString(R.string.basic_shell),
      context.getString(R.string.shizuku),
      context.getString(R.string.root)
    };

    int currentSelection = Preferences.getLocalAdbMode();
    final int[] selectedMode = {currentSelection};

    showSingleChoiceDialog(
        context,
        context.getString(R.string.local_adb)
            + " "
            + context.getString(R.string.mode).toLowerCase(),
        options,
        currentSelection,
        (dialog, which) -> selectedMode[0] = which,
        () -> Preferences.setLocalAdbMode(selectedMode[0]));
  }

  /** Displays a dialog to choose the layout style for showing command examples. */
  public static void examplesLayoutStyleDialog(Context context) {
    final CharSequence[] options = {
      context.getString(R.string.list), context.getString(R.string.grid)
    };

    int currentSelection = Preferences.getExamplesLayoutStyle() - 1;
    final int[] selectedStyle = {currentSelection};

    showSingleChoiceDialog(
        context,
        context.getString(R.string.choose),
        options,
        currentSelection,
        (dialog, which) -> selectedStyle[0] = which + 1,
        () -> Preferences.setExamplesLayoutStyle(selectedStyle[0]));
  }

  /** Displays a dialog to select the preferred output saving option. */
  public static void savePreferenceDialog(Context context) {
    final CharSequence[] options = {
      context.getString(R.string.only_last_command_output), context.getString(R.string.whole_output)
    };

    int currentSelection = Preferences.getSavePreference();
    final int[] selectedPreference = {currentSelection};

    showSingleChoiceDialog(
        context,
        context.getString(R.string.save),
        options,
        currentSelection,
        (dialog, which) -> selectedPreference[0] = which,
        () -> Preferences.setSavePreference(selectedPreference[0]));
  }

  /** Displays a dialog to configure the save directory. */
  public static MaterialTextView configureSaveDirDialog(Context context, Activity activity) {
    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_choose_directory, null);

    MaterialTextView textView = setupTextView(context, dialogView);
    Button folderPicker = dialogView.findViewById(R.id.folder_picker);
    Chip reset = dialogView.findViewById(R.id.chip_reset);

    setupFolderPicker(activity, folderPicker);
    setupResetButton(context, reset, textView);

    new MaterialAlertDialogBuilder(context).setView(dialogView).show();

    return textView;
  }

  // Separate method to setup and return the MaterialTextView
  private static MaterialTextView setupTextView(Context context, View dialogView) {
    MaterialTextView textView = dialogView.findViewById(R.id.path);
    String outputSaveDirectory = Preferences.getSavedOutputDir();

    if (outputSaveDirectory.isEmpty()) {
      textView.setText(Const.PREF_DEFAULT_SAVE_DIRECTORY);
    } else {
      String outputSaveDirPath =
          DocumentTreeUtil.getFullPathFromTreeUri(Uri.parse(outputSaveDirectory), context);
      textView.setText(outputSaveDirPath);
    }

    return textView;
  }

  // Separate method to handle folder picker button
  private static void setupFolderPicker(Activity activity, Button folderPicker) {
    folderPicker.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          activity.startActivityForResult(
              new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), MainActivity.SAVE_DIRECTORY_CODE);
        });
  }

  // Separate method to handle reset button
  private static void setupResetButton(Context context, Chip reset, MaterialTextView textView) {
    Drawable icon = reset.getChipIcon();

    reset.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Utils.startAnim(icon);
          Preferences.setSavedOutputDir("");
          if (textView.getVisibility() == View.VISIBLE) {
            textView.setText(Const.PREF_DEFAULT_SAVE_DIRECTORY);
          }
        });
  }

  /** Shows a single-choice selection dialog with a confirmation button. */
  private static void showSingleChoiceDialog(
      Context context,
      String title,
      CharSequence[] options,
      int currentSelection,
      android.content.DialogInterface.OnClickListener selectionListener,
      Runnable confirmAction) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setSingleChoiceItems(options, currentSelection, selectionListener)
        .setPositiveButton(
            context.getString(R.string.choose), (dialog, which) -> confirmAction.run())
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show();
  }

  /** Inflates a dialog layout and returns the corresponding View. */
  private static View inflateDialogView(Context context, int layoutResId) {
    return LayoutInflater.from(context).inflate(layoutResId, null);
  }
}
