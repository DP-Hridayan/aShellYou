package in.hridayan.ashell.ui.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.ui.dialogs.ErrorDialogs;
import in.hridayan.ashell.shell.localadb.RootShell;
import in.hridayan.ashell.utils.HapticUtils;
import rikka.shizuku.Shizuku;

/**
 * Handles permission-related dialogs such as Shizuku and Root requests.
 */
public class PermissionDialogs {

    /**
     * Shows a dialog for requesting Shizuku permission.
     */
    public static void shizukuPermissionDialog(Context context) {
        View dialogView = inflateDialogView(context, R.layout.dialog_shizuku_perm);
        AlertDialog dialog = createDialog(context, dialogView);

        MaterialCardView requestButton = dialogView.findViewById(R.id.request_perm);
        MaterialCardView cancelButton = dialogView.findViewById(R.id.cancel);

        requestButton.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            Shizuku.requestPermission(0);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            dialog.dismiss();
        });
    }

    /**
     * Shows a dialog for requesting Root permission.
     */
    public static void rootPermissionDialog(Context context) {
        View dialogView = inflateDialogView(context, R.layout.dialog_root_perm);
        AlertDialog dialog = createDialog(context, dialogView);

        MaterialCardView requestButton = dialogView.findViewById(R.id.request_perm);
        MaterialCardView cancelButton = dialogView.findViewById(R.id.cancel);

        requestButton.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            RootShell.exec("su", true);
            RootShell.refresh();

            if (!RootShell.hasPermission()) {
                ErrorDialogs.grantPermissionManually(context);
            }

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            dialog.dismiss();
        });
    }

    /**
     * Inflates a dialog view from the given layout resource.
     */
    private static View inflateDialogView(Context context, int layoutRes) {
        return LayoutInflater.from(context).inflate(layoutRes, null);
    }

    /**
     * Creates and returns a MaterialAlertDialog.
     */
    private static AlertDialog createDialog(Context context, View view) {
        return new MaterialAlertDialogBuilder(context).setView(view).show();
    }
}