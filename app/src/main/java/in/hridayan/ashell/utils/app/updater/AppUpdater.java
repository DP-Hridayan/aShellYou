package in.hridayan.ashell.utils.app.updater;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import java.io.File;

public class AppUpdater {

  public enum DownloadStatus {
    STARTED,
    COMPLETED,
    FAILED,
    ERROR
  }

  public static void fetchLatestReleaseAndInstall(
      Activity activity,
      LinearProgressIndicator progressBar,
      MaterialTextView description,
      LottieAnimationView loadingDots,
      Button downloadButton) {

    handleViews(
        DownloadStatus.STARTED, activity, progressBar, description, loadingDots, downloadButton);

    ReleaseFetcher.fetchLatestRelease(
        new ReleaseFetcher.ReleaseCallback() {
          @Override
          public void onReleaseFound(String apkUrl, String apkFileName) {
            ApkDownloader.downloadApk(
                activity,
                apkUrl,
                apkFileName,
                progressBar,
                new ApkDownloader.DownloadCallback() {
                  @Override
                  public void onDownloadComplete(File apkFile) {
                    handleViews(
                        DownloadStatus.COMPLETED,
                        activity,
                        progressBar,
                        description,
                        loadingDots,
                        downloadButton);
                    ApkInstaller.installApk(activity, apkFile);
                  }

                  @Override
                  public void onDownloadFailed() {
                    showError(activity, progressBar, description, loadingDots, downloadButton);
                  }
                });
          }

          @Override
          public void onError(String errorMessage) {
            showError(activity, progressBar, description, loadingDots, downloadButton);
          }
        });
  }

  private static void showError(
      Activity activity,
      LinearProgressIndicator progressBar,
      MaterialTextView description,
      LottieAnimationView loadingDots,
      Button downloadButton) {
    activity.runOnUiThread(
        () -> {
          handleViews(
              DownloadStatus.ERROR,
              activity,
              progressBar,
              description,
              loadingDots,
              downloadButton);
          Toast.makeText(activity, activity.getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
  }

  private static void handleViews(
      DownloadStatus status,
      Activity activity,
      LinearProgressIndicator progressBar,
      MaterialTextView description,
      LottieAnimationView loadingDots,
      Button downloadButton) {

    switch (status) {
      case STARTED:
        if (description != null) description.setVisibility(View.GONE);
        if (downloadButton != null) downloadButton.setText(null);
        if (loadingDots != null) loadingDots.setVisibility(View.VISIBLE);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        break;

      case COMPLETED:
      case FAILED:
      case ERROR:
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (downloadButton != null) downloadButton.setText(activity.getString(R.string.download));
        if (loadingDots != null) loadingDots.setVisibility(View.GONE);
        if (description != null) description.setVisibility(View.VISIBLE);
        break;

      default:
        return;
    }
  }
}
