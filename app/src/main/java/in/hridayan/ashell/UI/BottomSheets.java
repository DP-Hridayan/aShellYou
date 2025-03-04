package in.hridayan.ashell.UI;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.shell.WifiAdbShell;
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

  // The wireless debugging pairing and connect bottom sheet
  public static void showBottomSheetPairAndConnect(Context context, Activity activity) {
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
    View bottomSheetView =
        LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_pair_and_connect, null);
    bottomSheetDialog.setContentView(bottomSheetView);
    bottomSheetDialog.show();

    MaterialButton pairButton = bottomSheetView.findViewById(R.id.pair_button);
    MaterialButton connectButton = bottomSheetView.findViewById(R.id.connect_button);
    TextInputEditText pairingIPEditText = bottomSheetView.findViewById(R.id.ipAddressPairEditText);
    TextInputEditText pairingPortEditText = bottomSheetView.findViewById(R.id.pairingPortEditText);
    TextInputEditText pairingCodeEditText = bottomSheetView.findViewById(R.id.pairingCodeEditText);
    TextInputEditText connectingIPEditText =
        bottomSheetView.findViewById(R.id.ipAddressConnectEditText);
    TextInputEditText connectingPortEditText =
        bottomSheetView.findViewById(R.id.connectPortEditText);
    TextInputLayout pairingIPLayout = bottomSheetView.findViewById(R.id.ipAddressPairInputLayout);
    TextInputLayout pairingPortLayout = bottomSheetView.findViewById(R.id.pairingPortInputLayout);
    TextInputLayout pairingCodeLayout = bottomSheetView.findViewById(R.id.pairingCodeInputLayout);
    TextInputLayout connectingIPLayout =
        bottomSheetView.findViewById(R.id.ipAddressConnectInputLayout);
    TextInputLayout connectingPortLayout =
        bottomSheetView.findViewById(R.id.connectPortInputLayout);

    LinearLayout connectLayout = bottomSheetView.findViewById(R.id.connectLayout);

    // Add TextWatcher to remove errors as soon as user types
    TextWatcher clearErrorWatcher =
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (s == pairingIPEditText.getEditableText()) {
              pairingIPLayout.setError(null);
            } else if (s == pairingPortEditText.getEditableText()) {
              pairingPortLayout.setError(null);
            } else if (s == pairingCodeEditText.getEditableText()) {
              pairingCodeLayout.setError(null);
            }
          }
        };

    pairingIPEditText.addTextChangedListener(clearErrorWatcher);
    pairingPortEditText.addTextChangedListener(clearErrorWatcher);
    pairingCodeEditText.addTextChangedListener(clearErrorWatcher);

    pairButton.setOnClickListener(
        v -> {
          String pairingIP = pairingIPEditText.getText().toString().trim();
          String pairingPort = pairingPortEditText.getText().toString().trim();
          String pairingCode = pairingCodeEditText.getText().toString().trim();

          boolean hasError = false;

          if (pairingIP.isEmpty()) {
            pairingIPLayout.setError(context.getString(R.string.must_fill));
            hasError = true;
          }

          if (pairingPort.isEmpty()) {
            pairingPortLayout.setError(context.getString(R.string.must_fill));
            hasError = true;
          }

          if (pairingCode.isEmpty()) {
            pairingCodeLayout.setError(context.getString(R.string.must_fill));
            hasError = true;
          }

          if (!hasError) {
            WifiAdbShell.pair(
                context,
                pairingIP,
                pairingPort,
                pairingCode,
                new WifiAdbShell.PairingCallback() {
                  @Override
                  public void onSuccess() {
                    HapticUtils.weakVibrate(v);
                    connectLayout.setVisibility(View.VISIBLE);
                    pairButton.setText(context.getString(R.string.paired));
                    pairButton.setOnClickListener(
                        v -> {
                          ToastUtils.showToast(
                              context, context.getString(R.string.paired), ToastUtils.LENGTH_SHORT);
                        });
                  }

                  @Override
                  public void onFailure(String errorMessage) {
                    ToastUtils.showToast(context, context.getString(R.string.pairing_failed), ToastUtils.LENGTH_SHORT);
                  }
                });
          }
        });

    connectButton.setOnClickListener(
        v -> {
          String connectingIP = connectingIPEditText.getText().toString();
          String connectingPort = connectingPortEditText.getText().toString();

          boolean hasError = false;

          if (connectingIP.isEmpty()) {
            connectingIPLayout.setError(context.getString(R.string.must_fill));
            hasError = true;
          }

          if (connectingPort.isEmpty()) {
            connectingPortLayout.setError(context.getString(R.string.must_fill));
            hasError = true;
          }

          if (!hasError) {
            WifiAdbShell.connect(
                context,
                connectingIP,
                connectingPort,
                new WifiAdbShell.ConnectingCallback() {
                  @Override
                  public void onSuccess() {
                    HapticUtils.weakVibrate(v);
                    bottomSheetDialog.dismiss();
                    ToastUtils.showToast(
                        context, context.getString(R.string.connected), ToastUtils.LENGTH_SHORT);
                  }

                  @Override
                  public void onFailure(String errorMessage) {
                    ToastUtils.showToast(context, context.getString(R.string.pairing_failed), ToastUtils.LENGTH_SHORT);
                  }
                });
          }
        });
  }

  // Bottom sheet showing the popup if an update is available
  public static void showBottomSheetUpdate(Activity activity, Context context) {
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
    View bottomSheetView =
        LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_update_checker, null);
    bottomSheetDialog.setContentView(bottomSheetView);
    bottomSheetDialog.show();

    // Do a little tweaking to get the layout containing the progress bar the same height after text
    // description dissapears
    FrameLayout frameLayout = bottomSheetView.findViewById(R.id.progressBarLayout);
    frameLayout.post(
        () -> {
          int currentHeight = frameLayout.getHeight();
          frameLayout.setMinimumHeight(currentHeight);
          ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
          params.height = currentHeight; // Set max height
          frameLayout.setLayoutParams(params);
        });

    MaterialTextView currentVersion = bottomSheetView.findViewById(R.id.current_version);
    MaterialTextView latestVersion = bottomSheetView.findViewById(R.id.latest_version);
    MaterialTextView description = bottomSheetView.findViewById(R.id.body);
    MaterialButton downloadButton = bottomSheetView.findViewById(R.id.download_button);
    MaterialButton cancelButton = bottomSheetView.findViewById(R.id.cancel_button);
    LinearProgressIndicator progressBar = bottomSheetView.findViewById(R.id.download_progress);
    LottieAnimationView loadingDots = bottomSheetDialog.findViewById(R.id.loading_animation);

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
          AppUpdater.fetchLatestReleaseAndInstall(
              activity, progressBar, description, loadingDots, downloadButton);
        });

    cancelButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          bottomSheetDialog.dismiss();
        });
  }
}
