package in.hridayan.ashell.utils.app.updater;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import in.hridayan.ashell.config.Preferences;
import java.io.File;

public class ApkInstaller {

  public static void installApk(Activity activity, File apkFile) {
    if (!apkFile.exists() || apkFile.length() == 0) {
      Toast.makeText(activity, "Download completed, but APK file is missing!", Toast.LENGTH_SHORT)
          .show();
      return;
    }

    Preferences.setUpdateApkFileName(apkFile.getName());

    Uri apkUri =
        FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", apkFile);
    Intent installIntent =
        new Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, "application/vnd.android.package-archive")
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (!activity.getPackageManager().canRequestPackageInstalls()) {
        Preferences.setUnknownSourcePermAskStatus(true);
        activity.startActivity(
            new Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + activity.getPackageName())));
        return;
      }
    }
    activity.startActivity(installIntent);
  }
}
