package in.hridayan.ashell.UI.bottomsheets;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.Utils;

public class ChangelogBottomSheet {

    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;
    private View bottomSheetView;

    private MaterialTextView versionTextView;
    private MaterialTextView changelogTextView;

    /**
     * Constructor for initializing the changelog bottom sheet.
     *
     * @param activity The activity context used to create the dialog.
     */
    public ChangelogBottomSheet(@NonNull Activity activity) {
        this.activity = activity;
        initializeBottomSheet();
    }

    /**
     * Initializes the bottom sheet dialog and its components.
     */
    private void initializeBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_changelog, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        versionTextView = bottomSheetView.findViewById(R.id.version);
        changelogTextView = bottomSheetView.findViewById(R.id.changelog);
    }

    /**
     * Populates the changelog UI with the app version and changelog text.
     */
    private void populateChangelog() {
        String versionName = BuildConfig.VERSION_NAME;
        versionTextView.setText(versionName);
        changelogTextView.setText(Utils.loadChangelogText(versionName, activity));
    }

    /**
     * Displays the changelog bottom sheet.
     */
    public void show() {
        populateChangelog();
        bottomSheetDialog.show();
    }

    /**
     * Dismisses the changelog bottom sheet if it's showing.
     */
    public void dismiss() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    /**
     * Checks if the bottom sheet is currently displayed.
     *
     * @return True if the bottom sheet is visible, otherwise false.
     */
    public boolean isShowing() {
        return bottomSheetDialog != null && bottomSheetDialog.isShowing();
    }
}