package in.hridayan.ashell.UI;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.R;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.AppUpdater;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;

public class BottomSheets {

  /* <--------BOTTOM SHEETS SHOWN FOR FEEDBACK -------> */

  // Bottom sheet showing the popup after the app is updated
  public static void showBottomSheetChangelog(Activity activity) {
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
    View bottomSheetView =
        LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_changelog, null);
    bottomSheetDialog.setContentView(bottomSheetView);
    bottomSheetDialog.show();

    MaterialTextView changelog, version;

    String versionName = BuildConfig.VERSION_NAME;
    version = bottomSheetView.findViewById(R.id.version);
    changelog = bottomSheetView.findViewById(R.id.changelog);
    version.setText(versionName);
    changelog.setText(Utils.loadChangelogText(versionName, activity));
  }

  // Bottom sheet showing the popup if an update is available
  public static void showBottomSheetUpdate(Activity activity, Context context) {
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
    View bottomSheetView =
        LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_update_checker, null);
    bottomSheetDialog.setContentView(bottomSheetView);
    bottomSheetDialog.show();

    MaterialTextView currentVersion = bottomSheetView.findViewById(R.id.current_version);
    MaterialTextView latestVersion = bottomSheetView.findViewById(R.id.latest_version);
    MaterialButton downloadButton = bottomSheetView.findViewById(R.id.download_button);
    MaterialButton cancelButton = bottomSheetView.findViewById(R.id.cancel_button);
    LinearProgressIndicator progressBar = bottomSheetView.findViewById(R.id.download_progress);

    currentVersion.setText(
        context.getString(R.string.current)
            + " "
            + context.getString(R.string.version)
            + " : "
            + BuildConfig.VERSION_NAME);
    latestVersion.setText(
        context.getString(R.string.latest)
            + " "
            + context.getString(R.string.version)
            + " : "
            + Preferences.getLatestVersionName());

    downloadButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          AppUpdater.fetchLatestReleaseAndInstall(activity, progressBar);
        });

    cancelButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          bottomSheetDialog.dismiss();
        });
  }
}
