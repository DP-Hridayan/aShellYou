package in.hridayan.ashell.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import in.hridayan.ashell.R;

public class AppUpdater {

  private static final String GITHUB_API_URL =
      "https://api.github.com/repos/"
          + Const.GITHUB_OWNER
          + "/"
          + Const.GITHUB_REPOSITORY
          + "/releases/latest";

  public static void fetchLatestReleaseAndInstall(
      Activity activity,
      LinearProgressIndicator progressBar,
      MaterialTextView description,
      LottieAnimationView loadingDots,
      Button downloadButton) {

    description.setVisibility(View.GONE);
    downloadButton.setText(null);
    loadingDots.setVisibility(View.VISIBLE); // The loading animation on download button
    progressBar.setVisibility(View.VISIBLE);

    new Thread(
            () -> {
              try {
                // Fetch release info
                HttpURLConnection connection =
                    (HttpURLConnection) new URL(GITHUB_API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Read response
                StringBuilder response = new StringBuilder();
                try (InputStream inputStream = connection.getInputStream()) {
                  byte[] buffer = new byte[1024];
                  int bytesRead;
                  while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.append(new String(buffer, 0, bytesRead));
                  }
                }

                // Parse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray assets = jsonResponse.getJSONArray("assets");

                String apkUrl = null, apkFileName = null;
                for (int i = 0; i < assets.length(); i++) {
                  JSONObject asset = assets.getJSONObject(i);
                  if (asset.getString("name").endsWith(".apk")) {
                    apkFileName = asset.getString("name");
                    apkUrl = asset.getString("browser_download_url");
                    break;
                  }
                }

                if (apkUrl == null) throw new Exception("No APK file found.");

                downloadAndInstallApk(
                    activity,
                    apkUrl,
                    apkFileName,
                    progressBar,
                    description,
                    loadingDots,
                    downloadButton);

              } catch (Exception e) {
                Log.e("AppUpdater", "Error fetching update", e);
                activity.runOnUiThread(
                    () -> {
                      progressBar.setVisibility(View.GONE);
                      loadingDots.setVisibility(
                          View.GONE); // The loading animation on download button
                      downloadButton.setText(R.string.download);
                      description.setVisibility(View.VISIBLE);
                      Toast.makeText(activity, "Failed: " + e.getMessage(), Toast.LENGTH_LONG)
                          .show();
                    });
              }
            })
        .start();
  }

  private static void downloadAndInstallApk(
      Activity activity,
      String url,
      String fileName,
      LinearProgressIndicator progressBar,
      MaterialTextView description,
      LottieAnimationView loadingDots,
      Button downloadButton) {

    File dir = activity.getExternalFilesDir(null);
    if (dir != null) {
      // Delete previously downloaded APK files
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
            .setDescription("Downloading latest Ashell APK...")
            .setDestinationUri(Uri.fromFile(apkFile))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true) // Allow on mobile data
            .setAllowedOverRoaming(true); // Allow during roaming

    DownloadManager downloadManager =
        (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
    long downloadId = downloadManager.enqueue(request);

    activity.runOnUiThread(
        () -> {
          progressBar.setVisibility(View.VISIBLE);
          progressBar.setProgress(0);
        });

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
                    activity.runOnUiThread(() -> progressBar.setProgressCompat(progress, true));
                  }

                  if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;
                    cursor.close();
                    activity.runOnUiThread(
                        () -> {
                          loadingDots.setVisibility(View.GONE);
                          progressBar.setVisibility(View.GONE);
                          downloadButton.setText(R.string.download);
                          description.setVisibility(View.VISIBLE);
                          promptInstall(activity, apkFile);
                        });
                  } else if (status == DownloadManager.STATUS_FAILED) {
                    downloading = false;
                    cursor.close();
                    activity.runOnUiThread(
                        () -> {
                          loadingDots.setVisibility(View.GONE);
                          progressBar.setVisibility(View.GONE);
                          downloadButton.setText(R.string.download);
                          description.setVisibility(View.VISIBLE);
                          Toast.makeText(activity, "Download failed!", Toast.LENGTH_SHORT).show();
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

  // Prompts the package installer to update the app
  public static void promptInstall(Activity activity, File apkFile) {
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
        Preferences.setUnknownSourcePermAskStatus(true); // Store flag
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
