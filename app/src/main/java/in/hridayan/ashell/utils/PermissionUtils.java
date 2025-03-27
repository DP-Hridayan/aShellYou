package in.hridayan.ashell.utils;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
  private static final int REQUEST_STORAGE_PERMISSION = 100;
  private static final int REQUEST_PICK_FILE = 101;

  public static boolean haveStoragePermission(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      return Environment.isExternalStorageManager();
    } else {
      return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED;
    }
  }

  public static void requestStoragePermission(Activity activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
      intent.setData(Uri.parse("package:" + activity.getPackageName()));
      activity.startActivityForResult(intent, REQUEST_STORAGE_PERMISSION);
    } else {
      ActivityCompat.requestPermissions(
          activity,
          new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
          REQUEST_STORAGE_PERMISSION);
    }
  }

  // Check if app has notification access
  public static boolean hasNotificationPermission(Context context) {
    boolean hasPermission;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      hasPermission =
          ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
              == PackageManager.PERMISSION_GRANTED;
    } else {
      NotificationManager notificationManager =
          (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      hasPermission = notificationManager != null && notificationManager.areNotificationsEnabled();
    }
    return hasPermission;
  }

  // Request notification access
  public static void openAppNotificationSettings(Context context) {
    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
    context.startActivity(intent);
  }
}
