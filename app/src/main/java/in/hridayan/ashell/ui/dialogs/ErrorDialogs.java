package in.hridayan.ashell.ui.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;

/**
 * Handles error dialogs related to permission issues, missing dependencies, and OTG errors.
 */
public class ErrorDialogs {

    /**
     * Displays an error dialog when Shizuku is unavailable on the device.
     */
    public static void shizukuUnavailableDialog(Context context) {
        AlertDialog dialog = createDialog(context, R.layout.dialog_no_shizuku);

        Button close = dialog.findViewById(R.id.close);
        Button aboutShizuku = dialog.findViewById(R.id.aboutShizuku);

        close.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            dialog.dismiss();
        });

        aboutShizuku.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            Utils.openUrl(context, Const.URL_SHIZUKU_SITE);
        });
    }

    /**
     * Displays an error dialog when root access is unavailable.
     */
    public static void rootUnavailableDialog(Context context) {
        AlertDialog dialog = createDialog(context, R.layout.dialog_no_root);
        Button close = dialog.findViewById(R.id.close);

        close.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            dialog.dismiss();
        });
    }

    /**
     * Displays an error dialog when OTG connection fails.
     */
    public static void otgConnectionErrDialog(Context context) {
        AlertDialog dialog = createDialog(context, R.layout.dialog_no_otg_connection);
        Button close = dialog.findViewById(R.id.close);

        close.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            dialog.dismiss();
        });
    }

    /**
     * Displays a manual permission grant error dialog.
     */
    public static void grantPermissionManually(Context context) {
        new MaterialAlertDialogBuilder(context)
            .setTitle(R.string.error)
            .setMessage(R.string.grant_permission_manually)
            .setPositiveButton(R.string.ok, null)
            .show();
    }

    /**
     * Helper method to create an AlertDialog with a custom layout.
     */
    private static AlertDialog createDialog(Context context, int layoutResId) {
        View dialogView = LayoutInflater.from(context).inflate(layoutResId, null);
        return new MaterialAlertDialogBuilder(context).setView(dialogView).show();
    }
}