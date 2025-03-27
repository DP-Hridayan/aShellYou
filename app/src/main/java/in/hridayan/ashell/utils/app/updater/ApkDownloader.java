package in.hridayan.ashell.utils.app.updater;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import in.hridayan.ashell.R;
import in.hridayan.ashell.ui.ToastUtils;
import java.io.File;

public class ApkDownloader {
  public interface DownloadCallback {
    void onDownloadComplete(File apkFile);

    void onDownloadFailed();
  }

  public static void downloadApk(
      Activity activity,
      String url,
      String fileName,
      LinearProgressIndicator progressBar,
      DownloadCallback callback) {
    File dir = activity.getExternalFilesDir(null);
    if (dir != null) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.getName().endsWith(".apk")) {
            file.delete(); // Delete old APK file
          }
        }
      }
    }

    File apkFile = new File(dir, fileName);
    DownloadManager.Request request =
        new DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading Update")
            .setDescription("Downloading latest APK...")
            .setDestinationUri(Uri.fromFile(apkFile))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true);

    DownloadManager downloadManager =
        (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
    long downloadId = downloadManager.enqueue(request);

    new Thread(
            () -> {
              boolean downloading = true;
              DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);

              while (downloading) {
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                  int status =
                      cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                  int bytesDownloaded =
                      cursor.getInt(
                          cursor.getColumnIndexOrThrow(
                              DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                  int totalBytes =
                      cursor.getInt(
                          cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                  if (totalBytes > 0) {
                    int progress = (int) ((bytesDownloaded * 100L) / totalBytes);
                    new Handler(Looper.getMainLooper())
                        .post(() -> progressBar.setProgressCompat(progress, true));
                  }

                  if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;
                    cursor.close();
                    new Handler(Looper.getMainLooper())
                        .post(() -> callback.onDownloadComplete(apkFile));
                  } else if (status == DownloadManager.STATUS_FAILED) {
                    downloading = false;
                    cursor.close();
                    new Handler(Looper.getMainLooper())
                        .post(
                            () -> {
                              callback.onDownloadFailed();
                              ToastUtils.showToast(
                                  activity,
                                  activity.getString(R.string.failed),
                                  ToastUtils.LENGTH_SHORT);
                            });
                  }
                }
                try {
                  Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
              }
            })
        .start();
  }
}
