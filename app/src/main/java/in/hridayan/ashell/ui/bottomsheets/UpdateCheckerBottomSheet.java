package in.hridayan.ashell.ui.bottomsheets;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.R;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.AppUpdater;
import in.hridayan.ashell.utils.HapticUtils;

public class UpdateCheckerBottomSheet {

    private final Activity activity;
    private final Context context;
    private BottomSheetDialog bottomSheetDialog;
    private View bottomSheetView;

    private MaterialTextView currentVersionTextView;
    private MaterialTextView latestVersionTextView;
    private MaterialTextView descriptionTextView;
    private MaterialButton downloadButton;
    private MaterialButton cancelButton;
    private LinearProgressIndicator progressBar;
    private LottieAnimationView loadingDots;
    private FrameLayout progressBarLayout;

    /**
     * Constructor to initialize the Update Checker Bottom Sheet.
     *
     * @param activity The activity context.
     * @param context  The application context.
     */
    public UpdateCheckerBottomSheet(@NonNull Activity activity, @NonNull Context context) {
        this.activity = activity;
        this.context = context;
        initializeBottomSheet();
    }

    /**
     * Initializes the bottom sheet dialog and its UI components.
     */
    private void initializeBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_update_checker, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Initialize UI components
        progressBarLayout = bottomSheetView.findViewById(R.id.progressBarLayout);
        currentVersionTextView = bottomSheetView.findViewById(R.id.current_version);
        latestVersionTextView = bottomSheetView.findViewById(R.id.latest_version);
        descriptionTextView = bottomSheetView.findViewById(R.id.body);
        downloadButton = bottomSheetView.findViewById(R.id.download_button);
        cancelButton = bottomSheetView.findViewById(R.id.cancel_button);
        progressBar = bottomSheetView.findViewById(R.id.download_progress);
        loadingDots = bottomSheetView.findViewById(R.id.loading_animation);

        adjustProgressBarLayout();
        setUpButtonListeners();
    }

    /**
     * Adjusts the progress bar layout to maintain height consistency.
     */
    private void adjustProgressBarLayout() {
        progressBarLayout.post(() -> {
            int currentHeight = progressBarLayout.getHeight();
            progressBarLayout.setMinimumHeight(currentHeight);
            ViewGroup.LayoutParams params = progressBarLayout.getLayoutParams();
            params.height = currentHeight;
            progressBarLayout.setLayoutParams(params);
        });
    }

    /**
     * Populates the UI with version information.
     */
    private void populateVersionInfo() {
        String currentVersionText = context.getString(R.string.current) + " " +
                context.getString(R.string.version) + " : " + BuildConfig.VERSION_NAME;
        String latestVersionText = context.getString(R.string.latest) + " " +
                context.getString(R.string.version) + " : " + Preferences.getLatestVersionName();

        currentVersionTextView.setText(currentVersionText);
        latestVersionTextView.setText(latestVersionText);
    }

    /**
     * Sets up listeners for the download and cancel buttons.
     */
    private void setUpButtonListeners() {
        downloadButton.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            AppUpdater.fetchLatestReleaseAndInstall(activity, progressBar, descriptionTextView, loadingDots, downloadButton);
        });

        cancelButton.setOnClickListener(v -> {
            HapticUtils.weakVibrate(v);
            dismiss();
        });
    }

    /**
     * Displays the update checker bottom sheet.
     */
    public void show() {
        populateVersionInfo();
        bottomSheetDialog.show();
    }

    /**
     * Dismisses the bottom sheet if it's currently displayed.
     */
    public void dismiss() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    /**
     * Checks if the bottom sheet is currently displayed.
     *
     * @return True if showing, otherwise false.
     */
    public boolean isShowing() {
        return bottomSheetDialog != null && bottomSheetDialog.isShowing();
    }
}