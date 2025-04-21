package in.hridayan.ashell.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.AshellYou;
import in.hridayan.ashell.R;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.PermissionUtils;
import in.hridayan.ashell.utils.Utils;

/** Handles error dialogs related to permission issues, missing dependencies, and OTG errors. */
public class ErrorDialogs {

  /** Displays an error dialog when Shizuku is unavailable on the device. */
  public static void shizukuUnavailableDialog(Activity activity) {
    AlertDialog dialog = createDialog(activity, R.layout.dialog_no_shizuku);

    Button close = dialog.findViewById(R.id.close);
    Button action = dialog.findViewById(R.id.action);

    close.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dialog.dismiss();
        });

    boolean isShizukuInstalled = Utils.isAppInstalled(activity, Const.SHIZUKU_PACKAGE_NAME);

    action.setText(
        isShizukuInstalled
            ? activity.getString(R.string.open_shizuku)
            : activity.getString(R.string.shizuku));

    action.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          if (isShizukuInstalled) {
            Utils.launchApp(activity, Const.SHIZUKU_PACKAGE_NAME);
          } else {
            Utils.openUrl(activity, Const.URL_SHIZUKU_SITE);
          }
        });
  }

  /** Displays an error dialog when root access is unavailable. */
  public static void rootUnavailableDialog(Context context) {
    AlertDialog dialog = createDialog(context, R.layout.dialog_no_root);
    Button close = dialog.findViewById(R.id.close);

    close.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dialog.dismiss();
        });
  }

  /** Displays an error dialog when OTG connection fails. */
  public static void otgConnectionErrDialog(Context context) {
    AlertDialog dialog = createDialog(context, R.layout.dialog_no_otg_connection);
    Button close = dialog.findViewById(R.id.close);

    close.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dialog.dismiss();
        });
  }

  /** Displays a manual permission grant error dialog. */
  public static void grantPermissionManually(Context context) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(R.string.error)
        .setMessage(R.string.grant_permission_manually)
        .setPositiveButton(R.string.ok, null)
        .show();
  }

  public static void grantNotificationPermDialog(Context context) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(R.string.grant_permission)
        .setMessage(R.string.notification_access_not_granted)
        .setNegativeButton(R.string.close, null)
        .setPositiveButton(
            R.string.ok,
            (dialog, i) -> {
              PermissionUtils.openAppNotificationSettings(context);
            })
        .show();
  }

  /** Helper method to create an AlertDialog with a custom layout. */
  private static AlertDialog createDialog(Context context, int layoutResId) {
    View dialogView = LayoutInflater.from(context).inflate(layoutResId, null);
    return new MaterialAlertDialogBuilder(context).setView(dialogView).show();
  }
}
