package in.hridayan.ashell.UI.dialogs;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.DocumentTreeUtil;
import in.hridayan.ashell.utils.Utils;

/**
 * Handles feedback dialogs related to shell output, device connection, shell execution status,
 * and app exit confirmation.
 */
public class FeedbackDialogs {

    /**
     * Shows a dialog indicating whether the shell output was successfully saved or not.
     */
    public static void outputSavedDialog(Context context, boolean saved) {
        String saveDir = Environment.DIRECTORY_DOWNLOADS;
        String outputSaveDirectory = Preferences.getSavedOutputDir();

        if (!outputSaveDirectory.isEmpty()) {
            saveDir = DocumentTreeUtil.getFullPathFromTreeUri(Uri.parse(outputSaveDirectory), context);
        }

        String message = saved 
            ? getSuccessMessage(context, saveDir) 
            : context.getString(R.string.shell_output_not_saved_message);

        String title = saved 
            ? context.getString(R.string.success) 
            : context.getString(R.string.failed);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message);

        if (saved) {
            builder.setPositiveButton(context.getString(R.string.open), (dialog, i) ->
                Utils.openTextFileWithIntent(Preferences.getLastSavedFileName(), context)
            );
        }

        builder.setNegativeButton(context.getString(R.string.cancel), null).show();
    }

    /**
     * Shows a dialog displaying the name of the connected device for shell execution.
     */
    public static void connectedDeviceDialog(Context context, String connectedDevice) {
        View dialogView = inflateDialogView(context, R.layout.dialog_connected_device);
        MaterialTextView device = dialogView.findViewById(R.id.device);
        device.setText(connectedDevice);

        new MaterialAlertDialogBuilder(context).setView(dialogView).show();
    }

    /**
     * Displays a non-cancelable dialog indicating that the shell is currently working.
     */
    public static void shellWorkingDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setTitle(R.string.shell_working)
            .setMessage(R.string.app_working_message)
            .setPositiveButton(R.string.cancel, null)
            .show();
    }

    /**
     * Shows a confirmation dialog before exiting the application.
     */
    public static void confirmExitDialog(Context context, Activity activity) {
        new MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setTitle(R.string.confirm_exit)
            .setMessage(R.string.quit_app_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.quit, (dialog, i) -> activity.finish())
            .show();
    }

    /**
     * Retrieves the success message for saved shell output based on user preferences.
     */
    private static String getSuccessMessage(Context context, String saveDir) {
        return Preferences.getSavePreference() == Const.ALL_OUTPUT
            ? context.getString(R.string.shell_output_saved_whole_message, saveDir)
            : context.getString(R.string.shell_output_saved_message, saveDir);
    }

    /**
     * Inflates a dialog layout and returns the corresponding View.
     */
    private static View inflateDialogView(Context context, int layoutResId) {
        return LayoutInflater.from(context).inflate(layoutResId, null);
    }
}